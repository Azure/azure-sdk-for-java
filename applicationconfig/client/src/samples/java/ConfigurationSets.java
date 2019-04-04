import com.azure.applicationconfig.ConfigurationClient;
import com.azure.applicationconfig.ConfigurationClientCredentials;
import com.azure.applicationconfig.models.ConfigurationSetting;
import com.azure.applicationconfig.models.SettingSelector;
import com.azure.common.annotations.Beta;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Sample demonstrates how to use Azure Application Configuration to switch between "beta" and "production"
 * configuration sets.
 */
public class ConfigurationSets {
    private static final String CONNECTION_STRING_KEY = "connection-string";
    private static final String KEY_VAULT_KEY = "key-vault";
    private static final String BETA = "beta";
    private static final String PRODUCTION = "production";

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        // Retrieve the connection string from the configuration store.
        // You can get the string from your Azure portal.
        String connectionString = "endpoint={endpoint_value};id={id_value};secret={secret_value}";

        // Instantiate a client that will be used to call the service.
        ConfigurationClient client = ConfigurationClient.builder()
                .credentials(new ConfigurationClientCredentials(connectionString))
                .build();

        // Creating two sets of configuration, one for beta testing and one for production when we are ready to ship.
        KeyVaultConfiguration betaKeyVault = new KeyVaultConfiguration().endpointUri("https://beta-keyvault.vault.azure.net").secret("beta_secret");
        KeyVaultConfiguration productionKeyVault = new KeyVaultConfiguration().endpointUri("https://production-keyvault.vault.azure.net").secret("production_secret");

        // Adding both of those configuration sets to Azure App Configuration.
        addConfigurations(client, BETA, "https://beta-storage.core.windows.net", betaKeyVault, false).block();

        // For these production settings, we'll put a lock on them, so that they cannot be modified.
        addConfigurations(client, PRODUCTION, "https://production-storage.core.windows.net", productionKeyVault, true).block();

        // For your services, you can select settings with "beta" or "production" label, depending on what you want your
        // services to communicate with.
        // The sample below fetches all of the "beta" settings.
        SettingSelector selector = new SettingSelector().label(BETA);
        List<ConfigurationSetting> settings = client.listSettings(selector).collectList().block();

        for (ConfigurationSetting setting : settings) {
            System.out.println("Key: " + setting.key());
            if ("application/json".equals(setting.contentType())) {
                KeyVaultConfiguration kv = mapper.readValue(setting.value(), KeyVaultConfiguration.class);
                System.out.println("Value: " + kv.toString());
            } else {
                System.out.println("Value: " + setting.value());
            }
        }

        // Unlock the production settings and delete all settings afterwards.
        for (String set : new String[]{BETA, PRODUCTION}) {
            client.unlockSetting(new ConfigurationSetting().key(KEY_VAULT_KEY).label(set))
                    .map(config -> client.deleteSetting(config.value()))
                    .block();
        }
    }

    /*
     * Adds the "connection-string" and "key-vault" configuration settings
     */
    private static Mono<Void> addConfigurations(ConfigurationClient client, String configurationSet,
                                                String storageEndpoint, KeyVaultConfiguration keyVaultInfo, boolean lockSettings) throws JsonProcessingException {
        ConfigurationSetting endpointSetting = new ConfigurationSetting()
                .key(CONNECTION_STRING_KEY)
                .label(configurationSet)
                .value(storageEndpoint);
        ConfigurationSetting keyVaultSetting = new ConfigurationSetting()
                .key(KEY_VAULT_KEY)
                .label(configurationSet)
                .value(mapper.writeValueAsString(keyVaultInfo))
                .contentType("application/json");

        return Flux.merge(client.addSetting(keyVaultSetting), client.addSetting(endpointSetting))
                .flatMap(added -> lockSettings
                        ? client.lockSetting(added.value())
                        : Flux.just(added))
                .then();
    }
}

class KeyVaultConfiguration {
    @JsonProperty("endpointUri")
    private String endpointUri;

    @JsonProperty("secret")
    private String secret;

    String endpointUri() {
        return endpointUri;
    }

    KeyVaultConfiguration endpointUri(String endpointUri) {
        this.endpointUri = endpointUri;
        return this;
    }

    String secret() {
        return secret;
    }

    KeyVaultConfiguration secret(String secret) {
        this.secret = secret;
        return this;
    }

    @Override
    public String toString() {
        return "Endpoint: " + endpointUri() + ", Secret: " + secret();
    }
}
