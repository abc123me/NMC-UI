package test;

import net.net16.jeremiahlowe.nmc.NMCInstance;
import net.net16.jeremiahlowe.nmc.lib.NMCLibrary;
import net.net16.jeremiahlowe.nmc.ui.Module;

public class TestLibrary extends NMCLibrary{
	
	Module myModule;
	
	public TestLibrary(NMCInstance instance) {
		super(instance);
		myModule = new Module() {
			@Override public String getModuleName() {
				return getDisplayName();
			}
			/*@Override public JComponent getListComponent() {
				return new JButton(getDisplayName());
			}*/
			@Override public void onModuleAdded() {
				System.out.println("Test library module added!");
			}
			@Override public void onModuleRemoved() {
				System.out.println("Test library module removed!");
			}
		};
	}

	@Override public String getDisplayName() {
		return "Test library";
	}
	@Override public void onLibraryLoaded() {
		System.out.println("Test library adding modules...");
		addModule(myModule);
		System.out.println("Test library loaded!");
	}
	@Override public void onLibraryUnload() {
		System.out.println("Test library removing modules...");
		removeModule(myModule);
		System.out.println("Test library unloaded!");
	}
}
