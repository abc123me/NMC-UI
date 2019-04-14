package test;

import java.net.*;
import java.sql.*;
import java.util.ArrayList;

public class SQLConnection {
	public static final String PARAM_REGEXP = "^\\w+=\\w+$";
	public static final String SQL_SAFETY_REGEXP = "^(\\w|\\s|\\d)+$";
	public static final int DEFAULT_SQL_PORT = 3306;
	
	public final InetAddress ip;
	public final int port;
	public final String databaseName;
	public final String username, password;
	
	private Connection conn;
	
	public SQLConnection(String ip, String user) throws UnknownHostException { this(InetAddress.getByName(ip), DEFAULT_SQL_PORT, user, null); }
	public SQLConnection(String ip, int port, String user) throws UnknownHostException { this(InetAddress.getByName(ip), port, user, null); }
	public SQLConnection(String ip, String user, String db) throws UnknownHostException { this(InetAddress.getByName(ip), DEFAULT_SQL_PORT, user, db); }
	public SQLConnection(String ip, int port, String user, String db) throws UnknownHostException { this(InetAddress.getByName(ip), port, user, db); }
	public SQLConnection(InetAddress ip, String user) { this(ip, DEFAULT_SQL_PORT, user, null); }
	public SQLConnection(InetAddress ip, int port, String user) { this(ip, port, user, null, null); }
	public SQLConnection(InetAddress ip, String user, String db) { this(ip, DEFAULT_SQL_PORT, user, db); }
	public SQLConnection(InetAddress ip, int port, String user, String db) { this(ip, port, user, null, db); }
	
	public SQLConnection(InetAddress ip, int port, String username, String password, String databaseName) {
		this.ip = ip; this.port = port;
		this.databaseName = databaseName;
		this.username = username;
		this.password = password;
	}
	public Connection connect() throws Exception {
		return connect(null);
	}
	public Connection connect(String[] params) throws SQLException {
		String pstr = "";
		if(params != null) {
			for(String p : params) {
				if(p.matches(PARAM_REGEXP)) pstr += p + "&";
				else throw new RuntimeException("Parameter don't match regexp: " + PARAM_REGEXP);
			}
		}
		if(pstr.length() > 0) pstr = "?" + pstr.substring(0, pstr.length() - 1); //Cutoff the ending &, and add a ? to the beginning
		String host = ip.getHostAddress() + ":" + port;
		String url = "jdbc:mysql://" + host + "/" + (databaseName == null ? "" : databaseName) + pstr;
		System.out.println("Connecting to URL: " + url);
		conn = DriverManager.getConnection(url, username, password);
		return conn;
	}
	
	private static final void safetyCheck(String sql) throws SQLException{
		if(sql == null)
			throw new NullPointerException("SQL given is null");
		if(!sql.matches(SQL_SAFETY_REGEXP)) 
			throw new SQLException("If I had a dollar for every SQL injection i've seen, a black hole would form from the gravitational attraction between the dollars");
		
	}
	public String[] getTableColumns(String table) throws SQLException {
		safetyCheck(table);
		Statement stmt = conn.createStatement();
		String query = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME=\'" + table + "\'";
		ResultSet res = stmt.executeQuery(query);
		ArrayList<String> outlst = new ArrayList<String>();
		while(res.next()) outlst.add(res.getString("COLUMN_NAME"));
		stmt.close();
		res.close();
		return outlst.toArray(new String[0]);
	}
	public String[] getTableRow(String table, int row, String idColumnName, String idColumnValue) throws SQLException {
		safetyCheck(table); safetyCheck(idColumnName); safetyCheck(idColumnValue);
		throw new RuntimeException("Not yet implemented!"); //TODO: Implement this?
	}
}
