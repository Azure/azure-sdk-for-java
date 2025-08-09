import com.azure.core.credential.TokenCredential;
import com.azure.identity.WorkloadIdentityCredentialBuilder;

public class WorkloadIdentityCredentialSample {
    public void workloadIdentityCredentialCodeSnippets() {
        TokenCredential WorkloadIdentityCredentialInstance = new WorkloadIdentityCredentialBuilder().clientId("<clientID>")
            .tenantId("<tenantID>")
            .tokenFilePath("<token-file-path>")
            .build();
    }
}



