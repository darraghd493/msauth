package me.darragh.msauth.oauth2.server;

/**
 * A simple {@link OAuthPageHandler} that displays a message.
 *
 * @author darraghd493
 * @since 1.0.0
 *
 * @see OAuthPageHandler
 * */
public class SimpleOAuthPageHandler implements OAuthPageHandler {
    public static final String PAGE = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta http-equiv="X-UA-Compatible" content="IE=edge">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>OAuth2</title>
        </head>
        <body>
            <h1>%s</h1>
        </body>
        </html>
        """;

    @Override
    public String generatePage(String message) {
        return String.format(PAGE, message);
    }
}
