package net.net16.jeremiahlowe.nmc.lib.telemetry;

import java.util.Date;

/**
 * The ITelemetrySource is an interface that specifies that a class is a source
 * of valid telemetry data, noted implications of this are in requesting SQL data
 * from a SQL server or requesting specific data from a network socket
 * 
 * @author Jeremiah Lowe
 */
public interface ITelemetrySource {
	/**
	 * Returns the keys that this telemetry source has available
	 */
	public String[] getTelemetryKeys();
	/**
	 * Called when a specific value at a specific time is needed, if a group of values are
	 * needed make sure to call {@code prepareValuesAt} first, this is heavily recommended for
	 * optimization reasons!
	 * <br>
	 * @param key The keys that will be requested
	 * @param when For what date the keys are requested
	 * @param prep Whether or not the data was prepared for in advance
	 * @return The object referenced by key at the specified date, or null if non-existant
	 */
	public Object getValueAt(String key, Date when, boolean prep);
	/**
	 * Called before the keys are accessed, especially used for cases when reading data in
	 * chunks is faster then individually, eg. polling an SQL server
	 * <br>
	 * This method is not required but heavily suggested due to optimization reasons!
	 * <br>
	 * @param keys The keys that will be polled
	 * @param when What date is requested, of null then the current date is used
	 * @return Whether or not it sucessfully prepared the data
	 */
	public default boolean prepareValuesAt(String[] keys, Date when) { return false; }
}
