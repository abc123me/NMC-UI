package test;

import java.sql.*;
import java.net.*;

public class SQLTesting {
	public static void main(String[] args) throws Exception {
		SQLConnection sqlc = null;
		String host = "localhost";
		try{ 
			sqlc = new SQLConnection(host, "test", "testdb");
		} catch(UnknownHostException ue) { 
			System.out.println("Invalid host: " + host);
			System.exit(1);
		}
		Connection conn = null;
		try { 
			conn = sqlc.connect(new String[] {
					"useUnicode=true", "useJDBCCompliantTimezoneShift=true",
					"useLegacyDatetimeCode=false", "serverTimezone=UTC"
			});
		} catch (Exception e) {
			System.out.println("Issue when connecting!");
			e.printStackTrace();
		}
		if(conn != null) System.out.println("Connected!");
		else System.exit(1);
        /*Statement stmt = conn.createStatement();
        String tableName = "tbl";
        String paramName = "*";
        String strSelect = "SELECT " + paramName + " FROM " + tableName + ";";
        System.out.println(strSelect);
        ResultSet rset = stmt.executeQuery(strSelect);
        System.out.println(rset);
        rset.close();
        stmt.close();*/
		String[] cols = sqlc.getTableColumns("tbl");
		for(String s : cols)
			System.out.println(s);
        conn.close();
	}
}
