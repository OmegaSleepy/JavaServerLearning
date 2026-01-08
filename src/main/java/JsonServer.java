import com.google.gson.Gson;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import static spark.Spark.*;
import static util.Log.*;

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
        templateEngine.setTemplateResolver(resolver);


        get("/", (req, res) -> {
            Context context = new Context();

            String html = templateEngine.process("json", context);
            res.type(MediaType.HTML.value);

            return html;
        });

        get("/api/data", (req, res) -> {
           res.type(MediaType.JSON.value);
           return new Response("Hello", 42);
        }, gson::toJson
        );
    }
    record Response(String message, int value){}
}
