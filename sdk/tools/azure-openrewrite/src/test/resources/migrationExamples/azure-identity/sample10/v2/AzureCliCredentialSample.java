import com.azure.v2.identity.AzureCliCredentialBuilder;
import com.azure.v2.core.credentials.TokenCredential;

public class AzureCliCredentialSample {
    public void azureCliCredentialsCodeSnippets() {
        TokenCredential AzureCliCredentialInstance = new AzureCliCredentialBuilder().build();
    }
}



