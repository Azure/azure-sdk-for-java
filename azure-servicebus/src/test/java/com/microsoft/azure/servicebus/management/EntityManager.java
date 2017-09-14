package com.microsoft.azure.servicebus.management;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

import javax.net.ssl.HttpsURLConnection;

import com.microsoft.azure.servicebus.primitives.ClientConstants;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.SASUtil;

public class EntityManager {
    private static final int ONE_BOX_HTTPS_PORT = 4446;
    private static final String API_VERSION_QUERY = "api-version=2017-04";
    private static final String PUT_METHOD = "PUT";
    private static final String DELETE_METHOD = "DELETE";
    private static final String USER_AGENT_HEADER_NAME = "User-Agent";
    private static final String AUTHORIZATION_HEADER_NAME = "Authorization";
    private static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";
    private static final String CONTENT_TYPE = "application/atom+xml";
    private static final Duration CONNECTION_TIMEOUT = Duration.ofMinutes(1);
    private static final Duration READ_TIMEOUT = Duration.ofMinutes(2);
    private static final int SAS_TOKEN_VALIDITY_IN_MINUTES = 5;
    private static final String USER_AGENT = String.format("%s/%s(%s)", ClientConstants.PRODUCT_NAME, ClientConstants.CURRENT_JAVACLIENT_VERSION, ClientConstants.PLATFORM_INFO);
    
    public static void createEntity(ConnectionStringBuilder namespaceConnectionStringBuilder, ResourceDescripton resourceDescription) throws ManagementException
    {
        try
        {
            URL entityURL = getManagementURL(namespaceConnectionStringBuilder, resourceDescription.getPath());
            String sasToken = getSASToken(namespaceConnectionStringBuilder, entityURL);
            sendManagementHttpRequest(PUT_METHOD, entityURL, sasToken, resourceDescription.getAtomXml());
        }
        catch(ManagementException me)
        {
            throw me;
        }
        catch(Exception e)
        {
            throw new ManagementException("EntityCreation failed.", e);
        }
    }
    
    public static void deleteEntity(ConnectionStringBuilder namespaceConnectionStringBuilder, String entityPath) throws ManagementException
    {
        try
        {
            URL entityURL = getManagementURL(namespaceConnectionStringBuilder, entityPath);
            String sasToken = getSASToken(namespaceConnectionStringBuilder, entityURL);
            sendManagementHttpRequest(DELETE_METHOD, entityURL, sasToken, null);
        }
        catch(ManagementException me)
        {
            throw me;
        }
        catch(Exception e)
        {
            throw new ManagementException("EntityCreation failed.", e);
        }
    }
    
    private static URL getManagementURL(ConnectionStringBuilder namespaceConnectionStringBuilder, String entityPath) throws URISyntaxException, MalformedURLException
    {
        URI endPointURI = namespaceConnectionStringBuilder.getEndpoint();
        URI httpURI = new URI("https", null, endPointURI.getHost(), getPortNumberFromHost(endPointURI.getHost()), "/"+entityPath, API_VERSION_QUERY, null);
        return httpURI.toURL();
    }
    
    private static void sendManagementHttpRequest(String httpMethod, URL url, String sasToken, String atomEntryString) throws IOException, ManagementException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException
    {
        // No special handling of TLS here. Assuming the server cert is already trusted
        HttpsURLConnection httpConnection = (HttpsURLConnection)url.openConnection();
        httpConnection.setConnectTimeout((int)CONNECTION_TIMEOUT.toMillis());
        httpConnection.setReadTimeout((int)READ_TIMEOUT.toMillis());
        httpConnection.setDoOutput(true);
        httpConnection.setRequestMethod(httpMethod);
        httpConnection.setRequestProperty(USER_AGENT_HEADER_NAME, USER_AGENT);
        httpConnection.setRequestProperty(AUTHORIZATION_HEADER_NAME, sasToken);
        httpConnection.setRequestProperty(CONTENT_TYPE_HEADER_NAME, CONTENT_TYPE);
        if(atomEntryString != null && !atomEntryString.isEmpty())
        {
            try(BufferedOutputStream bos = new BufferedOutputStream(httpConnection.getOutputStream()))
            {
                bos.write(atomEntryString.getBytes(StandardCharsets.UTF_8));
                bos.flush();
            }
        }
        
        int responseCode = httpConnection.getResponseCode();
        //System.out.println("Reponse code : " + responseCode);
        if(responseCode == HttpsURLConnection.HTTP_CREATED || responseCode == HttpsURLConnection.HTTP_ACCEPTED || responseCode == HttpsURLConnection.HTTP_OK)
        {
//            try(BufferedInputStream bis = new BufferedInputStream(httpConnection.getInputStream()))
//            {
//                    StringBuffer response = new StringBuffer();
//                    byte[] readBytes = new byte[1024];
//                    int numRead = bis.read(readBytes);
//                    while(numRead != -1)
//                    {
//                        response.append(new String(readBytes, 0, numRead));
//                        numRead = bis.read(readBytes);
//                    }
//                    
//                    System.out.println("response is :" + response.toString());
//            }
        }
        else
        {
            throw new ManagementException("Entity creation failed with response code:" + responseCode);
        }
    }
    
    private static String getSASToken(ConnectionStringBuilder namespaceConnectionStringBuilder, URL url ) throws InvalidKeyException
    {
        String sasToken = namespaceConnectionStringBuilder.getSharedAccessSignatureToken();
        if(sasToken == null)
        {
            sasToken = SASUtil.generateSharedAccessSignatureToken(namespaceConnectionStringBuilder.getSasKeyName(), namespaceConnectionStringBuilder.getSasKey(), url.toString(), SAS_TOKEN_VALIDITY_IN_MINUTES * 60);
        }
        
        return sasToken;
    }
    
    private static int getPortNumberFromHost(String host)
    {
        if(host.endsWith("onebox.windows-int.net"))
        {
            return ONE_BOX_HTTPS_PORT;
        }
        else
        {
            return -1;
        }
    }
}
