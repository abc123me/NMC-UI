package net.net16.jeremiahlowe.nmc.lib;

import java.io.PrintStream;
import java.io.PrintWriter;

public class LibraryException extends Exception {
	public final NMCLibrary from;
	public final Exception cause;
	public final String cause_str;
	
	public LibraryException(Exception cause) {
		this(null, cause);
	}
	public LibraryException(String cause) {
		this(null, cause);
	}
	public LibraryException(NMCLibrary from, Exception cause) {
		this.from = from;
		this.cause = cause;
		this.cause_str = null;
	}
	public LibraryException(NMCLibrary from, String cause_str) {
		this.from = from;
		this.cause = null;
		this.cause_str = cause_str;
	}
	
	public String toString() {
		String c = "unknown";
		if(cause != null)
			c = cause.toString();
		if(cause_str != null)
			c = cause_str;
		String f = "unknown";
		if(from != null)
			f = from.getDisplayName();
		return "LibraryException from library: " + f + " caused by: " + c;  
	}
	
	@Override
	public String getMessage() {
		if(cause_str != null) return cause_str;
		return cause.getMessage();
	}
	@Override
	public String getLocalizedMessage() {
		if(cause_str != null) return cause_str;
		return cause.getLocalizedMessage();
	}
	@Override
	public synchronized Exception getCause() {
		if(cause != null)
			return cause;
		else if(cause_str != null)
			return new Exception(cause_str);
		else
			return null;
	}
	@Override
	public StackTraceElement[] getStackTrace() {
		if(cause != null)
			return cause.getStackTrace();
		else
			return super.getStackTrace();
	}
	@Override
	public void printStackTrace(PrintStream to) {
		if(cause != null)
			cause.printStackTrace(to);
		else
			super.printStackTrace(to);
	}
	@Override
	public void printStackTrace(PrintWriter to) {
		printStackTrace(to);
	}
	@Override
	public void printStackTrace() {
		printStackTrace(System.out);
	}
}
