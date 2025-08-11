import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientAssertionCredentialBuilder;

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



