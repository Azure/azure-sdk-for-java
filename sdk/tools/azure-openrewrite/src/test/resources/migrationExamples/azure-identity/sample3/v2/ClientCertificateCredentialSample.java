import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.identity.ClientCertificateCredentialBuilder;

import java.io.ByteArrayInputStream;

public class ClientCertificateCredentialSample {

    private String tenantId = System.getenv("AZURE_TENANT_ID");

    private String clientId = System.getenv("AZURE_CLIENT_ID");
    private String clientSecret = System.getenv("AZURE_CLIENT_SECRET");
    private String serviceConnectionId = System.getenv("SERVICE_CONNECTION_ID");

    public void clientCertificateCredentialCodeSnippets() {
        // BEGIN: com.azure.identity.credential.ClientCertificateCredentialSample.construct
        TokenCredential ClientCertificateCredentialInstance = new ClientCertificateCredentialBuilder().tenantId(tenantId)
            .clientId(clientId)
            .clientCertificate("<PATH-TO-PEM-CERTIFICATE>")
            .build();
        // END: com.azure.identity.credential.ClientCertificateCredentialSample.construct

        byte[] certificateBytes = new byte[0];

        // BEGIN: com.azure.identity.credential.ClientCertificateCredentialSample.constructWithStream
        ByteArrayInputStream certificateStream = new ByteArrayInputStream(certificateBytes);
        TokenCredential certificateCredentialWithStream = new ClientCertificateCredentialBuilder().tenantId(tenantId)
            .clientId(clientId)
            .clientCertificate(certificateStream)
            .build();
        // END: com.azure.identity.credential.ClientCertificateCredentialSample.constructWithStream

        // BEGIN: com.azure.identity.credential.ClientCertificateCredentialSample.constructwithproxy
        TokenCredential certificateCredential = new ClientCertificateCredentialBuilder().tenantId(tenantId)
            .clientId(clientId)
            .build();
        // END: com.azure.identity.credential.ClientCertificateCredentialSample.constructwithproxy
    }

}



