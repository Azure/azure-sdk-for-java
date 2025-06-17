import com.azure.identity.InteractiveBrowserCredentialBuilder;
import com.azure.core.credential.TokenCredential;

public class InteractiveBrowserCredentialSample {

    public void interactiveBrowserCredentialsCodeSnippets() {
        TokenCredential InteractiveBrowserCredentialInstance = new InteractiveBrowserCredentialBuilder().redirectUrl(
            "http://localhost:8765").build();
    }
}



