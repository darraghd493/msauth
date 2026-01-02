package me.darragh.msauth.oauth2.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.RequiredArgsConstructor;
import me.darragh.msauth.oauth2.OAuthOptions;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles the HTTP requests to the OAuth pageHandler.
 *
 * @author darraghd493
 * @since 1.0.0
 */
@RequiredArgsConstructor
public class OAuthServerHandler implements HttpHandler {
    private static final Executor EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    private static final Logger LOGGER = Logger.getLogger(OAuthServerHandler.class.getName());

    private final @NotNull OAuthPageHandler pageHandler;
    private final @NotNull OAuthResponseHandler responseHandler;

    private final int port;

    private HttpServer server;

    public OAuthServerHandler(@NotNull OAuthOptions options, @NotNull OAuthResponseHandler responseHandler) {
        this.pageHandler = options.pageHandler();
        this.port = options.port();
        this.responseHandler = responseHandler;
    }

    @Override
    public void handle(@NotNull HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            exchange.sendResponseHeaders(405, -1);
            exchange.close();
            return;
        }

        OAuthResponseState responseState;
        try {
            // Attempt to handle the response, this might throw an exception
            responseState = this.responseHandler.handleResponse(exchange);
        } catch (Exception e) {
            // If an exception occurs, log it and set the state to FAILURE
            LOGGER.log(Level.SEVERE, "An error occurred while handling the OAuth response.", e);
            responseState = OAuthResponseState.FAILURE;
        }

        // Based on the response state, generate the appropriate page
        String pageMessage = this.pageHandler.getMessage(responseState);
        String page = this.pageHandler.generatePage(pageMessage);

        // Always write a response, even on failure
        this.standardWrite(exchange, page);

        // Stop the server only on successful authentication
        if (responseState == OAuthResponseState.SUCCESS) {
            // Use the executor to stop the server asynchronously to avoid race conditions
            // and ensure the response is fully sent.
            EXECUTOR_SERVICE.execute(this::stop);
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
     * Checks if the server is running.
     *
     * @return True if the server is running, false otherwise.
     */
    public boolean isRunning() {
        return this.server != null && this.server.getAddress() != null;
    }

    /**
     * Writes the response to the given request in a standard way.
     * <p>
     * Performs:
     * - status code of 200
     * - indicates the content type of text/html
     * - indicates the charset of utf-8
     * - writes the given string to the response body in utf-8
     *
     * @param request The request to write to.
     * @param string The string to write.
     * @throws IOException If an I/O error occurs.
     */
    private void standardWrite(@NotNull HttpExchange request, @NotNull String string) throws IOException {
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        request.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        request.sendResponseHeaders(200, bytes.length);
        try (OutputStream outputStream = request.getResponseBody()) {
            outputStream.write(bytes);
            outputStream.flush();
        }
    }
}