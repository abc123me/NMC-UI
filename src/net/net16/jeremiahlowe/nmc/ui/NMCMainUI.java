package net.net16.jeremiahlowe.nmc.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.net16.jeremiahlowe.nmc.NMCInstance;
import net.net16.jeremiahlowe.nmc.lib.*;
import net.net16.jeremiahlowe.nmc.libs.telemetry.TelemetryMonitoringLibrary;
import net.net16.jeremiahlowe.shared.*;

public class NMCMainUI extends JPanel{
	public static void main(String[] args) throws LibraryException {
		NMCInstance.makeGUI(800, 600);
		Logger.getGlobal().info("test!");
		System.out.println("stdout: test");
		System.err.println("stderr: test");
		Logger.getGlobal().warning("test!");
		Logger.getGlobal().severe("test!");
	}
	
	public final NMCInstance ins;
	public Logger selectedLogger = Logger.getGlobal();
	
	private NMCLibrary mainPanelOwner = null;
	private ArrayList<Duplet<NMCLibrary, JMenuItem>> libraries = new ArrayList<>(); 
	private ArrayList<IMainPanelListener> mainPanelListeners = new ArrayList<>();
	private boolean logModuleChanges = true;
	private boolean logLibraryChanges = true;
	private float mapPercent = 0.2f;
	private float logPercent = 0.25f;
	
	private ModuleAccessPanel map;
	private JPanel mainPanel = new JPanel();
	private JPanel oldMainPanel = null;
	private LoggingPane loggingPane = null;
	
	private JMenuBar menuBar = new JMenuBar();

	private JMenu fileMenu = new JMenu("File");
	private JMenuItem fileMenuLoadConfig = new JMenuItem("Load config");
	private JMenuItem fileMenuSaveConfig = new JMenuItem("Save config");
	private JMenuItem fileMenuNewConfig = new JMenuItem("New config");
	private JMenuItem fileMenuLoadLibrary = new JMenuItem("Load library");
	private JMenu fileMenuUnloadLibraryMenu = new JMenu("Unload library", false);
	
	private JMenu editMenu = new JMenu("Edit");
	private JMenu editMenuTimezoneMenu = new JMenu("Set timezone");
	
	private JMenu loggingMenu = new JMenu("Logging");
	private JCheckBoxMenuItem loggingMenuShowLogs = new JCheckBoxMenuItem("Show logs");
	private JCheckBoxMenuItem loggingMenuLogModules = new JCheckBoxMenuItem("Log module changes", logModuleChanges);
	private JCheckBoxMenuItem loggingMenuLogLibraries = new JCheckBoxMenuItem("Log library changes", logLibraryChanges);
	private JMenu loggingMenuLogLevelsMenu = new JMenu("Set log level");
	private JRadioButtonMenuItem[] loggingMenuLogLevelButtons;
	private ButtonGroup loggingMenuLogLevelButtonGroup = new ButtonGroup();
	
	public NMCMainUI(NMCInstance ins) { this(ins, true); }
	public NMCMainUI(NMCInstance ins, boolean bind) {
		if(ins == null)
			throw new NullPointerException("NMCInstance cannot be null");
		this.ins = ins;
		setLayout(new BorderLayout());
		mainPanel.setLayout(new BorderLayout());
		map = new ModuleAccessPanel(this);
		//loggingPane.setEditable(false);
		add(mainPanel, BorderLayout.CENTER);
		if(ins.logger != null) {
			loggingPane = new LoggingPane(ins.logger);
			mainPanel.add(loggingPane, BorderLayout.SOUTH);
		} else {
			System.err.println("NMCInstance has no logger, ignoring it");
			loggingPane = null;
		}
		add(map, BorderLayout.WEST);
		initFileMenu();
		initEditMenu();
		initLoggingMenu();
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(loggingMenu);
		add(menuBar, BorderLayout.NORTH);
		updateSizes();
		if(loggingPane != null) {
			loggingPane.setGlobalBackground(getBackground().brighter());
			loggingPane.setEditable(false);
		}
		if(bind) ins.bindUI(this);
	}
	public void updateSizes() {
		Dimension mainPanelSize = mainPanel.getSize();
		Dimension d = null, mainSize = getSize();
		if(loggingPane != null) {
			d = loggingPane.getPreferredSize();
			d.height = Math.round(mainPanelSize.height * logPercent);
			loggingPane.setPreferredSize(d);
		}
		if(map != null) {
			d = map.getPreferredSize();
			d.width = Math.round(mainSize.width * mapPercent);
			map.setPreferredSize(d);
		}
	}
	
