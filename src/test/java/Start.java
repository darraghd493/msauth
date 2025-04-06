import me.darragh.msauth.AuthenticationRecord;
import me.darragh.msauth.oauth2.OAuthAuthenticator;
import me.darragh.msauth.oauth2.OAuthOptions;

public class Start {
    public static void main(String[] args) {
        OAuthAuthenticator authenticator = new OAuthAuthenticator(
                OAuthOptions.DEFAULT
        );
        System.out.println(authenticator.generateUrl());
        AuthenticationRecord record = authenticator.performAuthentication();

        AuthenticationRecord record2 = new OAuthAuthenticator(
                OAuthOptions.DEFAULT
        ).performAuthentication(record);

        System.out.println(record);
        System.out.println(record2);
    }
}
