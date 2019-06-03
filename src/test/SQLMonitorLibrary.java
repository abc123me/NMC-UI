package test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;

import net.net16.jeremiahlowe.nmc.NMCInstance;
import net.net16.jeremiahlowe.nmc.lib.NMCLibrary;
import net.net16.jeremiahlowe.nmc.lib.NMCLibraryInfo;
import net.net16.jeremiahlowe.nmc.lib.telemetry.TelemetryMonitoringLibrary;
import net.net16.jeremiahlowe.shared.Duplet;

public class SQLMonitorLibrary extends TelemetryMonitoringLibrary{
	public final SQLConnection conn;
	public final String timeColumn, tableName;
	
	public EDateMode roundingMode = EDateMode.ClosestOverall;
	
	public static void main(String[] args) throws Exception{
		NMCInstance ins = NMCInstance.makeGUI(800, 600);
		SQLConnection conn = new SQLConnection("localhost", "test", "testdb");
		conn.connect(new String[] {
			"useUnicode=true", "useJDBCCompliantTimezoneShift=true",
			"useLegacyDatetimeCode=false", "serverTimezone=UTC"
		});
		SQLMonitorLibrary lib = new SQLMonitorLibrary(ins, conn, "TestSQL", "tbl", "DateTime", 500);
		NMCLibraryInfo info = new NMCLibraryInfo(SQLMonitorLibrary.class.getName());
		ins.loadLibrary(new Duplet<NMCLibrary, NMCLibraryInfo>(lib, info));
		lib.startUpdater();
	}
	
	public SQLMonitorLibrary(NMCInstance instance, SQLConnection conn, String libraryName, String tableName, String timeColumnNume, int updateRate) throws Exception {
		super(instance, libraryName, updateRate);
		this.timeColumn = SQLConnection.safetyCheck(timeColumnNume);
		this.tableName = SQLConnection.safetyCheck(tableName);
		this.conn = conn;
	}
	
	@Override public String[] getTelemetryKeys() {
		try{
			return conn.getTableColumns("tbl");
		} catch (SQLException sqle) {
			return null;
		}
	}

	private HashMap<String, Object> prepared = new HashMap<String, Object>();
	@Override public boolean prepareValuesAt(String[] keys, Date when) {
		try {
			long epoch = when == null ? System.currentTimeMillis() : when.getTime();
			String query = roundingMode.generateQuery(keys, tableName, timeColumn, epoch);
			ResultSet r = conn.query(query + " limit 1");
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
				ResultSet r = conn.query(query + " limit 1");
				if(r.next()) out = r.getObject(1);
				r.close();
			} catch (SQLException e) {
				return null;
			}
		} else out = this.prepared.get(key);
		return out;
	}
	@Override protected void onUpdate() {}
}
enum EDateMode {
	ClosestPast, ClosestFuture, ClosestOverall;
	
	public String generateQuery(String[] keys, String tableName, String timeColumn, long epoch) throws SQLException {
		String key_str = "";
		// This must be done this way because the server does not have a known timezone!
		String date_str = "DATE_ADD(FROM_UNIXTIME(%d), INTERVAL %d MICROSECOND)";
		date_str = String.format(date_str, epoch / 1000, (epoch % 1000) * 1000);
		if(keys == null || keys.length <= 0) key_str = "*";
		else {
			for(String key : keys)
				key_str += SQLConnection.safetyCheck(key.trim()) + ",";
			key_str = key_str.substring(0, key_str.length() - 1);
		}
		String selectStmt = String.format("select %s from %s", key_str, tableName);
		String out = null;
		switch(this) {
			case ClosestOverall: //select KEYS from TBL order by ABS(TIMESTAMPDIFF(PRECISION, TCOL, DATE)) asc
				out = "%s order by ABS(TIMESTAMPDIFF(MICROSECOND, %s, %s)) asc";
				out = String.format(out, selectStmt, timeColumn, date_str);
				return out;
			case ClosestFuture: //select KEYS from TBL where TCOL >= DATE order by TCOL asc;
				out = "%s where %s >= %s order by %s asc";
				out = String.format(out, selectStmt, timeColumn, date_str, timeColumn);
				return out;
			case ClosestPast: //select KEYS from TBL where TCOL <= DATE order by TCOL desc;
				out = "%s where %s <= %s order by %s desc";
				out = String.format(out, selectStmt, timeColumn, date_str, timeColumn);
				return out;
			default: break;
		} throw new RuntimeException("WTF?");
	}
	public String generateQuery(String key, String tableName, String timeColumn, long epoch) throws SQLException {
		return generateQuery(new String[] { key }, tableName, timeColumn, epoch);
	}
}
