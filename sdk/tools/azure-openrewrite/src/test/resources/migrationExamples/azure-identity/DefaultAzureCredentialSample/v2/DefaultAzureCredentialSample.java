import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.identity.DefaultAzureCredentialBuilder;

public class DefaultAzureCredentialSample {

    public void defaultAzureCredentialCodeSnippets() {
        TokenCredential DefaultAzureCredentialInstance = new DefaultAzureCredentialBuilder().build();
        TokenCredential dacWithUserAssignedManagedIdentity
            = new DefaultAzureCredentialBuilder().managedIdentityClientId("<Managed-Identity-Client-Id").build();
    }
}



