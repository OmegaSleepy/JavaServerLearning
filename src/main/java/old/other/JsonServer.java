package old.other;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import util.MediaType;
import util.Log;

import static spark.Spark.*;

public class JsonServer {

    private static TemplateEngine templateEngine;

    public static int counter = 0;

    private static JsonParser jsonParser;

    public static void main(String[] args) {
        templateEngine = new TemplateEngine();
        ipAddress("0.0.0.0");
        port(4567);
        staticFileLocation("/public");

        Gson gson = new Gson();
        jsonParser = new JsonParser();

        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/templates/");   // път в resources
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false); // за разработка: true в продукция
        templateEngine.setTemplateResolver(resolver);


        post("/api/data", (req, res) -> {
            Log.exec(req.body());
            JsonObject body = jsonParser.parse(req.body()).getAsJsonObject();
            boolean increment = body.get("increment").getAsBoolean();

            if(increment) {
                counter++;
            }

            return new SimpleResponse("Hello", counter);
        }, gson::toJson);

        get("/", (req, res) -> {
            Log.exec(req.body());
            Context context = new Context();

            String html = templateEngine.process("json", context);
            res.type(MediaType.HTML.getValue());

            return html;
        });
    }

    public record SimpleResponse(String message, int count) {
    }
}
