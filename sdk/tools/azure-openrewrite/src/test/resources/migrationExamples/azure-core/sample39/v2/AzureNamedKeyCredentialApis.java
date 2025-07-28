import io.clientcore.core.credentials.NamedKey;
import io.clientcore.core.credentials.NamedKeyCredential;

public class AzureNamedKeyCredentialApis {
    public static void main(String... args) {
        NamedKeyCredential credential = new NamedKeyCredential("name", "key");
        NamedKey azureNamedKey = credential.getNamedKey();
        NamedKeyCredential newCredential = credential.update("newName", "newKey");
    }
}
