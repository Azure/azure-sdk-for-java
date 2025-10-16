import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.identity.AzureDeveloperCliCredentialBuilder;

public class AzureDeveloperCliCredentialSample {
    public void azureDeveloperCliCredentialCodeSnippets() {
        TokenCredential azureDevCliCredential = new AzureDeveloperCliCredentialBuilder().build();
    }
}

