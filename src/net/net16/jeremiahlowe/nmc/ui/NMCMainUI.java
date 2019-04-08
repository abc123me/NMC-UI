package net.net16.jeremiahlowe.nmc.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.net16.jeremiahlowe.nmc.NMCInstance;
import net.net16.jeremiahlowe.nmc.lib.*;
import net.net16.jeremiahlowe.shared.*;
import net.net16.jeremiahlowe.shared.ansi.AnsiTextPane;

public class NMCMainUI extends JPanel{
	
	public static void main(String[] args) throws LibraryException {
		JFrame fr = new JFrame();
		fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fr.setSize(800, 600);
		SwingUtility.centerJFrame(fr);
		NMCMainUI ui = new NMCMainUI();
		fr.setContentPane(ui);
		fr.setVisible(true);
	}
	
	public final NMCInstance ins;
	public Logger selectedLogger = Logger.getGlobal();
	
	private NMCLibrary mainPanelOwner = null;
	private ArrayList<Duplet<NMCLibrary, JMenuItem>> libraries = new ArrayList<>(); 
	
	private ModuleAccessPanel map = new ModuleAccessPanel();
	private JPanel mp = new JPanel();
	private JPanel oldMainPanel = null;
	private AnsiTextPane loggingPane = null;
	
	private JMenuBar menuBar = new JMenuBar();
	private JMenu fileMenu = new JMenu("File");
	private JMenuItem fileMenuLoadConfig = new JMenuItem("Load config");
	private JMenuItem fileMenuSaveConfig = new JMenuItem("Save config");
	private JMenuItem fileMenuNewConfig = new JMenuItem("New config");
	private JMenuItem fileMenuLoadLibrary = new JMenuItem("Load library");
	private JMenu fileMenuUnloadLibraryMenu = new JMenu("Unload library", false);
	private JMenu editMenu = new JMenu("Edit");
	private JMenu loggingMenu = new JMenu("Logging");
	private JCheckBoxMenuItem loggingMenuShowLogs = new JCheckBoxMenuItem("Show logs");
	private JCheckBoxMenuItem loggingMenuShowANSI = new JCheckBoxMenuItem("Use ANSI");
	private JMenu loggingMenuLogLevelsMenu = new JMenu("Set log level");
	private JRadioButtonMenuItem[] loggingMenuLogLevelButtons;
	private ButtonGroup loggingMenuLogLevelButtonGroup = new ButtonGroup();
	
	public NMCMainUI() {
		setLayout(new BorderLayout());
		mp.setLayout(new BorderLayout());
		add(map, BorderLayout.WEST);
		initFileMenu();
		initEditMenu();
		initLoggingMenu();
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(loggingMenu);
		add(menuBar, BorderLayout.NORTH);
		ins = new NMCInstance(map, this);
		Dimension d = map.getPreferredSize();
		d.width += 50; //Add 50 to make it look nicer
		map.setPreferredSize(d);
	}
	
	public void setMainView(NMCLibrary user, JPanel c) {
		if(oldMainPanel != null) remove(oldMainPanel);
		mainPanelOwner = user;
		add(c, BorderLayout.CENTER);
		c.revalidate();
		oldMainPanel = c;
	}
	public void clearMainView() {
		if(oldMainPanel != null) remove(oldMainPanel);
		oldMainPanel = null;
		mainPanelOwner = null;
		invalidate();
	}
	public void insertLibrary(NMCLibrary l) {
		if(l == null)
			throw new NullPointerException("Libray is null");
		JMenuItem m = new JMenuItem(l.getDisplayName());
		m.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ins.unloadLibrary(l);
			}
		});
		fileMenuUnloadLibraryMenu.add(m);
		libraries.add(new Duplet<NMCLibrary, JMenuItem>(l, m));
		fileMenuUnloadLibraryMenu.setEnabled(true);
	}
	public void removeLibrary(NMCLibrary l) {
		if(l == null)
			throw new NullPointerException("Library is null");
		Duplet<NMCLibrary, JMenuItem> r = null;
		for(Duplet<NMCLibrary, JMenuItem> d : libraries) {
			if(d == null) continue;
			NMCLibrary n = d.a;
			if(n == mainPanelOwner)
				clearMainView();
			JMenuItem m = d.b;
			if(n != null && m != null && n == l) {
				fileMenuUnloadLibraryMenu.remove(m);
				r = d; break;
			}
		}
		if(r != null)
			libraries.remove(r);
		libraries.trimToSize();
		if(libraries.size() <= 0)
			fileMenuUnloadLibraryMenu.setEnabled(false);
	}

	private void initFileMenu() {
		fileMenu.add(fileMenuLoadConfig);
		fileMenu.add(fileMenuSaveConfig);
		fileMenu.add(fileMenuNewConfig);
		fileMenu.add(fileMenuLoadLibrary);
		NMCMainUI ui = this;
		fileMenuLoadLibrary.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				File f = chooseFile(ui, null, "JAR files", "jar");
				if(f == null) return;
				try {
					ins.loadLibraryFromJar(f);
				} catch (LibraryException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(ui, e1.getLocalizedMessage(), "Error message", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		fileMenuUnloadLibraryMenu.setEnabled(false);
		fileMenu.add(fileMenuUnloadLibraryMenu);
	}
	private void initEditMenu() {
		
	}
	private void initLoggingMenu() {
		loggingMenu.add(loggingMenuShowLogs);
		loggingMenu.add(loggingMenuShowANSI);
		loggingMenu.add(loggingMenuLogLevelsMenu);
		int l = NMCInstance.LOG_LEVELS.length;
		loggingMenuLogLevelButtons = new JRadioButtonMenuItem[l];
		Level selectedLevel = selectedLogger.getLevel();
		if(selectedLevel == null) selectedLevel = Level.ALL;
		for(int i = 0; i < l; i++) {
			Level level = NMCInstance.LOG_LEVELS[i];
			JRadioButtonMenuItem btn = new JRadioButtonMenuItem(level.getLocalizedName(), level == selectedLevel);
			loggingMenuLogLevelButtons[i] = btn;
			loggingMenuLogLevelsMenu.add(btn);
			loggingMenuLogLevelButtonGroup.add(btn);
			btn.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent arg0) {
					updateSelectedLogLevel(level);
				}
			});
		}
		updateSelectedLogLevel(selectedLevel);
	}
	private void updateSelectedLogLevel(Level to) {
		selectedLogger.setLevel(to);
		selectedLogger.info("Updated log level to: " + to.getLocalizedName());
	}
	private static final File chooseFile(JComponent parent, File d, String desc, String... names) {
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new FileNameExtensionFilter(desc, names));
		fc.showOpenDialog(parent);
		File f = fc.getSelectedFile();
		if(f == null) return d;
		return f;
	}
}
