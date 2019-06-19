package net.net16.jeremiahlowe.nmc.libs;

import java.sql.SQLException;

import test.SQLConnection;

enum ETelemetryDateMode {
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