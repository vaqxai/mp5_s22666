import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Soldier {

    private final int database_ID;
    private String name;
    private String surname;
    private Squad squad;

    private static boolean checkNameSoft(String name) {
        if (name == null) return false;
        if (name.isEmpty()) return false;
        if (name.length() < 3) return false;

        return true;
    }

    private static boolean checkNamesSoft(String name, String surname) {
        return checkNameSoft(name) && checkNameSoft(surname);
    }


    private static boolean checkNames(String name, String surname) {
        if (name == null || surname == null) {
            throw new IllegalArgumentException("Name and surname cannot be null");
        }

        if (name.isEmpty() || surname.isEmpty()) {
            throw new IllegalArgumentException("Name and surname cannot be empty");
        }

        if (name.length() < 3 || surname.length() < 3) {
            throw new IllegalArgumentException("Name and surname need to be at least 3 characters long");
        }

        return true;
    }

    /**
     * Creates new soldier, does NOT put him in the database!
     * @param name
     * @param surname
     * @param database_ID
     * @throws SQLException
     */
    protected Soldier(String name, String surname, int database_ID) throws SQLException {
        checkNames(name, surname);

        this.name = name;
        this.surname = surname;
        this.database_ID = database_ID;
    }

    /**
     * Creates a new database soldier and returns his ID
     * @param name
     * @param surname
     * @return
     * @throws SQLException
     */
    private static int CreateSoldierDB(String name, String surname) throws SQLException {
        var conn = DBInterface.connect();
        var stat_ins = conn.prepareStatement("INSERT INTO Soldier (Name, Surname) VALUES (?, ?)");
        stat_ins.setString(1, name);
        stat_ins.setString(2, surname);
        stat_ins.executeUpdate();

        var res = conn.prepareStatement("SELECT last_insert_rowid() FROM Soldier").executeQuery();
        res.next();
        var ID = res.getInt(1);
        conn.close();
        return ID;
    }

    /**
     * Creates new soldier and places him in the database (always inserts a new soldier into the database!!)
     * @param name
     * @param surname
     * @throws SQLException
     */
    public Soldier(String name, String surname) throws SQLException {
        checkNames(name, surname);

        this.name = name;
        this.surname = surname;

        this.database_ID = CreateSoldierDB(name, surname);
    }

    /**
     * Imports a soldier from the database. If called multiple times with the same ID these soldiers will point to the same Database soldier.
     * @param ID
     * @return
     * @throws SQLException
     */
    public static Soldier getByID(int ID) throws SQLException {

        var conn = DBInterface.connect();
        var stat_sel = conn.prepareStatement("SELECT * FROM Soldier WHERE ID = ?");
        stat_sel.setInt(1, ID);
        var res = stat_sel.executeQuery();

        if (!res.next()) return null;

        var soldier = new Soldier(res.getString(2), res.getString(3), res.getInt(1));
        conn.close();

        return soldier;
    }

    /**
     * Imports one/many database soldiers. If called multiple times with the same name & surname, these soldiers will point to the same Database soldier.
     * @param name
     * @param surname
     * @return
     * @throws SQLException
     */
    public static List<Soldier> get(String name, String surname) throws SQLException {
        checkNames(name, surname);

        var conn = DBInterface.connect();
        var stat_sel = conn.prepareStatement("SELECT * FROM Soldier WHERE Name = ? AND Surname = ?");
        stat_sel.setString(1, name);
        stat_sel.setString(2, surname);
        var res = stat_sel.executeQuery();

        var list = new ArrayList<Soldier>();

        while(res.next()) {

            var database_ID = res.getInt(1);

            if (checkNamesSoft(name, surname)) {
                var soldier = new Soldier(name, surname, database_ID);
                list.add(soldier);
            }
        }

        conn.close();
        return list;
    }

    public int GetDBID() {
        return database_ID;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    /**
     * This function does NOT affect the database. Use the functions in the Squad class instead.
     * INTERNAL USE ONLY >:X
     * @param newSquad
     */
    void setSquad(Squad newSquad) throws SQLException {
        if (squad != null && !squad.GetMembers().contains(this)) return;

        // We COULD be doing a verif. of the database's state here BUT
        // It's easier to just tell the user "Don't do it" and restrict visiblity to the package...
        // Also it's 1:57 AM and I'm not about to implement that verif. on this side

        // Edit: 2:01AM I am implementing the database state verification...
        var conn = DBInterface.connect();
        var stat = conn.prepareStatement("SELECT SquadID FROM SquadSoldier WHERE SoldierID = ?");
        stat.setInt(1, database_ID);
        var res = stat.executeQuery();

        if (!res.next()) throw new RuntimeException("I told you 'Use the functions in the Squad class instead.'");
        // Somebody will complain about the association only being editable from one side so I AM gonna add manipulation on this side
        var res_squ_id = res.getInt(1);
        if (res_squ_id != newSquad.GetDBID()) {
            conn.close();
            return;
        } // this function is only for estabilshing links I N T E R N A L L Y
        conn.close();

        this.squad = newSquad;
    }

    // \/ \/ \/ HERE'S YER PUBLIC API

    /**
     * Makes this soldier join the given squad (and leave the current one if need be)
     * @param toJoin
     */
    public void joinSquad(Squad newSquad) throws SQLException {

        if (squad != null) leaveSquad();

        newSquad.addSoldier(this);
    }

    public void leaveSquad() throws SQLException {
        this.getSquad().removeSoldier(this);
    }

    public Squad getSquad() {
        return squad;
    }

    public void setName(String newName) throws SQLException {
        if (!checkNameSoft(newName)) return;

        var conn = DBInterface.connect();
        var stat = conn.prepareStatement("SELECT * FROM Soldier WHERE ID = ?");
        stat.setInt(1, database_ID);
        var res = stat.executeQuery();

        if (!res.next()) {
            throw new SQLException("Malformed data");
        }

        stat = conn.prepareStatement("UPDATE Soldier SET Name = ? WHERE ID = ?");
        stat.setInt(2, database_ID);
        stat.setString(1, newName);
        stat.executeUpdate();
        conn.close();

        this.name = newName;
    }

    public void setSurname(String newName) throws SQLException {
        if (!checkNameSoft(newName)) return;

        var conn = DBInterface.connect();
        var stat = conn.prepareStatement("SELECT * FROM Soldier WHERE ID = ?");
        stat.setInt(1, database_ID);
        var res = stat.executeQuery();

        if (!res.next()) {
            throw new SQLException("Malformed data");
        }

        stat = conn.prepareStatement("UPDATE Soldier SET Surname = ? WHERE ID = ?");
        stat.setInt(2, database_ID);
        stat.setString(1, newName);
        stat.executeUpdate();
        conn.close();

        this.name = newName;
    }

    @Override
    public boolean equals(Object obj){
        if (obj == null) {
            return false;
        }

        if (obj.getClass() != this.getClass()) {
            return false;
        }

        final Soldier other = (Soldier) obj;
        if (this.database_ID != other.GetDBID()) return false;

        return true;

    }

}
