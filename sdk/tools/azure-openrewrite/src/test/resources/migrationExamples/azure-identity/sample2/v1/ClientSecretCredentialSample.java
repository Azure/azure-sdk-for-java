import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientSecretCredentialBuilder;


public class ClientSecretCredentialSample {

    private String tenantId = System.getenv("AZURE_TENANT_ID");
    private String clientId = System.getenv("AZURE_CLIENT_ID");
    private String clientSecret = System.getenv("AZURE_CLIENT_SECRET");

    public void clientSecretCredentialCodeSnippets() {
        TokenCredential clientSecretCredential = new ClientSecretCredentialBuilder().tenantId(tenantId)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .build();

        TokenCredential secretCredential;
        secretCredential = new ClientSecretCredentialBuilder().tenantId(tenantId)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .build();
    }

}
