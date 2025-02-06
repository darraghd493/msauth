import me.darragh.msauth.AuthenticationRecord;
import me.darragh.msauth.oauth2.OAuthAuthenticator;
import me.darragh.msauth.oauth2.OAuthOptions;

public class Start {
    public static void main(String[] args) {
        AuthenticationRecord record = new OAuthAuthenticator(OAuthOptions.DEFAULT).performAuthentication();
        AuthenticationRecord record2 = new OAuthAuthenticator(OAuthOptions.DEFAULT).performAuthentication(record);
        System.out.println(record);
        System.out.println(record2);
    }
}
