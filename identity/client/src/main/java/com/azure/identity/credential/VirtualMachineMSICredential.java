package com.azure.identity.credential;

import com.azure.core.implementation.serializer.SerializerAdapter;
import com.azure.core.implementation.serializer.SerializerEncoding;
import com.azure.core.implementation.serializer.jackson.JacksonAdapter;
import com.azure.identity.credential.msi.MSIResourceType;
import com.azure.identity.credential.msi.VirtualMachineMSITokenSource;
import com.azure.identity.implementation.MSIToken;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class VirtualMachineMSICredential extends MSICredential {
    //
    private final List<Integer> retrySlots = new ArrayList<>();
    private static final Random RANDOM = new Random();
    private final SerializerAdapter adapter = JacksonAdapter.createDefaultSerializerAdapter();

    private VirtualMachineMSITokenSource tokenSource = VirtualMachineMSITokenSource.IMDS_ENDPOINT;
    private String objectId;
    private String clientId;
    private String identityId;
    private int msiPort = 50342;
    private int maxRetry;
    private static final int MAX_RETRY_DEFAULT_LIMIT = 20;

    VirtualMachineMSICredential() {
        super();
        this.maxRetry = MAX_RETRY_DEFAULT_LIMIT;
        // Simplified variant of https://en.wikipedia.org/wiki/Exponential_backoff
        for (int x = 0; x < this.maxRetry; x++) {
            this.retrySlots.add(500 * ((2 << 1) - 1) / 1000);
        }
    }

    @Override
    public final MSIResourceType resourceType() {
        return MSIResourceType.VIRTUAL_MACHINE;
    }

    /**
     * @return the token retrieval source (either MSI extension running in VM or IMDS service).
     */
    public VirtualMachineMSITokenSource tokenSource() {
        return this.tokenSource;
    }
    /**
     * @return the principal id of user assigned or system assigned identity.
     */
    public String objectId() {
        return this.objectId;
    }
    /**
     * @return the client id of user assigned or system assigned identity.
     */
    public String clientId() {
        return this.clientId;
    }
    /**
     * @return the ARM resource id of the user assigned identity resource.
     */
    public String identityId() {
        return this.identityId;
    }
    /**
     * @return the port of token retrieval service running in the extension.
     */
    public int msiPort() {
        return this.msiPort;
    }

    /**
     * @return the maximum retries allowed.
     */
    public int maxRetry() {
        return this.maxRetry;
    }

    /**
     * Specifies the token retrieval source.
     *
     * @param tokenSource the source of token
     *
     * @return VirtualMachineMSICredential
     */
    public VirtualMachineMSICredential tokenSource(VirtualMachineMSITokenSource tokenSource) {
        this.tokenSource = tokenSource;
        return this;
    }

    /**
     * specifies the principal id of user assigned or system assigned identity.
     *
     * @param objectId the object (principal) id
     * @return VirtualMachineMSICredential
     */
    public VirtualMachineMSICredential objectId(String objectId) {
        this.objectId = objectId;
        return this;
    }

    /**
     * Specifies the client id of user assigned or system assigned identity.
     *
     * @param clientId the client id
     * @return VirtualMachineMSICredential
     */
    public VirtualMachineMSICredential clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * Specifies the ARM resource id of the user assigned identity resource.
     *
     * @param identityId the identity ARM id
     * @return VirtualMachineMSICredential
     */
    public VirtualMachineMSICredential identityId(String identityId) {
        this.identityId = identityId;
        return this;
    }

    /**
     * Specifies the port of token retrieval msi extension service.
     *
     * @param msiPort the port
     * @return VirtualMachineMSICredential
     */
    public VirtualMachineMSICredential msiPort(int msiPort) {
        this.msiPort = msiPort;
        return this;
    }

    /**
     * Specifies the the maximum retries allowed.
     *
     * @param maxRetry max retry count
     * @return VirtualMachineMSICredential
     */
    public VirtualMachineMSICredential maxRetry(int maxRetry) {
        this.maxRetry = maxRetry;
        return this;
    }

    @Override
    public Mono<MSIToken> authenticateAsync(String resource) {
        if (this.tokenSource == VirtualMachineMSITokenSource.MSI_EXTENSION) {
            return Mono.fromCallable(() -> this.getTokenForVirtualMachineFromMSIExtension(resource));
        } else {
            return Mono.fromCallable(() -> this.getTokenFromIDMSWithRetry(resource));
        }
    }

    private MSIToken getTokenForVirtualMachineFromMSIExtension(String tokenAudience) throws IOException {
        URL url = new URL(String.format("http://localhost:%d/oauth2/token", this.msiPort));
        String postData = String.format("resource=%s", tokenAudience);
        if (this.objectId != null) {
            postData += String.format("&object_id=%s", this.objectId);
        } else if (this.clientId != null) {
            postData += String.format("&client_id=%s", this.clientId);
        } else if (this.identityId != null) {
            postData += String.format("&msi_res_id=%s", this.identityId);
        }
        HttpURLConnection connection = null;
        OutputStreamWriter wr = null;

        try {
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            connection.setRequestProperty("Metadata", "true");
            connection.setRequestProperty("Content-Length", Integer.toString(postData.length()));
            connection.setDoOutput(true);

            connection.connect();

            wr = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8);
            wr.write(postData);
            wr.flush();

            Scanner s = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8.name()).useDelimiter("\\A");
            String result = s.hasNext() ? s.next() : "";

            return adapter.deserialize(result, MSIToken.class, SerializerEncoding.JSON);
        } finally {
            if (wr != null) {
                wr.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private MSIToken getTokenFromIDMSWithRetry(String tokenAudience) throws IOException {
        StringBuilder payload = new StringBuilder();
        final int imdsUpgradeTimeInMs = 70 * 1000;

        //
        try {
            payload.append("api-version");
            payload.append("=");
            payload.append(URLEncoder.encode("2018-02-01", "UTF-8"));
            payload.append("&");
            payload.append("resource");
            payload.append("=");
            payload.append(URLEncoder.encode(tokenAudience, "UTF-8"));
            if (this.objectId != null) {
                payload.append("&");
                payload.append("object_id");
                payload.append("=");
                payload.append(URLEncoder.encode(this.objectId, "UTF-8"));
            } else if (this.clientId != null) {
                payload.append("&");
                payload.append("client_id");
                payload.append("=");
                payload.append(URLEncoder.encode(this.clientId, "UTF-8"));
            } else if (this.identityId != null) {
                payload.append("&");
                payload.append("msi_res_id");
                payload.append("=");
                payload.append(URLEncoder.encode(this.identityId, "UTF-8"));
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        int retry = 1;
        while (retry <= maxRetry) {
            URL url = new URL(String.format("http://169.254.169.254/metadata/identity/oauth2/token?%s", payload.toString()));
            //
            HttpURLConnection connection = null;
            //
            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Metadata", "true");
                connection.connect();

                Scanner s = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8.name()).useDelimiter("\\A");
                String result = s.hasNext() ? s.next() : "";

                return adapter.deserialize(result, MSIToken.class, SerializerEncoding.JSON);
            } catch (IOException exception) {
                if (connection == null) {
                    throw new RuntimeException(String.format("Could not connect to the url: %s.", url), exception);
                }
                int responseCode = connection.getResponseCode();
                if (responseCode == 410 || responseCode == 429 || responseCode == 404 || (responseCode >= 500 && responseCode <= 599)) {
                    int retryTimeoutInMs = retrySlots.get(RANDOM.nextInt(retry));
                    // Error code 410 indicates IMDS upgrade is in progress, which can take up to 70s
                    //
                    retryTimeoutInMs = (responseCode == 410 && retryTimeoutInMs < imdsUpgradeTimeInMs) ? imdsUpgradeTimeInMs : retryTimeoutInMs;
                    retry++;
                    if (retry > maxRetry) {
                        break;
                    } else {
                        sleep(retryTimeoutInMs);
                    }
                } else {
                    throw new RuntimeException("Couldn't acquire access token from IMDS, verify your objectId, clientId or msiResourceId", exception);
                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
        //
        if (retry > maxRetry) {
            throw new RuntimeException(String.format("MSI: Failed to acquire tokens after retrying %s times", maxRetry));
        }
        return null;
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }
}
