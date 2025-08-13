import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.identity.ChainedTokenCredentialBuilder;
import com.azure.v2.identity.InteractiveBrowserCredentialBuilder;
import com.azure.v2.identity.ManagedIdentityCredentialBuilder;

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

