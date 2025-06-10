import com.azure.identity.WorkloadIdentityCredentialBuilder;
import com.azure.core.credential.TokenCredential;

public class WorkloadIdentityCredential {
    public void workloadIdentityCredentialCodeSnippets() {
        TokenCredential workloadIdentityCredential = new WorkloadIdentityCredentialBuilder().clientId("<clientID>")
            .tenantId("<tenantID>")
            .tokenFilePath("<token-file-path>")
            .build();
    }
}
