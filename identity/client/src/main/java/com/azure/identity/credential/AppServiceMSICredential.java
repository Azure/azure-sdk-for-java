package com.azure.identity.credential;

import com.azure.core.configuration.BaseConfigurations;
import com.azure.core.configuration.Configuration;
import com.azure.core.configuration.ConfigurationManager;
import com.azure.core.implementation.serializer.SerializerAdapter;
import com.azure.core.implementation.serializer.SerializerEncoding;
import com.azure.core.implementation.serializer.jackson.JacksonAdapter;
import com.azure.identity.credential.msi.MSIResourceType;
import com.azure.identity.implementation.MSIToken;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class AppServiceMSICredential extends MSICredential {
    private String msiEndpoint;
    private String msiSecret;
    private final SerializerAdapter adapter = JacksonAdapter.createDefaultSerializerAdapter();

    AppServiceMSICredential() {
        super();
        Configuration configuration = ConfigurationManager.getConfiguration();
        if (configuration.contains(BaseConfigurations.MSI_ENDPOINT)) {
            msiEndpoint = configuration.get(BaseConfigurations.MSI_ENDPOINT);
        }
        if (configuration.contains(BaseConfigurations.MSI_SECRET)) {
            msiSecret = configuration.get(BaseConfigurations.MSI_SECRET);
        }
    }

    @Override
    public final MSIResourceType resourceType() {
        return MSIResourceType.APP_SERVICE;
    }

    /**
     * @return the endpoint from which token needs to be retrieved.
     */
    public String msiEndpoint() {
        return this.msiEndpoint;
    }
    /**
     * @return the secret to use to retrieve the token.
     */
    public String msiSecret() {
        return this.msiSecret;
    }

    public AppServiceMSICredential msiEndpoint(String msiEndpoint) {
        this.msiEndpoint = msiEndpoint;
        return this;
    }

    public AppServiceMSICredential msiSecret(String msiSecret) {
        this.msiSecret = msiSecret;
        return this;
    }

    @Override
    public Mono<String> getTokenAsync(String resource) {
        return Mono.fromSupplier(() -> {
            HttpURLConnection connection = null;
            try {
                String urlString = String.format("%s?resource=%s&api-version=2017-09-01", this.msiEndpoint, resource);
                URL url = new URL(urlString);
                InputStream stream = null;

                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setRequestProperty("Secret", this.msiSecret);
                connection.setRequestProperty("Metadata", "true");

                connection.connect();

                Scanner s = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8.name()).useDelimiter("\\A");
                String result = s.hasNext() ? s.next() : "";

                MSIToken msiToken = adapter.deserialize(result, MSIToken.class, SerializerEncoding.JSON);
                return msiToken.accessToken();
            } catch (IOException e) {
                throw Exceptions.propagate(e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }
}
