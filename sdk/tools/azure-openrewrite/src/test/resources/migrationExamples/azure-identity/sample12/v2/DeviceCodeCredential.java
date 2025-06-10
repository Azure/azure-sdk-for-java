import com.azure.identity.DeviceCodeCredentialBuilder;
import com.azure.core.credential.TokenCredential;

public class DeviceCodeCredential {
    public void deviceCodeCredentialsCodeSnippets() {
        TokenCredential deviceCodeCredential = new DeviceCodeCredentialBuilder().build();
    }
}
