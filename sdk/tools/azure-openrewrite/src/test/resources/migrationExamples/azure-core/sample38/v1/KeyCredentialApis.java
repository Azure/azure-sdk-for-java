import com.azure.core.credential.KeyCredential;

public class KeyCredentialApis {
    public static void main(String... args) {

        KeyCredential credential = new KeyCredential("key");
        String key = credential.getKey();
        KeyCredential newCredential = credential.update("newKey");
    }
}
