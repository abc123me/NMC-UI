package net.net16.jeremiahlowe.nmc.lib.telemetry;

public interface ITelemetrySource {
	public String[] getTelemetryKeys();
	
	public Object getCurrentValue(String key);
	public Object getValueAt(String key, long epoch, short millis);
}
