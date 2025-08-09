import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;

public class TokenCredentialApis {
    public static void main(String... args) {

        TokenCredential credential = null; // Assume this is initialized
        AccessToken token = credential.getTokenSync(new TokenRequestContext());

    }
}
