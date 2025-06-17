import com.azure.core.credential.TokenCredential;
import com.azure.identity.EnvironmentCredentialBuilder;

public class EnvironmentCredentialSample {
    public void environmentCredentialsCodeSnippets() {
        TokenCredential EnvironmentCredentialInstance = new EnvironmentCredentialBuilder().build();
    }
}



