import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.identity.AzureCliCredentialBuilder;

public class AzureCliCredentialSample {
    public void azureCliCredentialsCodeSnippets() {
        TokenCredential AzureCliCredentialInstance = new AzureCliCredentialBuilder().build();
    }
}



