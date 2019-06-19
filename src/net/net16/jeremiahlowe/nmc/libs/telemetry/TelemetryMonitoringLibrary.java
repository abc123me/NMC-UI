package net.net16.jeremiahlowe.nmc.libs.telemetry;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import net.net16.jeremiahlowe.nmc.NMCInstance;
import net.net16.jeremiahlowe.nmc.lib.NMCLibrary;
import net.net16.jeremiahlowe.nmc.ui.IMainPanelListener;
import net.net16.jeremiahlowe.nmc.ui.Module;
import net.net16.jeremiahlowe.nmc.ui.NMCMainUI;
import net.net16.jeremiahlowe.shared.SwingUtility;

public abstract class TelemetryMonitoringLibrary extends NMCLibrary implements ITelemetrySource, IMainPanelListener {
	//Library elements
	public final String subsystemName;
	public final Module subsystemModule;
	private final JPanel libraryMainPanel = new JPanel(new BorderLayout());
	//Telemetry menu
	protected JMenu telemetryMenu = new JMenu("Telemetry");
	private final JMenu telemetryMenuTimelineViewMenu = new JMenu("Timeline");
	private final JMenuItem telemetryMenuUpdateSpeed = new JMenuItem("Update speed");
	private final ButtonGroup timelineMenuBtnGrp = new ButtonGroup();
	private final HashMap<ETimelinePanelMode, JRadioButtonMenuItem> timelineMenuModeButtons;
	private HashMap<String, JLabel> telemetryLabels = new HashMap<String, JLabel>();
	//Timeline view
	private final TimelinePanel timelinePanel = new TimelinePanel();
	//Telemetry view
	protected final JPanel telemetryPanel = new JPanel();
	//Updater
	private Thread updateThread = null;
	private long updateRate = -1; //-1 means off
	
	//-----------------------------------
	//           Constructor
	//-----------------------------------
	public TelemetryMonitoringLibrary(NMCInstance instance, String name, int updateRate) {
		super(instance);
		if(!instance.hasUI()) 
			throw new RuntimeException("This library does not support headless mode!");
		subsystemName = name;
		subsystemModule = new Module() {
			@Override public String getModuleName() { return subsystemName; }
			@Override public JComponent getListComponent() { return getModuleListComponent(); }
			@Override public void onModuleRemoved() {}
		};
		timelineMenuModeButtons = new HashMap<ETimelinePanelMode, JRadioButtonMenuItem>();
		createLibraryMainPanel();
		createTelemetryTimelineMenu();
		createUpdaterMenu();
	}
	private void createUpdaterMenu() {
		telemetryMenuUpdateSpeed.addActionListener((ActionEvent e) -> {
			openUpdateSpeedDialog();
		});
		telemetryMenu.add(telemetryMenuUpdateSpeed);
	}
	private void createLibraryMainPanel() {
		libraryMainPanel.add(timelinePanel, BorderLayout.SOUTH);
		//timelinePanel.addChangeListener((ChangeEvent ce) -> requestUpdate());
		libraryMainPanel.add(telemetryPanel, BorderLayout.CENTER);
		telemetryPanel.setLayout(new BoxLayout(telemetryPanel, BoxLayout.PAGE_AXIS));
		instance.getUI().addMainPanelListener(this);
	}
	private void createTelemetryTimelineMenu() {
		for(ETimelinePanelMode mode : timelinePanel.getSupportedModes()) {
			JRadioButtonMenuItem btn = new JRadioButtonMenuItem();
			btn.setText(mode.toString());
			btn.addActionListener((ActionEvent e) -> timelinePanel.setMode(mode));
			timelineMenuBtnGrp.add(btn);
			timelineMenuModeButtons.put(mode, btn);
			telemetryMenuTimelineViewMenu.add(btn);
		}
		telemetryMenu.add(telemetryMenuTimelineViewMenu);
	}
	
