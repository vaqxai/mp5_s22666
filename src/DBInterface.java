import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBInterface {

    /**
     * Opens a new SQLite database connection, please remember to close it.
     * @return
     */
    public static Connection connect() {

        Connection conn = null;

        try {
            String url = "jdbc:sqlite:./database.db";
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return conn;

    }

    public static void SetupTables() throws SQLException {
        var conn = connect();
        var stat = conn.prepareStatement("""
               CREATE TABLE IF NOT EXISTS Soldier (
                ID INTEGER PRIMARY KEY AUTOINCREMENT,
                Name TEXT,
                Surname TEXT
               )
        """);
        stat.executeUpdate();

        stat = conn.prepareStatement("""
               CREATE TABLE IF NOT EXISTS Squad (
                ID INTEGER PRIMARY KEY AUTOINCREMENT,
                Name TEXT
               )
        """);
        stat.executeUpdate();

        stat = conn.prepareStatement("""
               CREATE TABLE IF NOT EXISTS SquadSoldier (
                ID INTEGER PRIMARY KEY AUTOINCREMENT,
                SquadID INTEGER,
                SoldierID INTEGER,
                FOREIGN KEY(SquadID) REFERENCES Squad(ID),
                FOREIGN KEY(SoldierID) REFERENCES Soldier(ID)
               )
        """);
        stat.executeUpdate();

        stat = conn.prepareStatement("""
               CREATE TABLE IF NOT EXISTS Officer (
                SoldierID INTEGER PRIMARY KEY,
                Rank TEXT,
                FOREIGN KEY(SoldierID) REFERENCES Soldier(ID)
               )
        """);
        stat.executeUpdate();

        conn.close();
    }
}
