import com.azure.identity.AuthorizationCodeCredentialBuilder;
import com.azure.core.credential.TokenCredential;

public class AuthorizationCodeCredentialsSample {
    public void authorizationCodeCredentialsCodeSnippets() {
        TokenCredential authorizationCodeCredential = new AuthorizationCodeCredentialBuilder().authorizationCode(
                "{authorization-code-received-at-redirectURL}")
            .redirectUrl("{redirectUrl-where-authorization-code-is-received}")
            .clientId("{clientId-of-application-being-authenticated")
            .build();
    }
}

