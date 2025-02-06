package me.darragh.msauth.oauth2.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.RequiredArgsConstructor;
import me.darragh.msauth.oauth2.OAuthOptions;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Handles the HTTP requests to the OAuth pageHandler.
 *
 * @author darraghd493
 * @since 1.0.0
 */
@RequiredArgsConstructor
public class OAuthServerHandler implements HttpHandler {
    private static final Executor EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    private final OAuthPageHandler pageHandler;
    private final OAuthResponseHandler responseHandler;

    private final int port;

    private HttpServer server;

    public OAuthServerHandler(OAuthOptions options, OAuthResponseHandler responseHandler) {
        this.pageHandler = options.pageHandler();
        this.port = options.port();
        this.responseHandler = responseHandler;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            exchange.sendResponseHeaders(405, -1);
            exchange.close();
            return;
        }

        OAuthResponseState responseState = this.responseHandler.handleResponse(exchange);
        String pageMessage = this.pageHandler.getMessage(responseState);
        String page = this.pageHandler.generatePage(pageMessage);

        this.standardWrite(exchange, page);
        exchange.close();

        if (responseState == OAuthResponseState.SUCCESS) {
            this.stop();
        }
    }

    /**
     * Starts the server.
     */
    public void start() throws IOException {
        this.server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
        this.server.createContext("/", this);
        this.server.setExecutor(EXECUTOR_SERVICE);
        this.server.start();
    }

    /**
     * Stops the server.
     */
    public void stop() {
        if (this.server == null) {
            return;
        }
        this.server.stop(0);
        this.server = null;
    }

    /**
     * Writes the response to the given request in <i>a standard way</i>.
     * <p>
     * Performs:
     *      - status code of 200
     *      - indicates the content type of text/html
     *      - indicates the charset of utf-8
     *      - writes the given string to the response body in utf-8
     *
     * @param request The request to write to.
     * @param string The string to write.
     * @throws IOException If an I/O error occurs.
     */
    private void standardWrite(HttpExchange request, String string) throws IOException {
        OutputStream out = request.getResponseBody();
        request.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        request.sendResponseHeaders(200, string.length());
        out.write(string.getBytes(StandardCharsets.UTF_8));
        out.flush();
        out.close();
    }
}
