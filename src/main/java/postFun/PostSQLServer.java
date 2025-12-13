package postFun;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import sql.Log;
import sql.Query;

import static spark.Spark.*;

public class PostSQLServer {

    private static TemplateEngine templateEngine;

    public static void main (String[] args) {

        port(8088);
        staticFileLocation("/public");

        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/templates/");   // път в resources
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false); // за разработка: true в продукция

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(resolver);

        get("/query", (req,res) -> {

            Context context = new Context();

            String html = templateEngine.process("query", context);
            res.type("text/html; charset=UTF-8");

            return html;

        });

        post("/query",(req,res) -> {
            String query = req.queryParams("query");
            Log.info(query);
            if(query!=null){
                Query.getResult(query);
            }

            res.redirect("/query");

            return res;

        });
    }

}
