package net.net16.jeremiahlowe.nmc.libs.telemetry;

import javax.swing.*;

import java.util.ArrayList;
import java.util.Date;
import java.awt.Container;
import java.awt.event.ActionEvent;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class TimelinePanel extends JPanel {
	private ETimelinePanelMode mode = ETimelinePanelMode.Disabled;
	private ArrayList<ChangeListener> changeListeners;
	private boolean changed = false;
	private ChangeListener internalChangeListener = (ChangeEvent e) -> onDateChanged();
	private final JCheckBox chkBoxUseCurrentTime = new JCheckBox("Now");
	private final JSpinner spinnerDateRequired = new JSpinner(new SpinnerDateModel());
	private final JSpinner spinnerDateOptional = new JSpinner(new SpinnerDateModel());
	private final JSlider sliderDateSelection = new JSlider(JSlider.HORIZONTAL);
	
	public TimelinePanel() {
		changeListeners = new ArrayList<ChangeListener>();
		spinnerDateRequired.addChangeListener(internalChangeListener);
		spinnerDateOptional.addChangeListener(internalChangeListener);
		sliderDateSelection.setMinimum(Integer.MIN_VALUE); //-2M
		sliderDateSelection.setMaximum(Integer.MAX_VALUE); //+2M
		sliderDateSelection.setValue(0); //The sesolution should handle 4K
		sliderDateSelection.addChangeListener(internalChangeListener);
		updateComponents();
	}
	
	private void onDateChanged() {
		if(mode == ETimelinePanelMode.Disabled)
			return; // *cue* how did I get here from Inter-Dimensional Cable II
		ChangeEvent internal = new ChangeEvent(this);
		for(ChangeListener cl : changeListeners) 
			if(cl != null) cl.stateChanged(internal);
		changed = true;
	}
	private void updateComponents() {
		removeAll();
		switch(mode) {
			case Scrollbar:
				add(chkBoxUseCurrentTime);
				add(spinnerDateRequired);
				add(sliderDateSelection);
				add(spinnerDateOptional);
				chkBoxUseCurrentTime.addActionListener((ActionEvent e) -> {
					setVariableOptionsEnabled(!chkBoxUseCurrentTime.isSelected());
				}); setVariableOptionsEnabled(true);
				break;
			case ExactDate:
				add(chkBoxUseCurrentTime);
				add(spinnerDateRequired);
				break;
		}
		validate();
		Container parent = getParent();
		if(parent != null) parent.validate();
	}
	private void setVariableOptionsEnabled(boolean en) {
		spinnerDateRequired.setEnabled(en);
		sliderDateSelection.setEnabled(en);
		spinnerDateOptional.setEnabled(en);
	}

	//Getters/Setters
	public void addChangeListener(ChangeListener cl) { changeListeners.add(cl); }
	public void removeChangeListener(ChangeListener cl) { changeListeners.remove(cl); }
	public ArrayList<ChangeListener> getChangeListeners(){ return changeListeners; }
	public ETimelinePanelMode getMode() { return mode; }
	public ETimelinePanelMode[] getSupportedModes() { return ETimelinePanelMode.values(); }
	public void setMode(ETimelinePanelMode mode) {
		this.mode = mode;
		updateComponents();
	}
	public Date getSelectedDate() {
		if(chkBoxUseCurrentTime.isSelected())
			return new Date(System.currentTimeMillis());
		switch(mode) {
			case ExactDate: 
				return (Date) spinnerDateRequired.getValue();
			case Scrollbar: 
				long rMin = ((Date) spinnerDateRequired.getValue()).getTime();
				long rStop = ((Date) spinnerDateOptional.getValue()).getTime() - rMin;
				long min = (long) sliderDateSelection.getMinimum();
				long start = (long) sliderDateSelection.getMaximum() - min;
				long stop = (long) sliderDateSelection.getValue() - min;
				return new Date(Math.round(((start / (double) stop) * rStop) + rMin));
		}
		return null;
	}
	public boolean changed() {
		boolean old = changed;
		changed = false;
		return old;
	}
}
enum ETimelinePanelMode {
	Disabled, ExactDate, Scrollbar;
	public String toString() {
		switch(this) {
			case ExactDate: return "Exact date";
			default: return name();
		}
	}
}