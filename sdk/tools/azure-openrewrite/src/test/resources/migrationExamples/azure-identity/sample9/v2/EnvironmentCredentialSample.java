import com.azure.v2.identity.EnvironmentCredentialBuilder;
import com.azure.v2.core.credentials.TokenCredential;

public class EnvironmentCredentialSample {
    public void environmentCredentialsCodeSnippets() {
        TokenCredential EnvironmentCredentialInstance = new EnvironmentCredentialBuilder().build();
    }
}



