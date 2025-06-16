import com.azure.v2.identity.AzureDeveloperCliCredentialBuilder;
import com.azure.v2.core.credentials.TokenCredential;

public class AzureDeveloperCliCredentialSample {
    public void azureDeveloperCliCredentialCodeSnippets() {
        TokenCredential azureDevCliCredential = new AzureDeveloperCliCredentialBuilder().build();
    }
}

