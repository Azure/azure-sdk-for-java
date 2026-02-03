import com.azure.core.credential.TokenCredential;
import com.azure.identity.OnBehalfOfCredentialBuilder;

public class OboCredentialSample {
    public void oboCredentialsCodeSnippets() {
        TokenCredential onBehalfOfCredential = new OnBehalfOfCredentialBuilder().clientId("<app-client-ID>")
            .clientSecret("<app-Client-Secret>")
            .tenantId("<app-tenant-ID>")
            .userAssertion("<user-assertion>")
            .build();
    }
}

