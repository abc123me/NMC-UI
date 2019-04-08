package net.net16.jeremiahlowe.nmc.lib;

import java.util.ArrayList;

import net.net16.jeremiahlowe.nmc.NMCInstance;
import net.net16.jeremiahlowe.nmc.ui.Module;

public abstract class NMCLibrary {
	public final NMCInstance instance;
	
	private ArrayList<Module> modules;
	
	public NMCLibrary(NMCInstance instance) {
		this.instance = instance;
		modules = new ArrayList<Module>();
	}
	
	/*
	 * Return error string if there was an error
	 * or returns null if there was no error.
	 */
	public String onLibraryLoad() {
		return null;
	}
	public final void preLibraryUnload() {
		System.out.println("Unloading all modules");
		for(Module m : modules)
			instance.removeModule(m);
	}
	public void onLibraryUnload() {}
	public void onLibraryLoaded() {}
	public abstract String getDisplayName();
	
	protected void addModule(Module m) {
		modules.add(m);
		instance.addModule(m);
	}
	protected void removeModule(Module m) {
		modules.remove(m);
		instance.removeModule(m);
	}
}
