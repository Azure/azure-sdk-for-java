import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.identity.WorkloadIdentityCredentialBuilder;

public class WorkloadIdentityCredentialSample {
    public void workloadIdentityCredentialCodeSnippets() {
        TokenCredential WorkloadIdentityCredentialInstance = new WorkloadIdentityCredentialBuilder().clientId("<clientID>")
            .tenantId("<tenantID>")
            .tokenFilePath("<token-file-path>")
            .build();
    }
}



