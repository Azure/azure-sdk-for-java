import com.azure.v2.identity.AuthorizationCodeCredentialBuilder;
import com.azure.v2.core.credentials.TokenCredential;

public class AuthorizationCodeCredentialsSample {
    public void authorizationCodeCredentialsCodeSnippets() {
        TokenCredential authorizationCodeCredential = new AuthorizationCodeCredentialBuilder().authorizationCode(
            "{authorization-code-received-at-redirectURL}")
            .redirectUrl("{redirectUrl-where-authorization-code-is-received}")
            .clientId("{clientId-of-application-being-authenticated")
            .build();
    }
}

