package postFun;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import static spark.Spark.*;
import static util.Log.*;

public class PostSQLServer {

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


        get("/log", (req, res) -> {

            Context context = new Context();

            String html = templateEngine.process("log", context);
            res.type("text/html; charset=UTF-8");

            return html;
        });

        post("/log", (req,res) -> {

            String text = req.queryParams("string");
            String logOperation = req.queryParams("logOperation");

            switch (logOperation) {
                case "info" -> info(text);
                case "error" -> error(text);
                case "warn" -> warn(text);
                case "execute" -> exec(text);
            }

            res.redirect("/log");

            return res;
        });

        get("/", (req, res) ->{
            res.redirect("/query");
            return res;
        });
    }

}
