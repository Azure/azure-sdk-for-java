import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.core.credential.TokenCredential;

public class AzureCliCredentialSample {
    public void azureCliCredentialsCodeSnippets() {
        TokenCredential AzureCliCredentialInstance = new AzureCliCredentialBuilder().build();
    }
}



