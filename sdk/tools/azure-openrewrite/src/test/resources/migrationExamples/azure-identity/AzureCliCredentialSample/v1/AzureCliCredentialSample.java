import com.azure.core.credential.TokenCredential;
import com.azure.identity.AzureCliCredentialBuilder;

public class AzureCliCredentialSample {
    public void azureCliCredentialsCodeSnippets() {
        TokenCredential AzureCliCredentialInstance = new AzureCliCredentialBuilder().build();
    }
}



