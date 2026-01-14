package crud;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import spark.utils.IOUtils;
import util.DBUtil;
import util.Log;
import util.MediaType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static spark.Spark.*;

public class CRUDIntro {

    private static TemplateEngine templateEngine;

    private static JsonParser jsonParser;
    static List<String> categories = List.of("Mathematics", "Science", "Biology", "Chemistry", "Physics", "English", "History",
            "Geography", "Art", "Music", "Computer Science", "Economics", "Philosophy",
            "Literature");



    private final static List<String> categories = List.of("Mathematics", "Science", "Biology", "Chemistry", "Physics", "English", "History",
            "Geography", "Art", "Music", "Computer Science", "Economics", "Philosophy",
            "Literature");

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

            try {
                Blog blog = DBUtil.getBlogById(Integer.parseInt(request.params(":id")));
                model.put("blog", blog);

                Context context = new Context();
                context.setVariables(model);

                return templateEngine.process("blog_page", context);
            } catch (Exception e) {
                response.status(400);
                return "{\"status\":\"error\"}";
            }

        });

        post("/api/blog/content", ((request, response) -> {
            response.type(MediaType.JSON.getValue());


            JsonObject body = jsonParser.parse(request.body()).getAsJsonObject();
//            JsonObject dd = gson.fromJson(request.body(), JsonObject.class);

            String title = body.get("title").getAsString();
            String category = body.get("category").getAsString();
            String excerpt = body.get("excerpt").getAsString();
            String content = body.get("content").getAsString();

            Blog blog = new Blog(0, title,category,excerpt,content);

            if(!isValidBlog(blog)) return "{\"status\":\"not ok\"}";

            Log.info(blog.toString());
            DBUtil.addBlog(blog);

            return "{\"status\":\"ok\"}";
        }));

        get("/api/blog/tags", (request, response) -> {
            response.type(MediaType.JSON.getValue());

            return categories;

        }, gson::toJson);

        get("api/blog/blogs", (request, response) -> {
            response.type(MediaType.JSON.getValue());
            return DBUtil.getBlogList();
        }, gson::toJson);

        get("api/blog/short_blogs", (request, response) -> {
            response.type(MediaType.JSON.getValue());
            return DBUtil.getBlogWithoutContents();
        }, gson::toJson);

        get("/api/posts/:id", (request, response) -> {
            response.type(MediaType.TXT.getValue());

            var id = Integer.parseInt(request.params(":id"));
            Blog blog;
            try{
                blog = DBUtil.getBlogById(id);
                return blog.content();

            } catch (Exception e){
                response.status(400);
                return "{\"status\":\"error\"}";
            }
        } );

        get("api/general/styles", (request, response) -> {
            response.type("text/css");

            try (var inputStream = CRUDIntro.class.getResourceAsStream("/public/css/umeniyaStyleSheet.css")) {
                if (inputStream == null) {
                    response.status(404);
                    return "/* CSS file not found in classpath */";
                }
                return IOUtils.toString(inputStream);
            } catch (Exception e) {
                response.status(500);
                return "/* Server Error loading CSS */";
            }
        });

        get("favicon.ico", (request, response) -> {
            response.type("image/x-icon");
            try (var inputStream = CRUDIntro.class.getResourceAsStream("/public/img/vlc.ico")) {
                if (inputStream == null) {
                    response.status(404);
                    return null;
                }

                // 3. Set caching (Favicons rarely change, so cache heavily!)
                response.header("Cache-Control", "public, max-age=604800"); // 1 week

                // 4. Return the raw bytes
                byte[] bytes = IOUtils.toByteArray(inputStream);

                // Important: Write the bytes directly to the raw response buffer
                response.raw().getOutputStream().write(bytes);
                response.raw().getOutputStream().flush();
                response.raw().getOutputStream().close();

                return response.raw();
            } catch (Exception e) {
                response.status(500);
                return "Internal Server Error";
            }
        });

        get("/api/filter/post/:category", (request, response) -> {
           response.type(MediaType.JSON.getValue());
           String category = request.params(":category");

           if(categories.contains(category)){
               return DBUtil.getBlogsByCategory(category);
           }

           response.status(400);
           return "Error";

        }, gson::toJson);

    }

    private static boolean isValidBlog(Blog blog) {
        var titleL = blog.title().length();
        if(titleL > 64) return false;
        if(!categories.contains(blog.tag())) return false;
        if(blog.excerpt().length() > 64) return false;
        if(blog.content().length() > 8000) return false;
        return true;
    }


}
