package net.net16.jeremiahlowe.nmc.ui;

import javax.swing.*;

public abstract class Module {
	public abstract String getModuleName();
	public void onModuleAdded() {};
	public void onModuleRemoved() {};
	
	public JComponent getListComponent() {
		JLabel l = new JLabel("Module: " + getModuleName());
		return l;
	}
}
