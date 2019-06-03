package net.net16.jeremiahlowe.nmc.ui;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.logging.*;

import net.net16.jeremiahlowe.shared.ansi.AnsiTextPane;

public class LoggingPane extends AnsiTextPane implements ComponentListener{
	private final Logger logger;
	private final Handler handler;
	
	private boolean clearOnFlush;
	
	public LoggingPane(Logger logger) {
		if(logger == null)
			throw new NullPointerException("LoggingPane needs a logger!");
		this.logger = logger;
		this.handler = new Handler() {
			@Override public void publish(LogRecord record) {
				String msg = record.getMessage();
				Level lvl = record.getLevel();
				int color = 0;
				if(lvl == Level.SEVERE) color = 31;
				else if(lvl == Level.WARNING) color = 33;
				else if(lvl == Level.CONFIG) color = 32;
				else color = 30;
				String prefix = AnsiTextPane.ANSI_ESCAPE + color + "m[" + record.getLevel() + "]";
				String suffix = AnsiTextPane.ANSI_CLEAR + System.lineSeparator();
				append(String.format("%s: %s%s", prefix, msg, suffix));
			}
			@Override public void flush() { if(clearOnFlush) setText(""); }
			@Override public void close() throws SecurityException {}
		};
		this.logger.addHandler(handler);
	}
	
	public void detach() {
		this.logger.removeHandler(handler);
	}
	@Override protected void finalize() throws Throwable {
		detach();
		super.finalize();
	}

	@Override public void componentHidden(ComponentEvent e) { }
	@Override public void componentMoved(ComponentEvent e) { }
	@Override public void componentResized(ComponentEvent e) { }
	@Override public void componentShown(ComponentEvent e) { }
}
