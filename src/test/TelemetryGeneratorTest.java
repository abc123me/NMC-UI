package test;

import javax.swing.JFrame;

import net.net16.jeremiahlowe.nmc.NMCInstance;
import net.net16.jeremiahlowe.nmc.lib.telemetry.TelemetryMonitoringLibrary;
import net.net16.jeremiahlowe.nmc.ui.NMCMainUI;
import net.net16.jeremiahlowe.shared.SwingUtility;

public class TelemetryGeneratorTest extends TelemetryMonitoringLibrary {
	public static final String SINE_KEY = "Sine wave";
	public static final String COSINE_KEY = "Cosine wave";
	public static final String TAN_KEY = "Tangent wave";
	
	public static void main(String[] args) throws Exception {
		JFrame fr = new JFrame();
		fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fr.setSize(800, 600);
		SwingUtility.centerJFrame(fr);
		NMCMainUI ui = new NMCMainUI();
		fr.setContentPane(ui);
		fr.setVisible(true);
		TelemetryGeneratorTest tl = new TelemetryGeneratorTest(ui.ins);
		ui.ins.loadLibrary(tl, TelemetryGeneratorTest.class.getName());
	}
	
	public TelemetryGeneratorTest(NMCInstance instance) {
		super(instance, "Math functions", 0);
		updateTimer.start();
	}

	@Override public String[] getTelemetryKeys() {
		return new String[] { SINE_KEY, COSINE_KEY, TAN_KEY };
	}
	@Override public Object getCurrentValue(String key) {
		long time = System.currentTimeMillis();
		return getValueAt(key, time / 1000L, (short) (time % 1000L));
	}
	@Override public Object getValueAt(String key, long epoch, short millis) {
		double time = epoch + millis / 1000.0;
		if(SINE_KEY.equals(key)) return Math.sin(time);
		if(COSINE_KEY.equals(key)) return Math.cos(time);
		if(TAN_KEY.equals(key)) return Math.tan(time);
		return null;
	}
	@Override protected void onUpdate() {
		
	}
}
