import com.azure.identity.ClientAssertionCredentialBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.ProxyOptions.Type;
import java.net.InetSocketAddress;

public class ClientAssertionCredential {

    private String tenantId = System.getenv("AZURE_TENANT_ID");
    private String clientId = System.getenv("AZURE_CLIENT_ID");

    public void clientAssertionCredentialCodeSnippets() {
        TokenCredential clientAssertionCredential = new ClientAssertionCredentialBuilder().tenantId(tenantId)
            .clientId(clientId)
            .clientAssertion(() -> "<Client-Assertion>")
            .build();

        TokenCredential assertionCredential = new ClientAssertionCredentialBuilder().tenantId(tenantId)
            .clientId(clientId)
            .clientAssertion(() -> "<Client-Assertion>")
            .proxyOptions(new ProxyOptions(Type.HTTP, new InetSocketAddress("10.21.32.43", 5465)))
            .build();
    }

}
