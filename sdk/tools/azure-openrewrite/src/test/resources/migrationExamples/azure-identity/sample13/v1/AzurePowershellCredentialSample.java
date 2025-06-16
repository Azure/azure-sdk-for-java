import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.core.credential.TokenCredential;

public class AzurePowershellCredentialSample {
    public void azurePowershellCredentialsCodeSnippets() {
        TokenCredential powerShellCredential = new AzurePowerShellCredentialBuilder().build();
    }
}

