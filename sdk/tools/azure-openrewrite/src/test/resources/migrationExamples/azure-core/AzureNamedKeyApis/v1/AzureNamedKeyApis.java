import com.azure.core.credential.AzureNamedKey;

public class AzureNamedKeyApis {
    public static void main(String... args) {

        AzureNamedKey azureNamedKey = null; // Assume this is initialized
        String keyName = azureNamedKey.getName();
        String keyValue = azureNamedKey.getKey();
    }
}
