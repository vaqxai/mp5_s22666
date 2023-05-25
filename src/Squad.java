import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Squad {

    private final int database_ID;
    private String name;
    private List<Soldier> members;

    public static boolean checkNameSoft(String name) {
        if (name == null) return false;
        if (name.isEmpty()) return false;
        if (name.length() < 3) return false;

        return true;
    }

    private static boolean checkName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }

        if (name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }

        if (name.length() < 3) {
            throw new IllegalArgumentException("Name needs to be at least 3 characters long");
        }

        return true;
    }

    public Squad(String name, List<Soldier> members) throws SQLException {
        checkName(name);

        var conn = DBInterface.connect();
        var stat_ins = conn.prepareStatement("INSERT INTO Squad (Name) VALUES (?)");
        stat_ins.setString(1, name);
        stat_ins.executeUpdate();
        var stat_sel = conn.prepareStatement("SELECT last_insert_rowid() FROM Squad");
        var res = stat_sel.executeQuery();

        if (!res.next()) {
            throw new SQLException("Malformed data");
        }

        var squad_ID = res.getInt(1);
        database_ID = squad_ID;
        this.name = name;

        for (Soldier soldier : members) {
            if (soldier.getSquad() != null) {
                throw new IllegalArgumentException("Soldier cannot be in two squads at once.");
                // TODO: Leave current squad and join this one
            }

            var stat_ins_sol = conn.prepareStatement("INSERT INTO SquadSoldier (SquadID, SoldierID) VALUES (?, ?)");
            stat_ins_sol.setInt(1, squad_ID);
            stat_ins_sol.setInt(2, soldier.GetDBID());
            stat_ins_sol.executeUpdate();
        }

        conn.close();
        this.members = members;
    }

    private Squad(String name, List<Soldier> members, int database_ID) {
        checkName(name);

        this.name = name;
        this.members = members;
        this.database_ID = database_ID;
    }

    /**
     * Given the ID, imports a squad from the database. Calling this multiple times, these squads will point to the same Database squad.
     * @param ID
     * @return
     * @throws SQLException
     */
    public static Squad getByID(int ID) throws SQLException {

        var conn = DBInterface.connect();
        var stat_sel = conn.prepareStatement("SELECT * FROM Squad WHERE ID = ?");
        stat_sel.setInt(1, ID);
        var res = stat_sel.executeQuery();

        if (!res.next()) throw new SQLException("Malformed data");

        var name = res.getString(2);

        var members = new ArrayList<Soldier>();

        var stat_sel_sol = conn.prepareStatement("SELECT * FROM SquadSoldier WHERE SquadID = ?");
        stat_sel_sol.setInt(1, ID);
        var res_sol = stat_sel_sol.executeQuery();

        var squad = new Squad(name, members, ID); // let's hope that arraylist is on heap and pass-by-ref

        while (res_sol.next()) {
            var soldier = Soldier.getByID(res_sol.getInt(2));
            members.add(soldier);
            soldier.setSquad(squad);
        }

        conn.close();
        return squad;

    }

    /**
     * Imports all squads with this name from the database. If called multiple times with the same name, these squads will point to the same Database squads.
     * @param name
     * @return
     * @throws SQLException
     */
    public static List<Squad> get(String name) throws SQLException {
        checkName(name);

        var conn = DBInterface.connect();
        var stat_sel = conn.prepareStatement("SELECT * FROM Squad WHERE Name = ?");
        stat_sel.setString(1, name);
        var res = stat_sel.executeQuery();

        var squads = new ArrayList<Squad>();

        while(res.next()) {

            var squad_ID = res.getInt(1);

            var members = new ArrayList<Soldier>();

            var stat_sel_sol = conn.prepareStatement("SELECT SoldierID FROM SquadSoldier WHERE SquadID = ?");
            stat_sel_sol.setInt(1, squad_ID);
            var res_sol = stat_sel_sol.executeQuery();

            var squad = new Squad(name, members, squad_ID); // let's hope ArrayList is a heap pass-by-ref variable

            while(res_sol.next()) {

                var soldier = Soldier.getByID(res_sol.getInt(1));
                members.add(soldier);
                soldier.setSquad(squad);
            }

            squads.add(squad);

        }

        conn.close();

        return squads;
    }

    public String GetName() {
        return name;
    }

    public void setName(String newName) throws SQLException {
        if (!checkNameSoft(newName)) return;

        var conn = DBInterface.connect();
        var stat = conn.prepareStatement("SELECT * FROM Squad WHERE ID = ?");
        stat.setInt(1, database_ID);
        var res = stat.executeQuery();

        if (!res.next()) {
            throw new SQLException("Malformed data");
        }

        stat = conn.prepareStatement("UPDATE Squad SET Name = ? WHERE ID = ?");
        stat.setInt(2, database_ID);
        stat.setString(1, newName);
        stat.executeUpdate();
        conn.close();

        this.name = newName;
    }

    public void addSoldier(Soldier soldier) throws SQLException {
        if (soldier == null) return;
        if (members.contains(soldier)) return;

        System.out.println("Test");

        var conn = DBInterface.connect();
        var stat = conn.prepareStatement("INSERT INTO SquadSoldier (SoldierID, SquadID) VALUES (?, ?)");
        stat.setInt(1, soldier.GetDBID());
        stat.setInt(2, database_ID);
        stat.executeUpdate();

        members.add(soldier);
        soldier.setSquad(this);
        conn.close();
    }

    public void removeSoldier(Soldier soldier) throws SQLException {
        if (soldier == null) return;
        if (!members.contains(soldier)) return;

        var conn = DBInterface.connect();
        // SquadID is not really necessary since Squad:Soldier is 1:*, but anyway
        var stat = conn.prepareStatement("DELETE FROM SquadSoldier WHERE SoldierID = ? AND SquadID = ?");
        stat.setInt(1, soldier.GetDBID());
        stat.setInt(2, database_ID);
        stat.executeUpdate();

        members.remove(soldier);
        soldier.setSquad(null);
        conn.close();
    }

    public List<Soldier> GetMembers() {
        return members;
    }

    public int GetDBID() {
        return database_ID;
    }

    @Override
    public boolean equals(Object obj){
        if (obj == null) {
            return false;
        }

        if (obj.getClass() != this.getClass()) {
            return false;
        }

        final Squad other = (Squad) obj;
        if (this.database_ID != other.GetDBID()) return false;

        return true;

    }
}
