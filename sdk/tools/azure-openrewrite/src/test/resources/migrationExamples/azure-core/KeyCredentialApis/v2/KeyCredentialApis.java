import io.clientcore.core.credentials.KeyCredential;

public class KeyCredentialApis {
    public static void main(String... args) {

        KeyCredential keyCredential = new KeyCredential("key");
        String key = keyCredential.getKey();
        keyCredential = keyCredential.update("newKey");
    }
}
