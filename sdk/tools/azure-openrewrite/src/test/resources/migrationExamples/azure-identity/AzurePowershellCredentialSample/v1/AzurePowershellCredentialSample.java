import com.azure.core.credential.TokenCredential;
import com.azure.identity.AzurePowerShellCredentialBuilder;

public class AzurePowershellCredentialSample {
    public void azurePowershellCredentialsCodeSnippets() {
        TokenCredential powerShellCredential = new AzurePowerShellCredentialBuilder().build();
    }
}

