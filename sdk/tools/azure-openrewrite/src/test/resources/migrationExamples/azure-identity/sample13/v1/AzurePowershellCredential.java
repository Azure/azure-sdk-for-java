import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.core.credential.TokenCredential;

public class AzurePowershellCredential {
    public void azurePowershellCredentialsCodeSnippets() {
        TokenCredential powerShellCredential = new AzurePowerShellCredentialBuilder().build();
    }
}
