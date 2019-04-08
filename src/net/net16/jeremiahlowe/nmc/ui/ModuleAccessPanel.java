package net.net16.jeremiahlowe.nmc.ui;

import javax.swing.*;

import java.awt.*;
import java.util.*;

public class ModuleAccessPanel extends JPanel{
	private HashMap<Module, JComponent> modulePairs;
	private JPanel holder;
	private JScrollPane pane;
	private GridLayout holderLayout;
	
	public ModuleAccessPanel() {
		modulePairs = new HashMap<Module, JComponent>();
		setLayout(new BorderLayout());
		pane = new JScrollPane();
		pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		add(pane, BorderLayout.CENTER);
		holder = new JPanel();
		holder.setBackground(Color.WHITE);
		holderLayout = new GridLayout(0, 1);
		holder.setLayout(holderLayout);
		pane.setViewportView(holder);
	}
	
	public void addModules(Module... m) {
		for(Module t : m)
			_addModule(t);
		invalidate();
	}
	public void addModule(Module m) {
		_addModule(m);
		for(Module m2 : modulePairs.keySet()) {
			JComponent c = modulePairs.get(m2);
			if(!c.isValid()) c.revalidate();
		}
		invalidate();
	}
	private void _addModule(Module m) {
		JComponent l = modulePairs.get(m);
		if(l == null) {
			l = m.getListComponent();
			if(l == null) {
				JOptionPane.showMessageDialog(this, "Failed to add module to module list!", "Error", ERROR);
				return;
			}
			l.setVisible(true);
			holder.add(l);
			int w = l.getPreferredSize().width;
			if(w > pane.getWidth()) {
				Dimension d = getPreferredSize();
				d.width += w - getWidth();
				setPreferredSize(d);
			}
		}
		modulePairs.put(m, l);
		m.onModuleAdded();
	}
	public void removeModule(Module m) {
		JComponent l = modulePairs.get(m);
		if(l != null) {
			holder.remove(l);
		}
		if(modulePairs.remove(m) != null)
			m.onModuleRemoved();
	}
}
