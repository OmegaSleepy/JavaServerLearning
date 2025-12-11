import sql.Log;
import sql.Query;
import sql.Settings;

public class smth {

    public static void main (String[] args) {

        Log.info("Hello World");
        Settings.logQueries = false;
        Query.getResult("create table if not exists users(" +
                "id integer primary key autoincrement," +
                "name varchar(20)," +
                "familyName varchar(20)," +
                "stars integer)");
        
        Query.getResult("select * from users");

    }

}
