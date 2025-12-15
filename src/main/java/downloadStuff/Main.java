package downloadStuff;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import static spark.Spark.*;

public class Main {

    private static TemplateEngine templateEngine;

    public static void main(String[] args) {


        ipAddress("0.0.0.0");
        port(4567);
        staticFileLocation("/public");

        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(resolver);

        get("/down", (req, res) ->{

            Context context = new Context();

            String html = templateEngine.process("down", context);
            res.type("text/html; charset=UTF-8");

            return html;
        });

        get("/", (req, res) -> "Hello");

//        post("/down", (req, res) ->{
//
//            res.type("text/plain");
//
//            res.header("Content-Disposition","attachment; filename=\"something\"");
//
//            Path path = Path.of("gpp.txt");
//
//            try (InputStream in = new FileInputStream(path.toAbsolutePath().toFile())) {
//                OutputStream out = res.raw().getOutputStream();
//                in.transferTo(out);
//            }
//
//            return res;
//        });

    }

}
