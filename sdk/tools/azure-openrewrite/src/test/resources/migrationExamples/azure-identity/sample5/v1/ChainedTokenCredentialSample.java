import com.azure.core.credential.TokenCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.identity.InteractiveBrowserCredentialBuilder;
import com.azure.identity.ChainedTokenCredentialBuilder;

public class ChainedTokenCredentialSample {

    private String clientId = System.getenv("AZURE_CLIENT_ID");

    public void chainedTokenCredentialCodeSnippets() {
        TokenCredential managedIdentityCredential = new ManagedIdentityCredentialBuilder().build();
        TokenCredential interactiveBrowserCredential = new InteractiveBrowserCredentialBuilder().clientId(clientId)
            .redirectUrl("https://localhost:8765")
            .build();
        TokenCredential credential = new ChainedTokenCredentialBuilder().addLast(managedIdentityCredential)
            .addLast(interactiveBrowserCredential)
            .build();
    }
}

