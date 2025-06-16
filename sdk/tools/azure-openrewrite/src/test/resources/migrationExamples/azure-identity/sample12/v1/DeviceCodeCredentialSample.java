import com.azure.identity.DeviceCodeCredentialBuilder;
import com.azure.core.credential.TokenCredential;

public class DeviceCodeCredentialSample {
    public void deviceCodeCredentialsCodeSnippets() {
        TokenCredential DeviceCodeCredentialInstance = new DeviceCodeCredentialBuilder().build();
    }
}



