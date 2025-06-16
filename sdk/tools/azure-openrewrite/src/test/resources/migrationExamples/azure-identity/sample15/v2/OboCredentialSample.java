import com.azure.v2.identity.OnBehalfOfCredentialBuilder;
import com.azure.v2.core.credentials.TokenCredential;

public class OboCredentialSample {
    public void oboCredentialsCodeSnippets() {
        TokenCredential onBehalfOfCredential = new OnBehalfOfCredentialBuilder().clientId("<app-client-ID>")
            .clientSecret("<app-Client-Secret>")
            .tenantId("<app-tenant-ID>")
            .userAssertion("<user-assertion>")
            .build();
    }
}

