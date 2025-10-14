import com.azure.core.credential.TokenCredential;
import com.azure.identity.DeviceCodeCredentialBuilder;

public class DeviceCodeCredentialSample {
    public void deviceCodeCredentialsCodeSnippets() {
        TokenCredential DeviceCodeCredentialInstance = new DeviceCodeCredentialBuilder().build();
    }
}



