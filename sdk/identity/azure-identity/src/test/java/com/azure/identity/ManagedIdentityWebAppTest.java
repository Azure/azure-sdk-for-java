package com.azure.identity;

import com.azure.core.util.Configuration;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.identity.implementation.MSIToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class ManagedIdentityWebAppTest {
    private URL endpoint;

    @BeforeAll
    public void setup() throws MalformedURLException {
        Configuration configuration = Configuration.getGlobalConfiguration().clone();
        endpoint= new URL(String.format("https://%s.azurewebsites.net/test", configuration.get("IDENTITY_WEBAPP_NAME")));
    }

    @Test
    public void testWebApp() throws IOException {


            HttpURLConnection connection = (HttpURLConnection) endpoint.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Metadata", "true");
            connection.connect();

            Assertions.assertEquals(connection.getResponseCode(), 200);
            Assertions.assertEquals(getResponseBody(connection),
                "Successfully acquired a token from ManagedIdentityCredential");
    }

    private String getResponseBody(HttpURLConnection connection) {

        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
            StringBuilder sb = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failure in parsing response body", e);
        }
    }
}
