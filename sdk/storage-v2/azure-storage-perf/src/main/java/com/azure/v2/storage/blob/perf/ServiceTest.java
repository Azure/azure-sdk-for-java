// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.storage.blob.perf;

import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;
import com.azure.v2.storage.blob.AzureBlobStorageBuilder;
import com.azure.v2.storage.blob.BlobClient;
import com.azure.v2.storage.blob.BlockBlobClient;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.client.JdkHttpClientBuilder;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpCredentialPolicy;
import io.clientcore.core.http.pipeline.HttpPipelineNextPolicy;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.utils.SharedExecutorService;
import io.clientcore.http.netty4.NettyHttpClientBuilder;
import io.clientcore.http.okhttp3.OkHttpHttpClientBuilder;
import org.conscrypt.Conscrypt;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {

    protected final BlobClient blobClient;
    protected final BlockBlobClient blockBlobClient;
    protected String sasQueryParams;

    private static final ConcurrentHashMap<String, Boolean> cache = new ConcurrentHashMap<>();

    public ServiceTest(TOptions options) {
        super(options);

        Configuration configuration = Configuration.getGlobalConfiguration().clone();
        String accountUrl = configuration.get("STORAGE_ACCOUNT_URL");
        if (CoreUtils.isNullOrEmpty(accountUrl)) {
            throw new IllegalStateException("Environment variable STORAGE_ACCOUNT_URL must be set to the account URL. "
                + "This is the basic 'https://<account name>.blob.core.windows.net' URL.");
        }

        this.sasQueryParams = configuration.get("STORAGE_SAS_QUERY_PARAMS");
        if (CoreUtils.isNullOrEmpty(this.sasQueryParams)) {
            throw new IllegalStateException("Environment variable STORAGE_SAS_QUERY_PARAMS must be set to the query "
                + "parameters that represent the SAS.");
        }

        String client = configuration.get("HTTP_CLIENT");
        if (CoreUtils.isNullOrEmpty(client)) {
            client = "default";
        }

        HttpClient httpClient;
        if (client.equals("netty")) {
            System.out.println("Configuring Netty");
            httpClient = new NettyHttpClientBuilder().build();
        } else if (client.equals("okhttp")) {
            System.out.println("Configuring OKHttp");
            httpClient = new OkHttpHttpClientBuilder().build();
        } else if (client.equals("noconfig")) {
            System.out.println("Configuring Default JDK HttpClient without Conscrypt");
            // Create a default JDK HttpClient without any specific configuration
            httpClient = new JdkHttpClientBuilder().executor(SharedExecutorService.getInstance()).build();
        } else {

            // Add Conscrypt as a security provider
            Security.insertProviderAt(Conscrypt.newProvider(), 1);

            //Create SSLContext with Conscrypt or default JSSE provider
            SSLContext sslContext = null;
            try {
                // Create SSLContext using the Conscrypt provider
                sslContext = SSLContext.getInstance("TLS", "Conscrypt");
                SSLContext.setDefault(sslContext);
                System.out.println("SSLContext created successfully with Conscrypt provider.");
            } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
                throw new RuntimeException("Failed to create SSLContext with Conscrypt.", e);
            }

            // Retrieve the default TrustManager
            X509TrustManager defaultTrustManager = null;
            try {
                defaultTrustManager = DefaultTrustManagerRetriever.getDefaultTrustManager();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // Wrap the default TrustManager with caching
            CachingTrustManager cachingTrustManager = new CachingTrustManager(defaultTrustManager);

            // Initialize the SSLContext with default TrustManagers
            try {
                sslContext.init(null, new javax.net.ssl.TrustManager[] { cachingTrustManager }, null);
            } catch (KeyManagementException e) {
                throw new RuntimeException(e);
            }

            // Configure SSLParameters for modern ciphers and session reuse
            SSLParameters sslParameters = new SSLParameters();

            // Set modern cipher suites (prioritize GCM ciphers for performance)
            String[] enabledCiphers = {
                "TLS_AES_128_GCM_SHA256",   // Modern cipher (TLS 1.3)
                "TLS_AES_256_GCM_SHA384",   // Modern cipher (TLS 1.3)
                "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256", // Fallback for TLS 1.2
                "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"    // Fallback for TLS 1.2
            };
            sslParameters.setCipherSuites(enabledCiphers);

            // Set TLS protocol versions (disable older versions)
            sslParameters.setProtocols(new String[] { "TLSv1.3", "TLSv1.2" });

            // Apply the SSLParameters to the SSLContext
            sslContext.getDefaultSSLParameters().setCipherSuites(enabledCiphers);
            sslContext.getDefaultSSLParameters().setProtocols(new String[] { "TLSv1.3", "TLSv1.2" });

            System.out.println("HttpClient configured with optimized TLS settings");

            System.out.println("Configuring Default JDK Http");
            httpClient = new JdkHttpClientBuilder().sslContext(sslContext).build();
        }

        SasCredentialPolicy sasCredentialPolicy = new SasCredentialPolicy(sasQueryParams);
        blobClient = new AzureBlobStorageBuilder().url(accountUrl)
            .addHttpPipelinePolicy(sasCredentialPolicy)
            .httpClient(httpClient)
            .buildBlobClient();

        blockBlobClient = new AzureBlobStorageBuilder().url(accountUrl)
            .addHttpPipelinePolicy(sasCredentialPolicy)
            .httpClient(httpClient)
            .buildBlockBlobClient();
    }

    private static final class SasCredentialPolicy extends HttpCredentialPolicy {
        private final String sasQueryParams;

        private SasCredentialPolicy(String sasQueryParams) {
            this.sasQueryParams = sasQueryParams;
        }

        @Override
        public Response<BinaryData> process(HttpRequest httpRequest, HttpPipelineNextPolicy next) {
            URI uri = httpRequest.getUri();
            String existingQuery = uri.getQuery();

            if (io.clientcore.core.utils.CoreUtils.isNullOrEmpty(existingQuery)) {
                existingQuery = sasQueryParams;
            } else {
                existingQuery += sasQueryParams;
            }

            httpRequest.setUri(uri.getScheme() + "://" + uri.getHost() + uri.getPath() + "?" + existingQuery);
            return next.process();
        }
    }
}
