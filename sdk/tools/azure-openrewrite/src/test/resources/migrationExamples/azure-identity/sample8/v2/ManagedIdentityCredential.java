import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.core.credential.TokenCredential;

public class ManagedIdentityCredential {
    private String clientId = System.getenv("AZURE_CLIENT_ID");

    public void managedIdentityCredentialsCodeSnippets() {
        TokenCredential managedIdentityCredentialUserAssigned = new ManagedIdentityCredentialBuilder().clientId(
                clientId)
            .build();
        TokenCredential managedIdentityCredential = new ManagedIdentityCredentialBuilder().build();
    }
}
