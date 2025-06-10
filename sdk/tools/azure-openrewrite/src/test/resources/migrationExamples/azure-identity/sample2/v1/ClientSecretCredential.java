
import java.net.InetSocketAddress;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.ProxyOptions.Type;
import com.azure.identity.ClientSecretCredentialBuilder;

public class ClientSecretCredential {

    private String tenantId = System.getenv("AZURE_TENANT_ID");
    private String clientId = System.getenv("AZURE_CLIENT_ID");
    private String clientSecret = System.getenv("AZURE_CLIENT_SECRET");
    
    public void clientSecretCredentialCodeSnippets() {
        // BEGIN: com.azure.identity.credential.clientsecretcredential.construct
        TokenCredential clientSecretCredential = new ClientSecretCredentialBuilder().tenantId(tenantId)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .build();
        // END: com.azure.identity.credential.clientsecretcredential.construct

        // BEGIN: com.azure.identity.credential.clientsecretcredential.constructwithproxy
        TokenCredential secretCredential;
        // END: com.azure.identity.credential.clientsecretcredential.constructwithproxy
        secretCredential = new ClientSecretCredentialBuilder().tenantId(tenantId)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .proxyOptions(new ProxyOptions(Type.HTTP, new InetSocketAddress("10.21.32.43", 5465)))
                .build();
    }

}
