package net.net16.jeremiahlowe.nmc.lib.telemetry;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;
import javax.swing.Timer;

import net.net16.jeremiahlowe.nmc.NMCInstance;
import net.net16.jeremiahlowe.nmc.lib.NMCLibrary;
import net.net16.jeremiahlowe.nmc.ui.Module;

public abstract class TelemetryMonitoringLibrary extends NMCLibrary implements ITelemetrySource{
	public final String subsystemName;
	public final Module subsystemModule;
	
	protected final JPanel telemetryPanel;
	protected final Timer updateTimer;
	
	private HashMap<String, JLabel> telemetryLabels;
	
	public TelemetryMonitoringLibrary(NMCInstance instance, String name, int updateRate) {
		super(instance);
		subsystemName = name;
		subsystemModule = generateSubsystemModule();
		telemetryPanel = new JPanel(new BorderLayout());
		telemetryLabels = new HashMap<String, JLabel>();
		updateTimer = new Timer(updateRate, new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				update();
			}
		});
		updateTimer.start();
	}
	
	@Override public void onLibraryLoaded() {
		telemetryLabels.clear();
		String[] keys = getTelemetryKeys();
		Box vbox = Box.createVerticalBox();
		for(String key : keys) {
			JLabel lbl = new JLabel(key + ": Unset");
			telemetryLabels.put(key, lbl);
			vbox.add(lbl);
		}
		telemetryPanel.add(vbox);
		
		addModule(subsystemModule);
		super.onLibraryLoaded();
	}
	@Override public String getDisplayName() {
		return subsystemName;
	}
	
	protected abstract void onUpdate();
	
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
		s.width += btn.getPreferredSize().width / 10;
		hbox.setPreferredSize(s);
		return hbox;
	}
	protected void showTelemetry() {
		String[] keys = getTelemetryKeys();
		for(String key : keys) {
			if(key == null) continue;
			JLabel lbl = telemetryLabels.get(key);
			lbl.setText(key + ": " + getCurrentValue(key));
		}
		telemetryPanel.invalidate();
	}
	protected void update() {
		onUpdate();
		if(telemetryPanel.isVisible())
			showTelemetry();
	}
	
	private void onModuleClicked() {
		if(instance.ui != null)
			instance.ui.setMainView(this, telemetryPanel);
		update();
	}
	private Module generateSubsystemModule() {
		return new Module() {
			@Override public String getModuleName() { return subsystemName; }
			@Override public JComponent getListComponent() { return getModuleListComponent(); }
		};
	}
}
