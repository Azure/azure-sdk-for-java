import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.identity.ClientSecretCredentialBuilder;


public class ClientSecretCredentialSample {

    private String tenantId = System.getenv("AZURE_TENANT_ID");
    private String clientId = System.getenv("AZURE_CLIENT_ID");
    private String clientSecret = System.getenv("AZURE_CLIENT_SECRET");

    public void clientSecretCredentialCodeSnippets() {
        // BEGIN: com.azure.identity.credential.ClientSecretCredentialSample.construct
        TokenCredential ClientSecretCredentialInstance = new ClientSecretCredentialBuilder().tenantId(tenantId)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .build();
        // END: com.azure.identity.credential.ClientSecretCredentialSample.construct

        // BEGIN: com.azure.identity.credential.ClientSecretCredentialSample.constructwithproxy
        TokenCredential secretCredential;
        // END: com.azure.identity.credential.ClientSecretCredentialSample.constructwithproxy
        secretCredential = new ClientSecretCredentialBuilder().tenantId(tenantId)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();
    }

}



