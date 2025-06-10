import com.azure.identity.EnvironmentCredentialBuilder;
import com.azure.core.credential.TokenCredential;

public class EnvironmentCredential {
    public void environmentCredentialsCodeSnippets() {
        TokenCredential environmentCredential = new EnvironmentCredentialBuilder().build();
    }
}
