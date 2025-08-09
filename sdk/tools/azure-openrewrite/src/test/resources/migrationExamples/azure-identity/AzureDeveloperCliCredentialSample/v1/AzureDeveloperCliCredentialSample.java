import com.azure.core.credential.TokenCredential;
import com.azure.identity.AzureDeveloperCliCredentialBuilder;

public class AzureDeveloperCliCredentialSample {
    public void azureDeveloperCliCredentialCodeSnippets() {
        TokenCredential azureDevCliCredential = new AzureDeveloperCliCredentialBuilder().build();
    }
}

