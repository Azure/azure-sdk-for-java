import com.azure.applicationconfig.ConfigurationClient;
import com.azure.applicationconfig.ConfigurationClientCredentials;
import com.azure.applicationconfig.models.ConfigurationSetting;
import com.azure.applicationconfig.models.SettingSelector;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Sample demonstrates how to use Azure Application Configuration to switch between "beta" and "production"
 * configuration sets.
 */
class ConfigurationSets {
    private static final String CONNECTION_STRING_KEY = "connection-string";
    private static final String KEY_VAULT_KEY = "key-vault";
    private static final String BETA = "beta";
    private static final String PRODUCTION = "production";

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException, InterruptedException {
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
        addConfigurations(client, BETA, "https://beta-storage.core.windows.net", betaKeyVault).block();
        addConfigurations(client, PRODUCTION, "https://production-storage.core.windows.net", productionKeyVault).block();

        // For your services, you can select settings with "beta" or "production" label, depending on what you want your
        // services to communicate with.
        fetchConfigurations(client, BETA);
        fetchConfigurations(client, PRODUCTION);
    }

    private static void fetchConfigurations(ConfigurationClient client, String configurationSet) throws InterruptedException {
        // Fetching all of the configuration values that are set for this label.
        SettingSelector options = new SettingSelector().label(configurationSet);

        List<ConfigurationSetting> settings = client.listSettings(options).collectList().block();
        for (ConfigurationSetting setting : settings) {
            System.out.println(String.format("%s %s: %s", setting.key(), setting.label(), setting.value()));
        }
    }

    /*
     * Adds the "" and CONNECTION_STRING_KEY configuration settings
     */
    private static Mono<Void> addConfigurations(ConfigurationClient client, String configurationSet, String storageEndpoint, KeyVaultConfiguration keyVaultInfo) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        ConfigurationSetting keyVaultSetting = new ConfigurationSetting()
                .key(KEY_VAULT_KEY)
                .label(configurationSet)
                .value(mapper.writeValueAsString(keyVaultInfo))
                .contentType("application/json");
        ConfigurationSetting endpointSetting = new ConfigurationSetting()
                .key(CONNECTION_STRING_KEY)
                .label(configurationSet)
                .value(storageEndpoint);

        return Flux.merge(client.addSetting(keyVaultSetting), client.addSetting(endpointSetting)).then();
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
}
