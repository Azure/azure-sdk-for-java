import com.azure.identity.EnvironmentCredentialBuilder;
import com.azure.core.credential.TokenCredential;

public class EnvironmentCredentialSample {
    public void environmentCredentialsCodeSnippets() {
        TokenCredential EnvironmentCredentialInstance = new EnvironmentCredentialBuilder().build();
    }
}



