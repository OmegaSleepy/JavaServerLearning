import com.google.gson.Gson;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import static spark.Spark.*;

public class JsonServer {

    private static TemplateEngine templateEngine;


    public static void main(String[] args) {
        templateEngine = new TemplateEngine();
        port(4567);
        staticFileLocation("/public");

        Gson gson = new Gson();

        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/templates/");   // път в resources
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false); // за разработка: true в продукция


        get("/", (req, res) -> {
            Context context = new Context();

            String html = templateEngine.process("json", context);
            res.type("text/html; charset=UTF-8");

            return html;
        });

        get("/api/data", (req, res) -> {
           res.type("application/json");
           return new Response("Hello", 42);
        }, gson::toJson
        );
    }
    record Response(String message, int value){}
}
