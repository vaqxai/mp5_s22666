import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Officer extends Soldier {

    private String rank;

    private static void cleanupDBSoldier(int database_ID) throws SQLException {

        var conn = DBInterface.connect();
        var stat = conn.prepareStatement("DELETE FROM Soldier WHERE ID = ?");
        stat.setInt(1, database_ID);
        stat.executeUpdate();
        conn.close();

    }

    private Officer(String name, String surname, String rank,  int database_ID) throws SQLException {
        super(name, surname, database_ID);

        if (rank == null) {
            throw new IllegalArgumentException("Rank cannot be null");
        }

        if (rank.isEmpty()) {
            throw new IllegalArgumentException("Rank cannot be 0-length");
        }

        this.rank = rank;
    }

    public Officer(String name, String surname, String rank) throws SQLException {
        super(name, surname);

        if (rank == null) {
            cleanupDBSoldier(GetDBID());
            throw new IllegalArgumentException("Rank cannot be null");
        }

        if (rank.isEmpty()) {
            cleanupDBSoldier(GetDBID());
            throw new IllegalArgumentException("Rank cannot be 0-length");
        }

        // let the child have the same database id as its parent (i think that will be fine)

        var conn = DBInterface.connect();
        var stat = conn.prepareStatement("INSERT INTO Officer (SoldierID, Rank) VALUES (?, ?)");
        stat.setInt(1, GetDBID());
        stat.setString(2, rank);
        stat.executeUpdate();

        conn.close();
    }

    public static Officer getByID(int ID) throws SQLException {
        var soldier = Soldier.getByID(ID);
        if (soldier == null) return null;

        var conn = DBInterface.connect();
        var stat = conn.prepareStatement("SELECT Rank FROM Officer WHERE SoldierID = ?");
        stat.setInt(1, ID);
        var res = stat.executeQuery();

        if (!res.next()) throw new SQLException("Malformed data");

        var rank = res.getString(1);

        conn.close();
        return new Officer(soldier.getName(), soldier.getSurname(), rank, ID);
    }

    public static List<Officer> get(String name, String surname, String rank) throws SQLException {
        var soldiers = Soldier.get(name, surname);

        var officers = new ArrayList<Officer>();

        for (Soldier soldier: soldiers) {
            var conn = DBInterface.connect();
            // should only be one officer per soldier
            var stat = conn.prepareStatement("SELECT * FROM Officer WHERE SoldierID = ?");
            stat.setInt(1, soldier.GetDBID());
            var res = stat.executeQuery();

            if (!res.next()) continue;

            var officer = new Officer(name, surname, rank, soldier.GetDBID());
            officers.add(officer);
            conn.close();
        }

        return officers;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String newRank) throws SQLException {
        if (rank == null) return;
        if (rank.isEmpty() || rank == newRank) return;

        var conn = DBInterface.connect();
        var stat = conn.prepareStatement("UPDATE Officer SET Rank = ? WHERE SoldierID = ?");
        stat.setString(1, newRank);
        stat.setInt(2, GetDBID());
        stat.executeUpdate();
        conn.close();

        rank = newRank;
    }

}