	/* =====================================
	 *        Library UI control shit
	 * ===================================== */
	public void setMainView(NMCLibrary user, JPanel c) {
		if(oldMainPanel != null) mainPanel.remove(oldMainPanel);
		mainPanelOwner = user;
		mainPanel.add(c, BorderLayout.CENTER);
		oldMainPanel = c;
		mainPanel.validate();
		for(IMainPanelListener l : mainPanelListeners)
			if(l != null) l.onMainPanelChanged(this, user, c);
	}
	public void clearMainView() {
		if(oldMainPanel != null) mainPanel.remove(oldMainPanel);
		oldMainPanel = null;
		mainPanelOwner = null;
		mainPanel.validate();
		for(IMainPanelListener l : mainPanelListeners)
			if(l != null) l.onMainPanelCleared(this);
	}
	public void insertLibrary(NMCLibrary l) {
		if(l == null)
			throw new NullPointerException("Libray is null");
		JMenuItem m = new JMenuItem(l.getDisplayName());
		m.addActionListener((ActionEvent ae) -> { ins.unloadLibrary(l); });
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
	public void removeModule(Module m) {
		map.removeModule(m);
		if(logModuleChanges)
			ins.logger.info("Removed module: " + m.getModuleName());
	}
	public void addModule(Module m) {
		map.addModule(m);
		if(logModuleChanges)
			ins.logger.info("Added module: " + m.getModuleName());
	}
	
	/* ==================================
	 *        Getters and setters
	 * ================================== */
	public ModuleAccessPanel getModuleAccessPanel() { return map; }
	public JMenuBar getMenuBar() { return menuBar; }
	public JMenu getFileMenu() { return fileMenu; }
	public JMenu getEditMenu() { return editMenu; }
	public JMenu getLoggingMenu() { return loggingMenu; }
	public void addMainPanelListener(IMainPanelListener l) { mainPanelListeners.add(l); }
	public void removeMainPanelListener(IMainPanelListener l) { mainPanelListeners.remove(l); }
	
	/* ==================================
	 *        UI initialization
	 * ================================== */
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
					Duplet<NMCLibrary, NMCLibraryInfo> d = ins.loadLibraryFromJar(f);
					if(d == null) ins.logger.severe("Failed to load library, loadLibraryFromJar returned null!");
					else if(d.a == null) ins.logger.severe("Failed to load library, library was null!");
					else if(d.b == null) ins.logger.warning("Error loading library, library information was null!");
					else if(logLibraryChanges) ins.logger.info("Loaded library " + d.a.getDisplayName());
						
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
		
		initTimezoneMenu();
	}
	private void initTimezoneMenu() {
		ButtonGroup timezoneBtnGroup = new ButtonGroup();
		ArrayList<String> places = new ArrayList<String>();
		editMenu.add(editMenuTimezoneMenu);
		//TODO: this crap
	}
	private void initLoggingMenu() {
		loggingMenu.add(loggingMenuShowLogs);
		loggingMenu.add(loggingMenuLogLevelsMenu);
		loggingMenu.add(loggingMenuLogModules);
		loggingMenu.add(loggingMenuLogLibraries);
		loggingMenuShowLogs.addActionListener((ActionEvent a) -> {
			boolean s = loggingMenuShowLogs.isSelected();
			loggingPane.setVisible(s);
			validate();
		});
		loggingMenuLogLibraries.addActionListener((ActionEvent a) -> { logLibraryChanges = loggingMenuLogLibraries.isSelected(); });
		loggingMenuLogModules.addActionListener((ActionEvent a) -> { logModuleChanges = loggingMenuLogModules.isSelected(); });
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
			btn.addActionListener((ActionEvent a) -> { updateSelectedLogLevel(level); });
		}
		updateSelectedLogLevel(selectedLevel);
	}
	
	/* ==================================
	 *           Random shit
	 * ================================== */
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
