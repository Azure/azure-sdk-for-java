import com.azure.v2.identity.ClientAssertionCredentialBuilder;
import com.azure.v2.core.credentials.TokenCredential;

public class ClientAssertionCredentialSample {

    private String tenantId = System.getenv("AZURE_TENANT_ID");
    private String clientId = System.getenv("AZURE_CLIENT_ID");

    public void clientAssertionCredentialCodeSnippets() {
        TokenCredential ClientAssertionCredentialInstance = new ClientAssertionCredentialBuilder().tenantId(tenantId)
            .clientId(clientId)
            .clientAssertion(() -> "<Client-Assertion>")
            .build();

        TokenCredential assertionCredential = new ClientAssertionCredentialBuilder().tenantId(tenantId)
            .clientId(clientId)
            .clientAssertion(() -> "<Client-Assertion>")
            .build();
    }

}



