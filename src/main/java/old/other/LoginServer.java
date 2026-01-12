package old.other;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import spark.ModelAndView;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

public class LoginServer {
    private static TemplateEngine templateEngine;

    public static void main(String[] args) {

        port(8088);

        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/templates/");   // път в resources
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false); // за разработка: true в продукция

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(resolver);

        // --- Login page (GET) ---
        get("/login", (req, res) -> {
            Context ctx = new Context();

            // Генерираме HTML чрез Thymeleaf
            String html = templateEngine.process("login", ctx);
            res.type("text/html; charset=UTF-8");
            return html;

        });

        // --- Login submit (POST) ---
        post("/login", (req, res) -> {
            String username = req.queryParams("username");
            String password = req.queryParams("password");

            if (username.equals("admin") && password.equals("1234")) {
                req.session(true).attribute("user", username);
                res.redirect("/home");
                return null;
            }

            // Ако не е валидно
            Map<String, Object> model = new HashMap<>();
            model.put("error", "Грешни данни!");
            return new ModelAndView(model, "login");
        });

        get("/home", (req, res) -> {
            String user = req.session().attribute("user");

            if (user == null) {
                res.redirect("/login");
                return null;
            }

            Map<String, Object> model = new HashMap<>();
            model.put("username", user);

            return new ModelAndView(model, "home");

        });

        // --- Logout ---
        post("/logout", (req, res) -> {
            req.session().invalidate();
            res.redirect("/login");
            return null;
        });
    }
}
