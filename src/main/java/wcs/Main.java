package main.java.wcs;

public class Main {
	public static void main(String args[]) {
		MySQLConnection.EstablishConnection();
		MySQLConnection.ClearDB();

		(new Program()).run();

		MySQLConnection.EndConnection();
	}
}

