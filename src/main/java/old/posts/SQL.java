package old.posts;

import util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQL {

    public static Connection getConnection(){
        try {
            return DriverManager.getConnection("jdbc:sqlite:blogs");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void executeSQL(String sql){
        try(var con = getConnection()) {
            Log.exec(sql);
            con.createStatement().execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void initDatabase(){
        executeSQL("""
                create table if not exists blogs(
                    id int primary key,
                    title TEXT,
                    body TEXT
                )
                """);
    }

    public static String quote(String s){
        return "'" + s + "'";
    }

    public static void addBlog(BlogPost post){
        String title = quote(post.title());
        String body = quote(post.body());

        executeSQL("insert into blogs(title, body) " +
                "values (%s,%s);".formatted(title,body));
    }
}
