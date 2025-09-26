import com.azure.core.credential.TokenCredential;
import com.azure.identity.AuthorizationCodeCredentialBuilder;

public class AuthorizationCodeCredentialsSample {
    public void authorizationCodeCredentialsCodeSnippets() {
        TokenCredential authorizationCodeCredential = new AuthorizationCodeCredentialBuilder().authorizationCode(
            "{authorization-code-received-at-redirectURL}")
            .redirectUrl("{redirectUrl-where-authorization-code-is-received}")
            .clientId("{clientId-of-application-being-authenticated")
            .build();
    }
}

