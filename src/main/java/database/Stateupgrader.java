package database;

public class Stateupgrader {
    public void stateupgrader(String State){
        Database database = new Database();
        database.setStatus(State);
    }
}
