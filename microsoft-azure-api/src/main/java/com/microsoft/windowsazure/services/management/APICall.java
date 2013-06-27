/*
 * 
 * The author contributes this code to the public domain,
 * retaining no rights and incurring no responsibilities for its use in whole or in part.
 */

package com.microsoft.windowsazure.services.management;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;

import javax.net.ssl.SSLContext;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

public class APICall {

    private final String url;

    private final SSLContext sslContext;

    public static final String SERIVICE_VERSION_HEADER_KEY = "x-ms-version";

    public static final String SERIVICE_VERSION_HEADER_VALUE = "2013-03-01";

    public static final String SERVICE_CONTENT_TYPE_HEADER_KEY = "Content-Type";

    public static final String SERVICE_CONTENT_TYPE_HEADER_VALUE = "application/xml";

    public APICall(String subscription, SSLContext context) {
        // make this changeable in whatever way suits you.....
        url = MessageFormat.format("https://management.core.windows.net/{0}/services/hostedservices", subscription);
        sslContext = context;
    }

    public APICall(String subscription, ConnectionCredential cred) throws GeneralSecurityException, IOException {
        this(subscription, SSLContextFactory.createSSLContext(cred));
    }

    public String get() throws IOException, GeneralSecurityException {
        Builder b = prepWebResourceBuilder();
        ClientResponse response = b.get(ClientResponse.class);
        checkBadResponse(response);
        return response.getEntity(String.class);
    }

    public String post(String body) throws IOException, GeneralSecurityException {
        Builder b = prepWebResourceBuilder();
        ClientResponse response = b.post(ClientResponse.class, body);
        checkBadResponse(response);
        return response.getEntity(String.class);
    }

    private Builder prepWebResourceBuilder() throws IOException, GeneralSecurityException {
        Client client = createClient();
        WebResource wr = client.resource(url);
        return wr.header(SERVICE_CONTENT_TYPE_HEADER_KEY, SERVICE_CONTENT_TYPE_HEADER_VALUE).header(
                SERIVICE_VERSION_HEADER_KEY, SERIVICE_VERSION_HEADER_VALUE);
    }

    private Client createClient() throws IOException, GeneralSecurityException {
        ClientConfig config = new DefaultClientConfig();
        config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new HTTPSProperties(null, sslContext));
        Client client = Client.create(config);
        return client;
    }

    void checkBadResponse(ClientResponse response) {
        if (response.getStatus() == 404) {
            System.out.println("Entity doesn't Exist");
        }
        else if (response.getStatus() != 200) {
            System.out.println("Something went wrong....");
            System.out.println(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
        }
    }

}
