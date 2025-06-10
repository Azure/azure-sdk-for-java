import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.core.credential.TokenCredential;

public class AzureCliCredential {
    public void azureCliCredentialsCodeSnippets() {
        TokenCredential azureCliCredential = new AzureCliCredentialBuilder().build();
    }
}