	//-----------------------------------
	//           NMCLibrary
	//-----------------------------------
	@Override public void onLibraryLoaded() {
		addModule(subsystemModule); // Load the subsystem's module into the UI
		super.onLibraryLoaded(); // Call the supermethod (safety)
	}
	@Override public String getDisplayName() { return subsystemName; }
	@Override public void onLibraryUnload() {
		if(updateThread != null)
			updateThread.interrupt();
		super.onLibraryUnload(); // Call the supermethod (safety)
	}
	//-----------------------------------
	//        IMainPanelListener
	//-----------------------------------
	@Override public void onMainPanelChanged(NMCMainUI ui, NMCLibrary newOwner, JPanel newPanel) {
		if(ui == null) return;
		if(newOwner == this) ui.getMenuBar().add(telemetryMenu);
		else ui.getMenuBar().remove(telemetryMenu);
		ui.validate();
	}
	@Override public void onMainPanelCleared(NMCMainUI ui) {
		if(ui == null) return;
		ui.getMenuBar().remove(telemetryMenu);
		ui.validate();
	}
	//-----------------------------------
	//    Internal utility functions
	//-----------------------------------
	protected JComponent getModuleListComponent() {
		Box hbox = Box.createHorizontalBox();
		JButton btn = new JButton(subsystemName);
		hbox.add(btn);
		btn.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent ae) {
				onModuleClicked();
			}
		});
		Dimension s = hbox.getPreferredSize();
		s.width = Math.max(s.width, btn.getPreferredSize().width);
		hbox.setPreferredSize(s);
		return hbox;
	}
	private void updateTelemetryPanel() {
		String[] keys = getTelemetryKeys();
		Date d = getSelectedDate();
		boolean prepped = prepareValuesAt(keys, d);
		for(String key : keys) {
			String txt = String.format("%s: %s", key, getValueAt(key, d, prepped));
			JLabel lbl = telemetryLabels.get(key);
			if(lbl == null) {
				lbl = new JLabel();
				telemetryLabels.put(key, lbl);
				telemetryPanel.add(lbl);
			}
			lbl.setText(txt);
		}
		libraryMainPanel.validate();
	}
	private void onModuleClicked() {
		instance.getUI().setMainView(this, libraryMainPanel);
	}
	private void openUpdateSpeedDialog() {
		//Create objects
		JPanel p = new JPanel(new GridLayout(4, 2));
		long updateRate = getUpdateRate();
		boolean autoUpdates = isUpdatingAutomatic();
		long m = autoUpdates ? updateRate : 0; //             VALUE                       MINIMUM      MAXIMUM                   STEP
		JSpinner msSpinner = new JSpinner(new SpinnerNumberModel(new Long(m % 1000),         new Long(0), new Long(1000),           new Long(50)));
		JSpinner sSpinner = new JSpinner( new SpinnerNumberModel(new Long((m /= 1000) % 60), new Long(0), new Long(60),             new Long(5)));
		JSpinner mSpinner = new JSpinner( new SpinnerNumberModel(new Long(m /= 60),          new Long(0), new Long(Long.MAX_VALUE), new Long(5)));
		JCheckBox autoUpdateChkBox = new JCheckBox();
		ActionListener valueShowListener = (ActionEvent ae) -> {
			boolean en = autoUpdateChkBox.isSelected();
			msSpinner.setEnabled(en);
			sSpinner.setEnabled(en);
			mSpinner.setEnabled(en);
		};
		autoUpdateChkBox.addActionListener(valueShowListener);
		autoUpdateChkBox.setSelected(autoUpdates);
		valueShowListener.actionPerformed(null);
 		JButton okBtn = new JButton("OK"), cancelBtn = new JButton("Cancel");
		p.add(new JLabel("Minutes: "));			p.add(mSpinner);
		p.add(new JLabel("Seconds: "));			p.add(sSpinner);
		p.add(new JLabel("Milliseconds: "));	p.add(msSpinner);
		p.add(new JLabel("Auto-Update?"));		p.add(autoUpdateChkBox);
		JPanel p1 = new JPanel();
		p1.setLayout(new BoxLayout(p1, BoxLayout.LINE_AXIS));
		p1.add(okBtn); p1.add(cancelBtn);
		//Put it all together
		JPanel p2 = new JPanel(new BorderLayout());
		p2.add(new JLabel("Update speed"), BorderLayout.NORTH);
		p2.add(p, BorderLayout.CENTER);
		p2.add(p1, BorderLayout.SOUTH);
		JFrame j = new JFrame("Update speed");
		j.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		j.setContentPane(p2); j.pack();
		ActionListener close = (ActionEvent e) -> // 1 line close lamda \/
			j.dispatchEvent(new WindowEvent(j, WindowEvent.WINDOW_CLOSING));
		cancelBtn.addActionListener(close);
		okBtn.addActionListener((ActionEvent e) -> {
			if(autoUpdateChkBox.isSelected()) {
				long ms = (long) msSpinner.getValue();
				ms += ((long) sSpinner.getValue()) * 1000;
				ms += ((long) mSpinner.getValue()) * 60000;
				setUpdateRate(ms);
			} else disableAutomaticUpdating();
		}); okBtn.addActionListener(close);
		SwingUtility.centerJFrame(j);
		j.setVisible(true);
	}
	//-----------------------------------
	//    Exposed utility functions
	//-----------------------------------
	public boolean requestUpdate() {
		if(needsUpdated()) {
			update();
			return true;
		}
		return false;
	}
	public void update() {
		if(libraryMainPanel.isVisible())
			updateTelemetryPanel();
	}
	public boolean needsUpdated() {
		return true;
	}
	//-----------------------------------
	//        Getters/Setters
	//-----------------------------------
	public Date getSelectedDate() { return timelinePanel.getSelectedDate(); }
	public void disableAutomaticUpdating() { setUpdateRate(-1); }
	public void setUpdateRate(long ms) {
		updateRate = ms;
		// Stop auto-updating
		if(updateThread != null) {
			updateThread.interrupt();
			updateThread = null;
		} 
		if(updateRate < 0)
			return;
		// Start auto-updating
		final long time = ms;
		updateThread = new Thread(() -> {
			try{ while(!Thread.interrupted()) {
				requestUpdate();
				Thread.sleep(time);
			}} catch(InterruptedException ie) {}
		});
		updateThread.setName("Update thread");
		updateThread.setPriority(Thread.MAX_PRIORITY);
		updateThread.start();
	}
	public long getUpdateRate() { return updateRate; }
	public boolean isUpdatingAutomatic() { return updateRate >= 0; }
}
