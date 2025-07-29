import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.identity.OnBehalfOfCredentialBuilder;

public class OboCredentialSample {
    public void oboCredentialsCodeSnippets() {
        TokenCredential onBehalfOfCredential = new OnBehalfOfCredentialBuilder().clientId("<app-client-ID>")
            .clientSecret("<app-Client-Secret>")
            .tenantId("<app-tenant-ID>")
            .userAssertion("<user-assertion>")
            .build();
    }
}

