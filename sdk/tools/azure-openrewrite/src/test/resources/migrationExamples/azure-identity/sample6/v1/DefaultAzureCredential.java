import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.core.credential.TokenCredential;

public class DefaultAzureCredential {

    public void defaultAzureCredentialCodeSnippets() {
        TokenCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();
        TokenCredential dacWithUserAssignedManagedIdentity
            = new DefaultAzureCredentialBuilder().managedIdentityClientId("<Managed-Identity-Client-Id").build();
    }
}
