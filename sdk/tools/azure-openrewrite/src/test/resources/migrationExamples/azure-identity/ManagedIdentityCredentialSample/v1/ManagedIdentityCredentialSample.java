import com.azure.core.credential.TokenCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;

public class ManagedIdentityCredentialSample {
    private String clientId = System.getenv("AZURE_CLIENT_ID");

    public void managedIdentityCredentialsCodeSnippets() {
        TokenCredential managedIdentityCredentialUserAssigned = new ManagedIdentityCredentialBuilder().clientId(clientId)
            .build();
        TokenCredential ManagedIdentityCredentialInstance = new ManagedIdentityCredentialBuilder().build();
    }
}



