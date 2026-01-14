package util;

import crud.Blog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBUtil {

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:server?busy_timeout=5000");
    }

    public static void innitBlogTable(){
        try (Connection con = getConnection(); Statement st = con.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS blogs(
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    tag TEXT NOT NULL,
                    excerpt TEXT NOT NULL,
                    content TEXT NOT NULL
                );
                """);
            st.execute("PRAGMA journal_mode=WAL;");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void executeQuery(String sql) throws SQLException {
        try(Connection con = getConnection()){
            Log.exec(sql);
            Statement statement = con.createStatement();
            statement.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static ResultSet executeSelect(String sql) {
        try(Connection con = getConnection()){
            Log.exec(sql);
            Statement statement = con.createStatement();
            return statement.executeQuery(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static void addBlog(Blog blog){
        String sql = "INSERT INTO blogs (title, tag, excerpt, content) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, blog.title());
            pstmt.setString(2, blog.tag());
            pstmt.setString(3, blog.excerpt());
            pstmt.setString(4, blog.content());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Blog> getBlogList(){
        List<Blog> blogList = new ArrayList<>();
        String sql = "SELECT * FROM blogs LIMIT 10";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                blogList.add(new Blog(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("tag"),
                        rs.getString("excerpt"),
                        rs.getString("content")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return blogList;
    }


    public static List<Blog> getBlogWithoutContents() {
        List<Blog> blogList = new ArrayList<>();

        try (ResultSet resultSet = executeSelect("""
                    select * from blogs
                    """)){

            while (resultSet.next()) {
                int id =  resultSet.getInt("id");
                String title = resultSet.getString("name");
                String category = resultSet.getString("category");
                String excerpt = resultSet.getString("excerpt");

                blogList.add(new Blog(id, title, category, excerpt, ""));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return blogList;
    }

    public static Blog getBlogById(int id) throws SQLException {
        String sql = "SELECT * FROM blogs WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            preparedStatement.setInt(1, id);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return new Blog(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("tag"),
                            rs.getString("excerpt"),
                            rs.getString("content")
                    );
                }
            }
        }
        return null;
    }

    public static List<Blog> getBlogsByCategory(String category) throws SQLException {
        List<Blog> blogList = new ArrayList<>();
        String sql = "SELECT * FROM blogs WHERE tag = ? Limit 15";
        try (Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    blogList.add( new Blog(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("tag"),
                            rs.getString("excerpt"),
                            rs.getString("content")
                    ));
                }
            }
            return blogList;

        }
    }
}
