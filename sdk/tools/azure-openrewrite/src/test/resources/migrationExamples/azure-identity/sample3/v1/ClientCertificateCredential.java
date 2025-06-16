import java.io.ByteArrayInputStream;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientCertificateCredentialBuilder;

public class ClientCertificateCredential {

    private String tenantId = System.getenv("AZURE_TENANT_ID");

    private String clientId = System.getenv("AZURE_CLIENT_ID");
    private String clientSecret = System.getenv("AZURE_CLIENT_SECRET");
    private String serviceConnectionId = System.getenv("SERVICE_CONNECTION_ID");

    public void clientCertificateCredentialCodeSnippets() {
        // BEGIN: com.azure.identity.credential.clientcertificatecredential.construct
        TokenCredential clientCertificateCredential = new ClientCertificateCredentialBuilder().tenantId(tenantId)
            .clientId(clientId)
            .pemCertificate("<PATH-TO-PEM-CERTIFICATE>")
            .build();
        // END: com.azure.identity.credential.clientcertificatecredential.construct

        byte[] certificateBytes = new byte[0];

        // BEGIN: com.azure.identity.credential.clientcertificatecredential.constructWithStream
        ByteArrayInputStream certificateStream = new ByteArrayInputStream(certificateBytes);
        TokenCredential certificateCredentialWithStream = new ClientCertificateCredentialBuilder().tenantId(tenantId)
            .clientId(clientId)
            .pemCertificate(certificateStream)
            .build();
        // END: com.azure.identity.credential.clientcertificatecredential.constructWithStream

        // BEGIN: com.azure.identity.credential.clientcertificatecredential.constructwithproxy
        TokenCredential certificateCredential = new ClientCertificateCredentialBuilder().tenantId(tenantId)
            .clientId(clientId)
            .build();
        // END: com.azure.identity.credential.clientcertificatecredential.constructwithproxy
    }
    
}
