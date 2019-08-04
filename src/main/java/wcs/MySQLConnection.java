package main.java.wcs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnection {
	public static Connection MySQLConn;

	public static void EstablishConnection() {
		try {
			MySQLConnection.MySQLConn = DriverManager.getConnection(
					"jdbc:mysql://wcsdb.c244jcopnv7v.eu-west-3.rds.amazonaws.com:3306/wcsdb?user=admin&password=adminWCSDB");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void EndConnection() {
		try {
			MySQLConnection.MySQLConn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void ClearDB() {
		try {
			MySQLConnection.MySQLConn.createStatement().execute("DELETE FROM AisleMovements");
			MySQLConnection.MySQLConn.createStatement().execute("DELETE FROM LoadsInAisle");
			MySQLConnection.MySQLConn.createStatement().execute("DELETE FROM LoadsInSystem");
			MySQLConnection.MySQLConn.createStatement().execute("DELETE FROM StorageMovements");
			MySQLConnection.MySQLConn.createStatement().execute("DELETE FROM AisleMovementsAvg");
			MySQLConnection.MySQLConn.createStatement().execute("DELETE FROM LoadsInSystemAvg");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void ExecuteInsert(String sql) {
		try {
			MySQLConnection.MySQLConn.createStatement().execute(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}