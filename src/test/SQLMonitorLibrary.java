package test;

import net.net16.jeremiahlowe.nmc.NMCInstance;
import net.net16.jeremiahlowe.nmc.lib.telemetry.TelemetryMonitoringLibrary;

public class SQLMonitorLibrary extends TelemetryMonitoringLibrary{

	public final SQLConnection conn;
	
	public SQLMonitorLibrary(NMCInstance instance, String name, SQLConnection conn, int updateRate) {
		super(instance, name, updateRate);
		this.conn = conn;
	}

	@Override
	public String[] getTelemetryKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getCurrentValue(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getValueAt(String key, long epoch, short millis) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void onUpdate() {
		// TODO Auto-generated method stub
		
	}
	
}
