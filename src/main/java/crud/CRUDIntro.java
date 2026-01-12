package crud;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import util.DBUtil;
import util.Log;
import util.MediaType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static spark.Spark.*;

public class CRUDIntro {

    private static TemplateEngine templateEngine;

    private static JsonParser jsonParser;

    public static void main(String[] args) {

        templateEngine = new TemplateEngine();
        ipAddress("0.0.0.0");
        port(4567);
        staticFileLocation("/public");

        Gson gson = new Gson();
        jsonParser = new JsonParser();
        DBUtil.innitBlogTable();

        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/templates/");   // път в resources
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false); // за разработка: true в продукция
        templateEngine.setTemplateResolver(resolver);

        get("/", ((request, response) -> {
            response.type(MediaType.HTML.getValue());
            return templateEngine.process("start", new Context());
        }));

        get("/create", (req,res) ->{

            res.type(MediaType.HTML.getValue());
            return templateEngine.process("create_blog", new Context());

        });

        get("/home", ((request, response) -> {
            response.type(MediaType.HTML.getValue());
            return templateEngine.process("home_blogs", new Context());
        }));

        get("blog/:id", (request, response) -> {
            response.type(MediaType.HTML.getValue());

            Map<String, Object> model = new HashMap<>();

            model.put("id", request.params(":id"));
            Blog blog = DBUtil.getBlogById(Integer.parseInt(request.params(":id")));

            model.put("blog", blog);

            Context context = new Context();
            context.setVariables(model);

            return templateEngine.process("blog_page", context);
        });

        post("/api/blog/content", ((request, response) -> {

            JsonObject body = jsonParser.parse(request.body()).getAsJsonObject();


            String title = body.get("title").getAsString();
            String category = body.get("category").getAsString();
            String excerpt = body.get("excerpt").getAsString();
            String content = body.get("content").getAsString();

            Blog blog = new Blog(0, title,category,excerpt,content);

            Log.info(blog.toString());
            DBUtil.addBlog(blog);

            response.type(MediaType.JSON.getValue());
            return "{\"status\":\"ok\"}";
        }));

        get("/api/blog/tags", (request, response) -> {

            response.type(MediaType.JSON.getValue());

            return List.of("Mathematics", "Science", "Biology", "Chemistry", "Physics", "English", "History",
                    "Geography", "Art", "Music", "Computer Science", "Economics", "Philosophy",
                    "Literature");

        }, gson::toJson);

        get("api/blog/blogs", (request, response) -> {
            response.type(MediaType.JSON.getValue());
            return DBUtil.getBlogList();
        }, gson::toJson);

        get("api/blog/short_blogs", (request, response) -> {
            response.type(MediaType.JSON.getValue());
            return DBUtil.getBlogWithoutContents();
        }, gson::toJson);

    }


}
