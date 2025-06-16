import com.azure.v2.identity.AzurePowerShellCredentialBuilder;
import com.azure.v2.core.credentials.TokenCredential;

public class AzurePowershellCredentialSample {
    public void azurePowershellCredentialsCodeSnippets() {
        TokenCredential powerShellCredential = new AzurePowerShellCredentialBuilder().build();
    }
}

