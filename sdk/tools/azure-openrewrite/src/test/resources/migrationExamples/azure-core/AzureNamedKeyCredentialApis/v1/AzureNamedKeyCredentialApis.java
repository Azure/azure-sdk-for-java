import com.azure.core.credential.AzureNamedKey;
import com.azure.core.credential.AzureNamedKeyCredential;

public class AzureNamedKeyCredentialApis {
    public static void main(String... args) {
        AzureNamedKeyCredential credential = new AzureNamedKeyCredential("name", "key");
        AzureNamedKey azureNamedKey = credential.getAzureNamedKey();
        AzureNamedKeyCredential newCredential = credential.update("newName", "newKey");
    }
}
