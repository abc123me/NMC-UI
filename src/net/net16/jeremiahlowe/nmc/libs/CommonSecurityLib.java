package net.net16.jeremiahlowe.nmc.libs;

import net.net16.jeremiahlowe.nmc.NMCInstance;
import net.net16.jeremiahlowe.nmc.lib.NMCLibrary;

//TODO: CommonSecurityLib.java
public class CommonSecurityLib extends NMCLibrary{
	public CommonSecurityLib(NMCInstance instance) {
		super(instance);
	}

	@Override
	public String getDisplayName() {
		return "Common security manager";
	}
}
