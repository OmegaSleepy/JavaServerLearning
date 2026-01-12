package posts;

import com.google.gson.Gson;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import util.Log;

import java.util.concurrent.atomic.AtomicInteger;

import static spark.Spark.*;
import static spark.Spark.get;

public class Server {
    private static TemplateEngine templateEngine;

    public static AtomicInteger count;


    public static void main(String[] args) {
        templateEngine = new TemplateEngine();
        port(4567);
        staticFileLocation("/public");
        SQL.initDatabase();

        count = new AtomicInteger();

        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/templates/");   // път в resources
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false); // за разработка: true в продукция
        templateEngine.setTemplateResolver(resolver);

        get("/create", (req, res) -> {
            Context context = new Context();

            String html = templateEngine.process("blogPostCreator", context);
            res.type(MediaType.HTML.value);

            return html;
        });

        post("/api/blog", (req, res) -> {
            String title = req.queryParams("title");
            String body = req.queryParams("blog-body");

            SQL.addBlog(new BlogPost(title,body));

            Log.info(title + " | " + body);

            res.redirect("/create");

            return res;
        });
    }
}
