package old.downloadMusicServer;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import spark.Response;
import util.Log;

import java.io.*;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static old.downloadMusicServer.DownloadMusicKt.runPython;
import static spark.Spark.*;

public class Server {

    private static TemplateEngine templateEngine;

    public static void main (String[] args) {

        ipAddress("0.0.0.0");
        port(4567);
        staticFileLocation("/public");

        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/templates/");   // път в resources
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false); // за разработка: true в продукция

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(resolver);

        get("/download", (req, res) -> {
            Context context = new Context();
            String html = templateEngine.process("download", context);
            res.type("text/html; charset=UTF-8");

            return html;

        });

        post("/download", (req, res) -> {

            String url = req.queryParams("url");
            String configuredUrl = "";

            switch (req.queryParams("operation")) {
                case "single" -> configuredUrl = old.downloadMusicServer.UtilKt.packageSingles(Collections.singletonList(url));
                case "album" -> configuredUrl = old.downloadMusicServer.UtilKt.packageAlbums(Collections.singletonList(url));
                case "playlist" -> configuredUrl = old.downloadMusicServer.UtilKt.packagePlaylists(Collections.singletonList(url));
            }

            runPython(List.of(configuredUrl).toArray(new String[0]));


            Connection con = DriverManager.getConnection("jdbc:sqlite:test.sqlite", "root", "");

            if(req.queryParams("operation").equals("single")) {
                return single(res, con);
            }

            try(Statement st = con.createStatement()) {
                PreparedStatement pst = con.prepareStatement("" +
                        "select filepath from songs s\n" +
                        "where s.album = ( " +
                        "select s2.album from songs s2 " +
                        "order by s2.id desc " +
                        "limit 1)");
                ResultSet rs = pst.executeQuery();

                var list = (Object[]) rs.getArray("filepath").getArray();
                List<Path> filepath = new ArrayList<>();

                for (Object o : list) {
                    filepath.add((Path) o);
                }

                long number = System.nanoTime()<<2 | 39;

                AlbumZipper.zipAlbum(("temp" + number),filepath, Path.of("temp/"));



            }


            return null;
        });

    }

    private static Object single (Response res, Connection con) throws IOException {
        Path singlePath = Path.of("");
        String title = "ERROR";
        try (Statement st = con.createStatement()) {
            PreparedStatement ps = con.prepareStatement("Select \"hello\"");
            ResultSet rs = ps.executeQuery();

            Log.info(String.valueOf(rs.next()));

            ps = con.prepareStatement(
                    "select * from songs " +
                            "order by id " +
                            "desc limit 1");
            rs = ps.executeQuery();
            if (rs.next()) {
                singlePath = Path.of(rs.getString("filepath"));
                title = rs.getString("title");
            }

        } catch (SQLException e) {
            return res.status();
        }
        Path fullSinglePath = Path.of(("C:/Users/thebeast/music/" + singlePath.toString()));

        Log.info("Downloaded " + fullSinglePath.toAbsolutePath());

        res.type("audio/mpeg");
        res.header("Content-Disposition", "attachment; filename=\"" + title + "\"");

        try (InputStream in = new FileInputStream(fullSinglePath.toAbsolutePath().toFile())) {
            OutputStream out = res.raw().getOutputStream();
            in.transferTo(out);
        }

        return res.raw();
    }

}
