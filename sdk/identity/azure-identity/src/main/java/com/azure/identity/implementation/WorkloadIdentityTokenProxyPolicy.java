package com.azure.identity.implementation;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import reactor.core.publisher.Mono;


public class WorkloadIdentityTokenProxyPolicy implements HttpPipelinePolicy {

    private static final ClientLogger LOGGER = new ClientLogger(WorkloadIdentityCustomProxyConfiguration.class);
    
    public static final String AZURE_KUBERNETES_TOKEN_PROXY = "AZURE_KUBERNETES_TOKEN_PROXY";
    public static final String AZURE_KUBERNETES_CA_FILE = "AZURE_KUBERNETES_CA_FILE";
    public static final String AZURE_KUBERNETES_CA_DATA = "AZURE_KUBERNETES_CA_DATA";
    public static final String AZURE_KUBERNETES_SNI_NAME = "AZURE_KUBERNETES_SNI_NAME";

    private byte[] caData;
    private String caFile;
    private String sniName;
    private URI tokenProxyUri;
    private HttpClient httpClient;
    private SSLContext sslContext;
    private byte[] lastCaBytes;


    WorkloadIdentityTokenProxyPolicy(IdentityClientOptions identityClientOptions) {
        Configuration configuration = identityClientOptions.getConfiguration() == null
            ? Configuration.getGlobalConfiguration()
            : identityClientOptions.getConfiguration();

        String tokenProxyUrl = configuration.get(AZURE_KUBERNETES_TOKEN_PROXY);
        String sniName = configuration.get(AZURE_KUBERNETES_SNI_NAME);
        String caFile = configuration.get(AZURE_KUBERNETES_CA_FILE);
        String caData = configuration.get(AZURE_KUBERNETES_CA_DATA);

        if (CoreUtils.isNullOrEmpty(tokenProxyUrl)) {
            if (!CoreUtils.isNullOrEmpty(sniName) 
                || !CoreUtils.isNullOrEmpty(caFile) 
                || !CoreUtils.isNullOrEmpty(caData)) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(
                    "AZURE_KUBERNETES_TOKEN_PROXY is not set but other custom endpoint-related environment variables are present"));
            }
            this.tokenProxyUri = null;
            this.caFile = null;
            this.caData = null;
            this.sniName = null;
            return;
        }

        if (!CoreUtils.isNullOrEmpty(caFile) && !CoreUtils.isNullOrEmpty(caData)) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "Only one of AZURE_KUBERNETES_CA_FILE or AZURE_KUBERNETES_CA_DATA can be set."));
        }

        this.tokenProxyUri = parseAndValidateProxyUrl(tokenProxyUrl);
        this.sniName = sniName;
        this.caFile = caFile;
        this.caData = CoreUtils.isNullOrEmpty(caData) ? null : caData.getBytes();
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        //HttpClient client = createHttpClient();
        HttpRequest request = context.getHttpRequest();
        HttpRequest proxyRequest = rewriteTokenRequestForProxy(request);
        context.setHttpRequest(proxyRequest);
        return next.process();
    }


    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        HttpRequest request = context.getHttpRequest();
        HttpRequest proxyRequest = rewriteTokenRequestForProxy(request);
        context.setHttpRequest(proxyRequest);
        return next.processSync();
    }

    private HttpRequest rewriteTokenRequestForProxy(HttpRequest request) {
        try {
            URI originalUri = request.getUrl().toURI();
            String originalPath = originalUri.getRawPath();
            String originalQuery = originalUri.getRawQuery();

            String tokenProxyBase = tokenProxyUri.toString();
            if(!tokenProxyBase.endsWith("/")) tokenProxyBase += "/";
            URI combined = URI.create(tokenProxyBase).resolve(originalPath.startsWith("/") ? originalPath.substring(1) : originalPath);

            String combinedStr = combined.toString();
            if (originalQuery != null && !originalQuery.isEmpty()) {
                combinedStr += "?" + originalQuery;
            }

            URI newUri = URI.create(combinedStr);
            HttpRequest newRequest = new HttpRequest(request.getHttpMethod(), newUri.toURL());
            
            if (request.getHeaders() != null) {
                newRequest.setHeaders(request.getHeaders());
            }
            
            if (request.getBodyAsBinaryData() != null) {
                newRequest.setBody(request.getBodyAsBinaryData());
            }
                        
            return newRequest;

        } catch (Exception e) {
            throw new RuntimeException("Failed to rewrite token request for proxy", e);
        }
    }

    // private HttpClient createHttpClient() {
    //     if((caData == null || caData.length == 0) && (caFile == null || caFile.isEmpty())) {
    //         if(httpClient == null) {
    //             // httpClient = 
    //         }
    //     }
    //     if(caFile == null || caFile.isEmpty()) {
    //         // httpClient = 
    //     }
    //     throw new UnsupportedOperationException("Unimplemented method 'createHttpClient'");
    // }

    private SSLContext getSSLContext() {
        try {
            // If no CA override provide, use default
            if(CoreUtils.isNullOrEmpty(caFile) && (caData == null || caData.length == 0)) {
                if(sslContext == null) {
                    sslContext = SSLContext.getDefault();
                }
                return sslContext;
            }

            // If CA data provided, use it
            if(CoreUtils.isNullOrEmpty(caFile)) {
                if(sslContext == null) {
                    sslContext = createSslContextFromBytes(caData);
                }
                return sslContext;
            }

            // If CA file provided, read it (and re-read if it changes)
            Path path = Paths.get(caFile);
            if(!Files.exists(path)) {
                throw new IOException("CA File not found: " + caFile);
            }

            byte[] currentContent = Files.readAllBytes(path);

            if(currentContent.length == 0) {
                if(sslContext == null) {
                    throw new IOException("CA File " + caFile + " is empty.");
                }
                return sslContext;
            }

            if(sslContext == null || !Arrays.equals(currentContent, lastCaBytes)) {
                sslContext = createSslContextFromBytes(currentContent);
                lastCaBytes = currentContent;
            }

            return sslContext;

        } catch (Exception e) {
            throw new RuntimeException("Failed to create default SSLContext", e);
        }
    }

    // Create SSLContext from byte array containing PEM certificate data
    private SSLContext createSslContextFromBytes(byte[] certificateData) {
        try (InputStream inputStream = new ByteArrayInputStream(certificateData)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate caCert = (X509Certificate) cf.generateCertificate(inputStream);
            return createSslContext(caCert);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SSLContext from bytes", e);
        }
    }

    // Create SSLContext from a single X509Certificate
    private SSLContext createSslContext(X509Certificate caCert) {
        try {
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(null, null);
            keystore.setCertificateEntry("ca-cert", caCert);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keystore);

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);
            return context;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SSLContext", e);
        }
    }

    /**
     * Parses and validates the custom token proxy URL.
     *
     * @param endpoint The proxy endpoint URL string
     * @return Validated URI
     * @throws IllegalArgumentException if URL is invalid
     */
    private static URI parseAndValidateProxyUrl(String endpoint) {
        if (CoreUtils.isNullOrEmpty(endpoint)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Proxy endpoint cannot be null or empty"));
        }

        URI tokenProxy;
        try {
            tokenProxy = new URI(endpoint);
        } catch (URISyntaxException e) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "Failed to parse custom token proxy URL: " + endpoint, e));
        }

        if (!"https".equals(tokenProxy.getScheme())) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "Custom token endpoint must use https scheme, got: " + tokenProxy.getScheme()));
        }

        if (tokenProxy.getRawUserInfo() != null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "Custom token endpoint URL must not contain user info: " + endpoint));
        }

        if (tokenProxy.getRawQuery() != null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "Custom token endpoint URL must not contain a query: " + endpoint));
        }

        if (tokenProxy.getRawFragment() != null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "Custom token endpoint URL must not contain a fragment: " + endpoint));
        }

        if (tokenProxy.getRawPath() == null || tokenProxy.getRawPath().isEmpty()) {
            try {
                tokenProxy = new URI(tokenProxy.getScheme(), null, tokenProxy.getHost(), 
                    tokenProxy.getPort(), "/", null, null);
            } catch (URISyntaxException e) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                    "Failed to normalize proxy URL path", e));
            }
        }

        return tokenProxy;
    }

}