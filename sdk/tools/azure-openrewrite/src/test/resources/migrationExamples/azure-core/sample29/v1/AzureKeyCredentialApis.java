import com.azure.core.credential.AzureKeyCredential;

public class AzureKeyCredentialApis {
    public static void main(String... args) {

        AzureKeyCredential keyCredential = new AzureKeyCredential("key");

        keyCredential = keyCredential.update("newKey");
    }
}
