package net.net16.jeremiahlowe.nmc.ui;

import javax.swing.*;

import java.awt.*;
import java.util.*;

public class ModuleAccessPanel extends JPanel{
	private final Container parent;
	private HashMap<Module, JComponent> modulePairs;
	private JPanel viewport;
	private JScrollPane pane;
	
	public ModuleAccessPanel() {
		this(null);
	}
	public ModuleAccessPanel(Container parent) {
		this.parent = parent;
		modulePairs = new HashMap<Module, JComponent>();
		setLayout(new BorderLayout());
		pane = new JScrollPane();
		pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(pane, BorderLayout.CENTER);
		viewport = new JPanel();
		viewport.setBackground(Color.WHITE);
		viewport.setLayout(new BoxLayout(viewport, BoxLayout.PAGE_AXIS));
		viewport.setAlignmentX(Component.LEFT_ALIGNMENT);
		viewport.setAlignmentY(Component.TOP_ALIGNMENT);
		pane.setViewportView(viewport);
	}
	
	public void addModules(Module... m) {
		for(Module t : m)
			_addModule(t);
		Container parent = this.parent != null ? this.parent : getParent();
		if(parent != null)
			parent.validate();
	}
	public void addModule(Module m) {
		_addModule(m);
		for(Module m2 : modulePairs.keySet()) {
			JComponent c = modulePairs.get(m2);
			if(!c.isValid()) c.revalidate();
		}
		Container parent = this.parent != null ? this.parent : getParent();
		if(parent != null)
			parent.validate();
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
			viewport.add(l);
			int w = l.getPreferredSize().width;
			if(w > pane.getWidth()) {
				Dimension d = getPreferredSize();
				d.width += w - getWidth();
				setPreferredSize(d);
			}
		}
		modulePairs.put(m, l);
		m.onModuleAdded();
		viewport.validate();
	}
	public void removeModule(Module m) {
		JComponent l = modulePairs.get(m);
		if(l != null) 
			viewport.remove(l);
		if(modulePairs.remove(m) != null)
			m.onModuleRemoved();
		viewport.validate();
	}
}
