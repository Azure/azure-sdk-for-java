import com.azure.identity.IntelliJCredentialBuilder;
import com.azure.core.credential.TokenCredential;

public class IntellijCredential {
    public void intelliJCredentialsCodeSnippets() {
        TokenCredential intelliJCredential = new IntelliJCredentialBuilder().build();
    }
}
