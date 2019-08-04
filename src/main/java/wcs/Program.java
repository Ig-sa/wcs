package main.java.wcs;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;

public class Program {
	public class Load {
		public int Id;
		public LinkedList<Integer> Destinations = new LinkedList<>();
		public long TimeOfLastAction;
		public int CurrentlyInAisle = 0;

		public Load(int id) {
			this.Id = id;
			this.TimeOfLastAction = System.currentTimeMillis();
		}

		public void DoNext() {
			if (CurrentlyInAisle > 0)
				ExitAisle();
			else if (Destinations.size() > 0)
				EnterNextAisle();
			else
				EnterStorage();
		}

		public void ExitStorage() {
			int storageSource = Rand.nextInt(2) + 1;
			loadsInSystem.add(this);
			this.TimeOfLastAction = System.currentTimeMillis();

			// Report to DB
			MySQLConnection.ExecuteInsert("INSERT INTO LoadsInSystem VALUES (" + this.Id + ")");
			MySQLConnection.ExecuteInsert("INSERT INTO StorageMovements (StorageId, Type, LoadId) VALUES ("
					+ storageSource + ", 1, " + this.Id + ")");
			System.out.println("<= (S) Load " + this.Id + " exiting Storage " + storageSource);
		}

		public void EnterStorage() {
			int storageDest = Rand.nextInt(2) + 1;
			loadsInSystem.remove(this);
			this.TimeOfLastAction = System.currentTimeMillis();

			// Report to DB
			MySQLConnection.ExecuteInsert("DELETE FROM LoadsInSystem WHERE LoadId = " + this.Id);
			MySQLConnection.ExecuteInsert("INSERT INTO StorageMovements (StorageId, Type, LoadId) VALUES ("
					+ storageDest + ", 0, " + this.Id + ")");
			System.out.println("=> (S) Load " + this.Id + " entering Storage " + storageDest);
		}

		public void EnterNextAisle() {
			if (this.CurrentlyInAisle > 0) {
				ExitAisle();
				return;
			}

			int destination = this.Destinations.pop();

			// Report movement to DB
			MySQLConnection.ExecuteInsert("INSERT INTO LoadsInAisle VALUES (" + this.Id + ", " + destination + ")");
			MySQLConnection.ExecuteInsert("INSERT INTO AisleMovements (LoadId, AisleId, Type) VALUES (" + this.Id + ", "
					+ destination + ", " + 0 + ")");
			System.out.println("=> (A) Load " + this.Id + " entering in Aisle " + destination);

			CurrentlyInAisle = destination;

			loadsInAisle[destination - 1].add(this);
			this.TimeOfLastAction = System.currentTimeMillis();

			LoadsInAisle[destination - 1]++;
			LoadsCurrentInAisle[destination - 1]++;
		}

		public void ExitAisle() {
			// Report movement to DB
			MySQLConnection.ExecuteInsert("DELETE FROM LoadsInAisle WHERE LoadId = " + this.Id);
			MySQLConnection.ExecuteInsert("INSERT INTO AisleMovements (LoadId, AisleId, Type) VALUES (" + this.Id + ", "
					+ CurrentlyInAisle + ", " + 1 + ")");
			System.out.println("<= (A) Load " + this.Id + " exiting Aisle " + CurrentlyInAisle);

			loadsInAisle[CurrentlyInAisle - 1].remove(this);
			this.TimeOfLastAction = System.currentTimeMillis();

			LoadsOutAisle[CurrentlyInAisle - 1]++;
			LoadsCurrentInAisle[CurrentlyInAisle - 1]--;

			CurrentlyInAisle = 0;
		}
	}

	public static int[] LoadsInAisle = new int[4];
	public static int[] LoadsOutAisle = new int[4];
	public static int[] LoadsCurrentInAisle = new int[4];
	public static long TimeSinceLastPrint = System.currentTimeMillis();

	int MAX_LOADS_IN_SYSTEM = 20;
	long GENERATION_TIME = 14400;// Long.MAX_VALUE;
	int MILLIS_BETWEEN_ACTIONS = 15000;
	int INFINITE_LOOP_WAIT_LAPSE = 1000;
	int LOAD_MAX_NUM_DESTINATIONS = 3;

	Random Rand = new Random();

	ArrayList<Load> loadsInSystem = new ArrayList<Load>();
	ArrayList<Load>[] loadsInAisle = new ArrayList[] { new ArrayList<Load>(), new ArrayList<Load>(),
			new ArrayList<Load>(), new ArrayList<Load>() };

	void run() {
		System.out.println("PROGRAM START");

		long infiniteLoop = GENERATION_TIME;
		while (infiniteLoop > 0) {
			try {
				if (loadsInSystem.size() < MAX_LOADS_IN_SYSTEM)
					if (Rand.nextInt(5) == 0)
						GenerateLoad();

				if (loadsInSystem.size() > 0)
					for (Load load : (ArrayList<Load>) loadsInSystem.clone())
						if ((System.currentTimeMillis() - load.TimeOfLastAction) > MILLIS_BETWEEN_ACTIONS)
							load.DoNext();

				Date currentTime = new Date();
				if ((currentTime.getMinutes() % 5 == 0) && (currentTime.getSeconds() > 5)
						&& ((System.currentTimeMillis() - TimeSinceLastPrint) > 180000))
					PrintStatisticData();

				Thread.sleep(INFINITE_LOOP_WAIT_LAPSE);
				infiniteLoop--;
			} catch (Exception e) {
				System.out.println(e);
			}
		}

		System.out.println("PROGRAM END");
	}

	int NextLoadId = 0;

	void GenerateLoad() {
		NextLoadId++;

		Load newLoad = new Load(NextLoadId);

		int[] goToAisle = new int[4];
		for (int i = 0; i < LOAD_MAX_NUM_DESTINATIONS; i++)
			goToAisle[Rand.nextInt(4)] = 1;

		for (int i = 0; i < 4; i++)
			if (goToAisle[i] == 1)
				newLoad.Destinations.add(i + 1);

		System.out.println("(++) Created Load " + newLoad.Id);
		newLoad.ExitStorage();
	}

	void PrintStatisticData() {
		MySQLConnection
				.ExecuteInsert("INSERT INTO LoadsInSystemAvg (LoadsInSystem) VALUES (" + loadsInSystem.size() + ")");

		for (int i = 0; i < 4; i++) {
			MySQLConnection.ExecuteInsert("INSERT INTO AisleMovementsAvg (LoadsCount, AisleId, Type) VALUES ("
					+ LoadsInAisle[i] + ", " + i + ", 0)");
			MySQLConnection.ExecuteInsert("INSERT INTO AisleMovementsAvg (LoadsCount, AisleId, Type) VALUES ("
					+ LoadsOutAisle[i] + ", " + i + ", 1)");
			MySQLConnection.ExecuteInsert("INSERT INTO AisleMovementsAvg (LoadsCount, AisleId, Type) VALUES ("
					+ LoadsCurrentInAisle[i] + ", " + i + ", 2)");

			LoadsInAisle[i] = 0;
			LoadsOutAisle[i] = 0;
		}

		TimeSinceLastPrint = System.currentTimeMillis();
	}
}