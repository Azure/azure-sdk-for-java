import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class DefaultAzureCredentialSample {

    public void defaultAzureCredentialCodeSnippets() {
        TokenCredential DefaultAzureCredentialInstance = new DefaultAzureCredentialBuilder().build();
        TokenCredential dacWithUserAssignedManagedIdentity
            = new DefaultAzureCredentialBuilder().managedIdentityClientId("<Managed-Identity-Client-Id").build();
    }
}



