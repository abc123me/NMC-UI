package net.net16.jeremiahlowe.nmc.lib.telemetry;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;

import net.net16.jeremiahlowe.nmc.NMCInstance;
import net.net16.jeremiahlowe.nmc.lib.NMCLibrary;
import net.net16.jeremiahlowe.nmc.ui.Module;

public abstract class TelemetryMonitoringLibrary extends NMCLibrary implements ITelemetrySource{
	public final String subsystemName;
	public final Module subsystemModule;
	
	protected final JPanel telemetryPanel = new JPanel(new BorderLayout());
	protected final JSlider sliderCurrent = new JSlider(JSlider.HORIZONTAL);
	protected final JSpinner spinnerBeginDate = new JSpinner(new SpinnerDateModel());
	protected final JSpinner spinnerEndDate = new JSpinner(new SpinnerDateModel());
	protected final JCheckBox chkBoxUseCurrentTime = new JCheckBox("Now");
	protected final Timer updateTimer;
	
	private HashMap<String, JLabel> telemetryLabels = new HashMap<String, JLabel>();
	private Date beginDate = new Date(0), endDate = new Date(System.currentTimeMillis());
	
	public TelemetryMonitoringLibrary(NMCInstance instance, String name, int updateRate) {
		super(instance);
		subsystemName = name;
		subsystemModule = new Module() {
			@Override public String getModuleName() { return subsystemName; }
			@Override public JComponent getListComponent() { return getModuleListComponent(); }
			@Override public void onModuleRemoved() { stopUpdater(); }
		};
		updateTimer = new Timer(updateRate, (ActionEvent e) -> update());
		setDateRange(new Date(0), new Date(System.currentTimeMillis()));
	}
	
	protected void generateTelemetryLabels(JComponent addTo) {
		telemetryLabels.clear();
		String[] keys = getTelemetryKeys();
		for(String key : keys) {
			JLabel lbl = new JLabel(key + ": Unset");
			telemetryLabels.put(key, lbl);
			addTo.add(lbl);
		}
	}
	protected void setDisplayDateMode(boolean showCurrentDate) {
		chkBoxUseCurrentTime.setSelected(showCurrentDate);
		showCurrentDate = !showCurrentDate;
		spinnerBeginDate.setEnabled(showCurrentDate);
		sliderCurrent.setEnabled(showCurrentDate);
		spinnerEndDate.setEnabled(showCurrentDate);
	}
	protected void generateTimeNavigator(JComponent addTo) {
		addTo.add(chkBoxUseCurrentTime);
		addTo.add(spinnerBeginDate);
		addTo.add(sliderCurrent);
		addTo.add(spinnerEndDate);
		sliderCurrent.addChangeListener((ChangeEvent e) -> {
			//sliderCurrent.setText("test");
		});
		chkBoxUseCurrentTime.addActionListener((ActionEvent e) -> {
			setDisplayDateMode(chkBoxUseCurrentTime.isSelected());
		}); setDisplayDateMode(true);
		spinnerBeginDate.setPreferredSize(new Dimension(0, 0));
	}
	@Override public void onLibraryLoaded() {
		// Initialize main panel components
		Box telemetryLabelsBox = Box.createVerticalBox();
		Box timeNavigationBox = Box.createHorizontalBox();
		generateTelemetryLabels(telemetryLabelsBox);
		generateTimeNavigator(timeNavigationBox);
		telemetryPanel.add(timeNavigationBox, BorderLayout.SOUTH);
		telemetryPanel.add(telemetryLabelsBox, BorderLayout.CENTER);
		// Load the subsystem's module into the UI
		addModule(subsystemModule);
		// Call the supermethod (safety)
		super.onLibraryLoaded();
	}
	@Override public String getDisplayName() { return subsystemName; }
	public void startUpdater() { updateTimer.start(); }
	public void stopUpdater() { updateTimer.stop(); }
	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
		spinnerBeginDate.setValue(beginDate);
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
		spinnerEndDate.setValue(endDate);
	}
	public void setDateRange(Date begin, Date end) {
		if(begin.getTime() > end.getTime()) {
			Date d = begin;
			begin = end;
			end = d;
		}
		setBeginDate(begin);
		setEndDate(end);
	}
	public Date getBeginDate() { return beginDate; }
	public Date getEndDate() { return endDate; }
	public Date getSelectedDate() { return null; }
	public long getDurationMS() { return getEndDate().getTime() - getBeginDate().getTime(); }
	
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
	protected void showTelemetry() {
		String[] keys = getTelemetryKeys();
		Date d = null;
		if(chkBoxUseCurrentTime.isSelected()) d = new Date(System.currentTimeMillis());
		else d = getSelectedDate();
		boolean prepped = prepareValuesAt(keys, d);
		for(String key : keys) {
			JLabel lbl = telemetryLabels.get(key);
			if(lbl == null) continue;
			else lbl.setText(key + ": " + getValueAt(key, d, prepped));
		}
		telemetryPanel.validate();
	}
	protected void update() {
		onUpdate();
		if(telemetryPanel.isVisible())
			showTelemetry();
	}
	
	private void onModuleClicked() {
		if(instance.hasUI())
			instance.getUI().setMainView(this, telemetryPanel);
	}

	
	protected abstract void onUpdate();
}
