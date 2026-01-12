package voting;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import util.DBUtil;
import util.Log;
import util.MediaType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static spark.Spark.*;



public class VotingGame {

    private static TemplateEngine templateEngine;

    private static JsonParser jsonParser;

    private static final String ADMIN_COOKIE = "adminToken";

    static Map<String, Integer> votes;
    static{
        votes = new HashMap<>();
        votes.put("1", 0);
        votes.put("2", 0);
        votes.put("3", 0);
    }

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

        String globalVoteHash = "92039203";

        post("/api/auth", (req, res) -> {
            Map<String, String> body = gson.fromJson(req.body(), Map.class);

            String secret = body.get("secret");
            String correctSecret = "mySecret123";

            res.type("application/json");
            if(secret != null && secret.equals(correctSecret)) {
                String token = UUID.randomUUID().toString();
                res.cookie("adminToken", token, 3600); // 1 hour
                return Map.of("status","authorized","token",token);
            } else {
                return Map.of("status","unauthorized");
            }
        }, gson::toJson);

        post("api/vote", (request, response) -> {
            String voteHash = request.cookie("voteHash");
//            if(voteHash != null) {
//                response.type("application/json");
//                return Map.of("status", "already_voted");
//            }
            Log.exec(request.body());
            String option = request.queryParams("option");
            if(votes.containsKey(option)) {
                votes.put(option, votes.get(option) + 1);
            }

            response.cookie("voteHash", globalVoteHash, 3600);
            response.type("application/json");
            return votes;
        }, gson::toJson);

        get("/api/votes", (req, res) -> {
            res.type("application/json");
            return votes;
        }, gson::toJson);


        post("/api/reset", (req, res) -> {
            String token = req.cookie(ADMIN_COOKIE);
            if(token == null) {
                res.status(403);
                return Map.of("status", "unauthorized");
            }

            votes.replaceAll((k, v) -> 0);
            res.type("application/json");
            return Map.of("status", "votes_reset");
        }, gson::toJson);

        get("/", (request, response) -> {
            response.type(MediaType.HTML.getValue());
            return templateEngine.process("voting", new Context());
        });



    }
}
