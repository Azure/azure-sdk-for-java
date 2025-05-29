import io.clientcore.core.credentials.KeyCredential;

public class AzureKeyCredentialApis {
    public static void main(String... args) {

        KeyCredential keyCredential = new KeyCredential("key");

        keyCredential = keyCredential.update("newKey");
    }
}
