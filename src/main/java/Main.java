import static spark.Spark.*;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.HashMap;
import java.util.Map;

public class Main {
    private static TemplateEngine templateEngine;

    public static void main(String[] args) {
        port(4567);

        // Сервиране на статични файлове от resources/public
        staticFileLocation("/public");

        // Конфигуриране на Thymeleaf
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/templates/");   // път в resources
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false); // за разработка: true в продукция

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(resolver);

        // Примерен route
        get("/article/:id", (req, res) -> {
            // Тук нормално четеш от DB; за пример — хардкоднати данни
            String id = req.params("id");
            Map<String, Object> model = new HashMap<>();
            model.put("title", "Примерна статия #" + id);
            model.put("author", "Иван Петров");
            model.put("points", 12);
            // content може да съдържа HTML (p, img и т.н.)
            model.put("content", "<p>y = 8/12x; x = %s -> %d</p>".formatted(id,(Integer.parseInt(id)*8/12)));

            Context ctx = new Context();
            ctx.setVariables(model);

            // Генерираме HTML чрез Thymeleaf
            String html = templateEngine.process("article", ctx);
            res.type("text/html; charset=UTF-8");
            return html;
        });
    }
}
