import com.azure.identity.AzureDeveloperCliCredentialBuilder;
import com.azure.core.credential.TokenCredential;

public class AzureDeveloperCliCredentialSample {
    public void azureDeveloperCliCredentialCodeSnippets() {
        TokenCredential azureDevCliCredential = new AzureDeveloperCliCredentialBuilder().build();
    }
}

