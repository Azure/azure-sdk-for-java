import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.identity.EnvironmentCredentialBuilder;

public class EnvironmentCredentialSample {
    public void environmentCredentialsCodeSnippets() {
        TokenCredential EnvironmentCredentialInstance = new EnvironmentCredentialBuilder().build();
    }
}



