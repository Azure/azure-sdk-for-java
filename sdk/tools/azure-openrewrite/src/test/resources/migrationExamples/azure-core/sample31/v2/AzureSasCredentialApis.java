import com.azure.v2.core.credentials.AzureSasCredential;

public class AzureSasCredentialApis {
    public static void main(String... args) {

        AzureSasCredential azureSasCredential = new AzureSasCredential("sas");
        AzureSasCredential azureSasCredential2 = new AzureSasCredential("sas", (string) -> {;
            return string;
        });
        String sasToken = azureSasCredential.getSignature();
        azureSasCredential2 = azureSasCredential2.update("newSas");
    }
}
