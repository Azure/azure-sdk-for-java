import com.azure.applicationconfig.ConfigurationClient;
import com.azure.applicationconfig.ConfigurationClientCredentials;
import com.azure.applicationconfig.models.ConfigurationSetting;
import com.azure.common.http.rest.Response;
import reactor.core.publisher.Mono;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Sample demonstrates how to add, get, and delete a configuration setting.
 */
public class HelloWorld {
    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException {
        // Retrieve the connection string from the configuration store.
        // You can get the string from your Azure portal.
        String connectionString = "endpoint={endpoint_value};id={id_value};secret={secret_value}";

        // Instantiate a client that will be used to call the service.
        ConfigurationClient client = ConfigurationClient.builder()
            .credentials(new ConfigurationClientCredentials(connectionString))
            .build();

        // Create a setting to be stored by the configuration service.
        ConfigurationSetting settingToAdd = new ConfigurationSetting().key("hello").value("world");

        // setSetting adds or updates a setting to Azure Application Configuration store.
        // Alternatively, you can call addSetting which only succeeds if the setting does not exist in the store. Or,
        // you can call updateSetting to update a setting that is already present in the store.
        Mono<Response<ConfigurationSetting>> setSetting = client.setSetting(settingToAdd);

        // Retrieve a stored setting by calling client.getSetting after setSetting completes.
        // When we retrieve the value of that configuration, we print it out and then delete it.
        // .block() is used to prevent the program from quitting before it is complete.
        setSetting.then(client.getSetting(settingToAdd.key())).map(response -> {
            ConfigurationSetting setting = response.value();
            System.out.println(String.format("Key: %s, Value: %s", setting.key(), setting.value()));

            return client.deleteSetting(setting.key());
        }).block();
    }
}
