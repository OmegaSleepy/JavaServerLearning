//import static spark.Spark.*;
//
//import log.Log;
//import org.thymeleaf.TemplateEngine;
//import org.thymeleaf.context.Context;
//import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
//import sql.query.Query;
//import sql.query.Result;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Objects;
//import java.util.logging.Logger;
//
//public class Main {
//   private static TemplateEngine templateEngine;
////
////    private static User getUserById(int id){
////
////        int max = Integer.parseInt(
////                Result.getFirstRow(
////                        Objects.requireNonNull(
////                                Query.fromString(
////                                        "Select count(*) from Users where id is not null"
////                                )
////                        )
////                )
////                        [0]);
////
////        if(id > max) return null;
////
////        var result = Result.getFirstRow(
////                Objects.requireNonNull(
////                        Query.fromString(
////                        "Select * from users where id = %d".formatted(id)
////                    )
////                )
////        );
////
////        return new User(id, result[1], result[2], Integer.parseInt(result[3]));
////    }
////
//   public static void main(String[] args) {
//       port(4567);
//
//       // Сервиране на статични файлове от resources/public
//       staticFileLocation("/public");
//
//       // Конфигуриране на Thymeleaf
//       ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
//       resolver.setPrefix("/templates/");   // път в resources
//       resolver.setSuffix(".html");
//       resolver.setTemplateMode("HTML");
//       resolver.setCharacterEncoding("UTF-8");
//       resolver.setCacheable(false); // за разработка: true в продукция
//
//       templateEngine = new TemplateEngine();
//       templateEngine.setTemplateResolver(resolver);
//
//       Log.info("Template Engine Initialized");
//       Log.info("localhost:%s/user/".formatted(4567));
//
//       // Примерен route
//       get("/user/:id", (req, res) -> {
//           // Тук нормално четеш от DB; за пример — хардкоднати данни
//           String id = req.params("id");
//
//           User user = getUserById(Integer.parseInt(id));
//
//           Map<String, Object> model = new HashMap<>();
//           model.put("title", "Потребител #" + user.id());
//           model.put("author", "%s %s".formatted(user.name(), user.lastName()));
//           model.put("points", user.stars());
//           // content може да съдържа HTML (p, img и т.н.)
//           model.put("content", "<p>y = 8/12x; x = %s -> %d</p>".formatted(id,(Integer.parseInt(id)*8/12)));
//
//           Context ctx = new Context();
//           ctx.setVariables(model);
//
//           // Генерираме HTML чрез Thymeleaf
//           String html = templateEngine.process("article", ctx);
//           res.type("text/html; charset=UTF-8");
//           return html;
//       });
//   }
//}
