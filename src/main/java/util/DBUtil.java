package util;

import crud.Blog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBUtil {

    private static Connection connection;

    static {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:server");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void executeQuery(String sql) throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute(sql);
        Log.exec(sql);
    }

    private static ResultSet executeSelect(String sql) throws SQLException {
        Statement statement = connection.createStatement();
        Log.exec(sql);
        return statement.executeQuery(sql);
    }

    public static void innitBlogTable(){
        try {
            executeQuery("""
                CREATE TABLE IF NOT EXISTS blogs(
                    id NUMBER PRIMARY KEY,
                    name TEXT NOT NULL,
                    category TEXT NOT NULL,
                    excerpt TEXT NOT NULL,
                    content TEXT NOT NULL
                );
                """);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addBlog(Blog blog){
        try{
            executeQuery(String.format(
                    """
                        insert into blogs(name, category, excerpt, content)
                        values ('%s', '%s', '%s', '%s');
                    """,
                    blog.title(), blog.tag(), blog.excerpt(), blog.content()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Blog> getBlogList(){
        List<Blog> blogList = new ArrayList<>();

        try {
            ResultSet resultSet = executeSelect("""
                    select * from blogs
                    """);

            while (resultSet.next()) {
                String title = resultSet.getString("name");
                String category = resultSet.getString("category");
                String excerpt = resultSet.getString("excerpt");
                String content = resultSet.getString("content");

                blogList.add(new Blog(title, category, excerpt, content));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return blogList;

    }


}
