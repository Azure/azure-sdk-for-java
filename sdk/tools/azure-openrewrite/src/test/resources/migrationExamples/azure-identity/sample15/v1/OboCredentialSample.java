import com.azure.identity.OnBehalfOfCredentialBuilder;
import com.azure.core.credential.TokenCredential;

public class OboCredentialSample {
    public void oboCredentialsCodeSnippets() {
        TokenCredential onBehalfOfCredential = new OnBehalfOfCredentialBuilder().clientId("<app-client-ID>")
            .clientSecret("<app-Client-Secret>")
            .tenantId("<app-tenant-ID>")
            .userAssertion("<user-assertion>")
            .build();
    }
}

