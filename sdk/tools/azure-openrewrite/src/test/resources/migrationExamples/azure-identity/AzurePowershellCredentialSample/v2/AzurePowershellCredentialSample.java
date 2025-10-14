import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.identity.AzurePowerShellCredentialBuilder;

public class AzurePowershellCredentialSample {
    public void azurePowershellCredentialsCodeSnippets() {
        TokenCredential powerShellCredential = new AzurePowerShellCredentialBuilder().build();
    }
}

