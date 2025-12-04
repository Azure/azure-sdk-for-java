import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.identity.DeviceCodeCredentialBuilder;

public class DeviceCodeCredentialSample {
    public void deviceCodeCredentialsCodeSnippets() {
        TokenCredential DeviceCodeCredentialInstance = new DeviceCodeCredentialBuilder().build();
    }
}



