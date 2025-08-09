import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientCertificateCredentialBuilder;
import java.io.ByteArrayInputStream;

public class ClientCertificateCredentialSample {

    private String tenantId = System.getenv("AZURE_TENANT_ID");

    private String clientId = System.getenv("AZURE_CLIENT_ID");
    private String clientSecret = System.getenv("AZURE_CLIENT_SECRET");
    private String serviceConnectionId = System.getenv("SERVICE_CONNECTION_ID");

    public void clientCertificateCredentialCodeSnippets() {
        TokenCredential ClientCertificateCredentialInstance = new ClientCertificateCredentialBuilder().tenantId(tenantId)
            .clientId(clientId)
            .pemCertificate("<PATH-TO-PEM-CERTIFICATE>")
            .build();

        byte[] certificateBytes = new byte[0];

        ByteArrayInputStream certificateStream = new ByteArrayInputStream(certificateBytes);
        TokenCredential certificateCredentialWithStream = new ClientCertificateCredentialBuilder().tenantId(tenantId)
            .clientId(clientId)
            .pemCertificate(certificateStream)
            .build();

        TokenCredential certificateCredential = new ClientCertificateCredentialBuilder().tenantId(tenantId)
            .clientId(clientId)
            .build();
    }

}



