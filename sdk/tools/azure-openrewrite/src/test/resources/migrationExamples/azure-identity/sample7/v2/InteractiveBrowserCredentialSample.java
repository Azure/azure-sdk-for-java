import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.identity.InteractiveBrowserCredentialBuilder;

public class InteractiveBrowserCredentialSample {

    public void interactiveBrowserCredentialsCodeSnippets() {
        TokenCredential InteractiveBrowserCredentialInstance = new InteractiveBrowserCredentialBuilder().redirectUrl(
            "http://localhost:8765").build();
    }
}



