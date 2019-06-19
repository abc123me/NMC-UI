package net.net16.jeremiahlowe.nmc.libs;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;

import net.net16.jeremiahlowe.nmc.NMCInstance;
import net.net16.jeremiahlowe.nmc.libs.telemetry.TelemetryMonitoringLibrary;
import test.SQLConnection;

public abstract class BaseSQLTelemetryLibrary extends TelemetryMonitoringLibrary{
	public final String timeColumn, tableName;
	
	public ETelemetryDateMode roundingMode = ETelemetryDateMode.ClosestOverall;
	
	public BaseSQLTelemetryLibrary(NMCInstance instance, String libraryName, String tableName, String timeColumnNume, int updateRate) throws Exception {
		super(instance, libraryName, updateRate);
		this.timeColumn = SQLConnection.safetyCheck(timeColumnNume);
		this.tableName = SQLConnection.safetyCheck(tableName);
	}
	
	protected abstract SQLConnection getConnection();

	@Override public String[] getTelemetryKeys() {
		try{
			return getConnection().getTableColumns("tbl");
		} catch (SQLException sqle) {
			return null;
		}
	}

	private HashMap<String, Object> prepared = new HashMap<String, Object>();
	@Override public boolean prepareValuesAt(String[] keys, Date when) {
		try {
			long epoch = when == null ? System.currentTimeMillis() : when.getTime();
			String query = roundingMode.generateQuery(keys, tableName, timeColumn, epoch);
			System.out.println(query);
			ResultSet r = getConnection().query(query + " limit 1");
			if(r.next()) {
				for(int i = 0; i < keys.length; i++) {
					String key = keys[i];
					Object o = r.getObject(key);
					prepared.put(key, o);
				}
			}
			r.close();
			return true;
		} catch (SQLException e) {
			System.err.println(e);
			e.printStackTrace(System.err);
		}
		return false;
	}
	@Override public Object getValueAt(String key, Date time, boolean prepared) {
		Object out = null;
		if(!prepared) {
			try{
				long epoch = time == null ? System.currentTimeMillis() : time.getTime();
				String query = roundingMode.generateQuery(key, tableName, timeColumn, epoch);
				ResultSet r = getConnection().query(query + " limit 1");
				if(r.next()) out = r.getObject(1);
				r.close();
			} catch (SQLException e) {
				return null;
			}
		} else out = this.prepared.get(key);
		return out;
	}
}
