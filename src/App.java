import java.util.ArrayList;
import java.util.List;

public class App {
    public static void main(String[] args) throws Exception {

        // Database setup

        DBInterface.SetupTables();

        // End database setup

            // Klasa

            List<Soldier> jan_kowalskis = Soldier.get("Jan", "Kowalski");
            Soldier jan_kowalski = null;
            if (jan_kowalskis.size() == 0) {
                jan_kowalski = new Soldier("Jan", "Kowalski");
            } else {
                jan_kowalski = jan_kowalskis.get(0);
            }

        List<Soldier> adam_kowalskis = Soldier.get("Adam", "Kowalski");
        Soldier adam_kowalski = null;
        if (adam_kowalskis.size() == 0) {
            adam_kowalski = new Soldier("Adam", "Kowalski");
        } else {
            adam_kowalski = adam_kowalskis.get(0);
        }

        List<Soldier> adam_nowaks = Soldier.get("Adam", "Nowak");
        Soldier adam_nowak = null;
        if (adam_nowaks.size() == 0) {
            adam_nowak = new Soldier("Adam", "Nowak");
        } else {
            adam_nowak = adam_nowaks.get(0);
        }

        System.out.println(jan_kowalski.GetDBID());
        System.out.println(adam_kowalski.GetDBID());
        System.out.println(adam_nowak.GetDBID());

        // Asocjacja (1:*)

        List<Squad> alphas = Squad.get("Alpha");
        Squad alpha = null;
        if (alphas.size() == 0) {
            var members = new ArrayList<Soldier>();
            members.add(adam_kowalski);
            members.add(adam_nowak);
            alpha = new Squad("Alpha", members);
        } else {
            alpha = alphas.get(0);
        }

        System.out.println("Squad: " + alpha.GetDBID());
        for (Soldier soldier : alpha.GetMembers()) {
            System.out.println(soldier.GetDBID());
        }

        // Dziedziczenie

        List<Officer> officers = Officer.get("Janusz", "Nowak", "Kapitan");
        Officer officer = null;
        if (officers.size() == 0) {
            officer = new Officer("Janusz", "Nowak", "Kapitan");
        } else {
            officer = officers.get(0);
        }

        System.out.println(officer.GetDBID());

        // ?

        System.out.println("Pre create soldier");
        Soldier s = new Soldier("xxx", "DDDD");

        System.out.println("Soldier create over");
        List<Squad> alpha_s = Squad.get("Alpha");
        System.out.println("Squad get");
        Squad alpha2 = alpha_s.get(0);

        alpha2.addSoldier(s);

    }
}
