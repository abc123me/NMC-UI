package test;

import java.util.Date;

import net.net16.jeremiahlowe.nmc.NMCInstance;
import net.net16.jeremiahlowe.nmc.lib.telemetry.TelemetryMonitoringLibrary;

public class TelemetryGeneratorTest extends TelemetryMonitoringLibrary {
	public static final String SINE_KEY = "Sine wave";
	public static final String COSINE_KEY = "Cosine wave";
	public static final String TAN_KEY = "Tangent wave";
	
	public static void main(String[] args) throws Exception {
		NMCInstance ins = NMCInstance.makeGUI(800, 600);
		TelemetryGeneratorTest tl = new TelemetryGeneratorTest(ins);
		ins.loadLibrary(tl, TelemetryGeneratorTest.class.getName());
	}
	
	public TelemetryGeneratorTest(NMCInstance instance) {
		super(instance, "Math functions", 0);
		updateTimer.start();
	}

	@Override public String[] getTelemetryKeys() {
		return new String[] { SINE_KEY, COSINE_KEY, TAN_KEY };
	}
	@Override public Object getValueAt(String key, Date when, boolean prep) {
		double time = when.getTime() / 1000.0;
		if(SINE_KEY.contentEquals(key)) return Math.sin(time);
		if(COSINE_KEY.contentEquals(key)) return Math.cos(time);
		if(TAN_KEY.contentEquals(key)) return Math.tan(time);
		return null;
	}
	@Override protected void onUpdate() { }
}
