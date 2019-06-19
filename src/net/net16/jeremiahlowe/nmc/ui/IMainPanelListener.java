package net.net16.jeremiahlowe.nmc.ui;

import javax.swing.JPanel;

import net.net16.jeremiahlowe.nmc.lib.NMCLibrary;

public interface IMainPanelListener {
	public void onMainPanelChanged(NMCMainUI ui, NMCLibrary newOwner, JPanel newPanel);
	public void onMainPanelCleared(NMCMainUI ui);
}
