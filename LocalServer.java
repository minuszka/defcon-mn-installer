import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LocalServer {
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 8080;
    private static final Path WEB_ROOT = Paths.get("templates");

    public static void main(String[] args) throws Exception {
        String host = System.getenv().getOrDefault("DEFCON_HOST", DEFAULT_HOST);
        int port = Integer.parseInt(System.getenv().getOrDefault("DEFCON_PORT", String.valueOf(DEFAULT_PORT)));

        HttpServer server = HttpServer.create(new InetSocketAddress(host, port), 0);
        server.createContext("/", new StaticHandler(WEB_ROOT));
        server.setExecutor(null);
        server.start();

        System.out.println("========================================");
        System.out.println("DEFCON Masternode Helper (Local Server)");
        System.out.println("URL: http://" + host + ":" + port + "/");
        System.out.println("Root: " + WEB_ROOT.toAbsolutePath());
        System.out.println("========================================");
    }

    private static class StaticHandler implements HttpHandler {
        private final Path root;

        StaticHandler(Path root) {
            this.root = root.toAbsolutePath().normalize();
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String rawPath = exchange.getRequestURI().getPath();
            String requestPath = rawPath.equals("/") ? "/index.html" : rawPath;

            Path target = root.resolve(requestPath.substring(1)).normalize();
            if (!target.startsWith(root)) {
                sendPlain(exchange, 403, "Forbidden");
                return;
            }

            if (!Files.exists(target) || Files.isDirectory(target)) {
                sendPlain(exchange, 404, "Not Found");
                return;
            }

            String contentType = guessContentType(target);
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
            exchange.getResponseHeaders().set("X-Frame-Options", "DENY");
            exchange.getResponseHeaders().set("Referrer-Policy", "no-referrer");

            long length = Files.size(target);
            exchange.sendResponseHeaders(200, length);
            try (OutputStream os = exchange.getResponseBody();
                 InputStream is = Files.newInputStream(target)) {
                is.transferTo(os);
            }
        }

        private String guessContentType(Path target) {
            String name = target.getFileName().toString().toLowerCase();
            if (name.endsWith(".html")) return "text/html; charset=utf-8";
            if (name.endsWith(".css")) return "text/css; charset=utf-8";
            if (name.endsWith(".js")) return "application/javascript; charset=utf-8";
            if (name.endsWith(".json")) return "application/json; charset=utf-8";
            if (name.endsWith(".png")) return "image/png";
            if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
            if (name.endsWith(".svg")) return "image/svg+xml";
            return "application/octet-stream";
        }

        private void sendPlain(HttpExchange exchange, int status, String message) throws IOException {
            byte[] bytes = message.getBytes();
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}
