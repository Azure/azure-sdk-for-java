import com.azure.identity.InteractiveBrowserCredentialBuilder;
import com.azure.core.credential.TokenCredential;

public class InteractiveBrowserCredential {

    public void interactiveBrowserCredentialsCodeSnippets() {
        TokenCredential interactiveBrowserCredential = new InteractiveBrowserCredentialBuilder().redirectUrl(
            "http://localhost:8765").build();
    }
}
