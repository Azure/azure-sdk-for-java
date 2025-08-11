import com.azure.core.credential.AccessToken;
import com.azure.core.credential.BasicAuthenticationCredential;
import com.azure.core.credential.TokenRequestContext;

public class BasicAuthenticationCredentialsApis {
    public static void main(String... args) {

        BasicAuthenticationCredential basicAuthenticationCredentials = new BasicAuthenticationCredential("username", "password");
        AccessToken accessToken = basicAuthenticationCredentials.getTokenSync(new TokenRequestContext());
    }
}
