package downloadMusicServer;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import util.Log;

import java.io.*;
import java.util.Collections;

import static downloadMusicServer.UtilKt.*;
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
           String album = req.queryParams("album");
           String single = req.queryParams("single");

           File mp3 = new File("MANIFEST.mp3");


           album = packageAlbums(Collections.singletonList(getAlbumHash(album)));
           single = packageSingles(Collections.singletonList(getSingleHash(single)));

           Log.info(album + " " + single);

           String response = album + " " + single;
           String content = "Hello from server!\nGenerated at: " + System.currentTimeMillis() + "\n" + response;

            res.type("audio/mpeg");
            res.header("Content-Disposition", "attachment; filename=\"song.mp3\"");

            try (InputStream in = new FileInputStream(mp3)){
                OutputStream out = res.raw().getOutputStream();
                in.transferTo(out);
            }

           return res.raw();

        });

    }

}
