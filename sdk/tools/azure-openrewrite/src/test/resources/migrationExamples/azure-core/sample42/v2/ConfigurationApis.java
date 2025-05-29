import com.azure.core.util.Configuration;

public class ConfigurationApis {
    public static void main(String... args) {
        Configuration config = Configuration.getGlobalConfiguration();

        config.get(Configuration.PROPERTY_HTTP_PROXY);
        config.get(Configuration.PROPERTY_HTTPS_PROXY);
        config.get(Configuration.PROPERTY_IDENTITY_ENDPOINT);
        config.get(Configuration.PROPERTY_IDENTITY_HEADER);
        config.get(Configuration.PROPERTY_NO_PROXY);
        config.get(Configuration.PROPERTY_MSI_ENDPOINT);
        config.get(Configuration.PROPERTY_MSI_SECRET);
        config.get(Configuration.PROPERTY_AZURE_SUBSCRIPTION_ID);
        config.get(Configuration.PROPERTY_AZURE_USERNAME);
        config.get(Configuration.PROPERTY_AZURE_PASSWORD);
        config.get(Configuration.PROPERTY_AZURE_CLIENT_ID);
        config.get(Configuration.PROPERTY_AZURE_CLIENT_SECRET);
        config.get(Configuration.PROPERTY_AZURE_TENANT_ID);
        config.get(Configuration.PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH);
        config.get(Configuration.PROPERTY_AZURE_CLIENT_CERTIFICATE_PASSWORD);
        config.get(Configuration.PROPERTY_AZURE_CLIENT_SEND_CERTIFICATE_CHAIN);
        config.get(Configuration.PROPERTY_AZURE_IDENTITY_DISABLE_CP1);
        config.get(Configuration.PROPERTY_AZURE_POD_IDENTITY_TOKEN_URL);
        config.get(Configuration.PROPERTY_AZURE_REGIONAL_AUTHORITY_NAME);
        config.get(Configuration.PROPERTY_AZURE_RESOURCE_GROUP);
        config.get(Configuration.PROPERTY_AZURE_CLOUD);
        config.get(Configuration.PROPERTY_AZURE_AUTHORITY_HOST);
        config.get(Configuration.PROPERTY_AZURE_TELEMETRY_DISABLED);
        config.get(Configuration.PROPERTY_AZURE_LOG_LEVEL);
        config.get(Configuration.PROPERTY_AZURE_HTTP_LOG_DETAIL_LEVEL);
        config.get(Configuration.PROPERTY_AZURE_TRACING_DISABLED);
        config.get(Configuration.PROPERTY_AZURE_TRACING_IMPLEMENTATION);
        config.get(Configuration.PROPERTY_AZURE_METRICS_DISABLED);
        config.get(Configuration.PROPERTY_AZURE_METRICS_IMPLEMENTATION);
        config.get(Configuration.PROPERTY_AZURE_REQUEST_RETRY_COUNT);
        config.get(Configuration.PROPERTY_AZURE_REQUEST_CONNECT_TIMEOUT);
        config.get(Configuration.PROPERTY_AZURE_REQUEST_WRITE_TIMEOUT);
        config.get(Configuration.PROPERTY_AZURE_REQUEST_RESPONSE_TIMEOUT);
        config.get(Configuration.PROPERTY_AZURE_REQUEST_READ_TIMEOUT);
        config.get(Configuration.PROPERTY_AZURE_HTTP_CLIENT_IMPLEMENTATION);

        String env_value = config.get("enviornment_variable_name");
        int int_value = config.get("enviornment_variable_name", 0);
        boolean bool_value = config.contains("enviornment_variable_name");
    }
}
