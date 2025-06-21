import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.identity.ManagedIdentityCredentialBuilder;

public class ManagedIdentityCredentialSample {
    private String clientId = System.getenv("AZURE_CLIENT_ID");

    public void managedIdentityCredentialsCodeSnippets() {
        TokenCredential managedIdentityCredentialUserAssigned = new ManagedIdentityCredentialBuilder().clientId(clientId)
            .build();
        TokenCredential ManagedIdentityCredentialInstance = new ManagedIdentityCredentialBuilder().build();
    }
}



