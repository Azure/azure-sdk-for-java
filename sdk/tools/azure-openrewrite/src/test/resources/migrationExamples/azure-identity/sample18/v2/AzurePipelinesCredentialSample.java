import com.azure.v2.identity.AzurePipelinesCredential;
import com.azure.v2.identity.AzurePipelinesCredentialBuilder;

public class AzurePipelinesCredentialSample {
    private String tenantId = System.getenv("AZURE_TENANT_ID");

    private String clientId = System.getenv("AZURE_CLIENT_ID");
    private String clientSecret = System.getenv("AZURE_CLIENT_SECRET");
    private String serviceConnectionId = System.getenv("SERVICE_CONNECTION_ID");

    public void azurePipelinesCredentialCodeSnippets() {

        // BEGIN: com.azure.identity.credential.AzurePipelinesCredentialSample.construct
        // serviceConnectionId is retrieved from the portal.
        // systemAccessToken is retrieved from the pipeline environment as shown.
        // You may choose another name for this variable.

        String systemAccessToken = System.getenv("SYSTEM_ACCESSTOKEN");
        AzurePipelinesCredential credential = new AzurePipelinesCredentialBuilder().clientId(clientId)
            .tenantId(tenantId)
            .serviceConnectionId(serviceConnectionId)
            .systemAccessToken(systemAccessToken)
            .build();
        // END: com.azure.identity.credential.AzurePipelinesCredentialSample.construct
    }
}


