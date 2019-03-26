/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.auth.credentials;

import com.azure.common.annotations.Beta;
import com.azure.common.implementation.serializer.SerializerEncoding;
import com.azure.common.implementation.serializer.jackson.JacksonAdapter;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Managed Service Identity token based credentials for use with a REST Service Client.
 */
@Beta
public final class MSICredentials extends AzureTokenCredentials {
    //
    private final List<Integer> retrySlots = new ArrayList<>();
    private final Lock lock = new ReentrantLock();
    private final ConcurrentHashMap<String, MSIToken> cache = new ConcurrentHashMap<>();
    //
    private final JacksonAdapter adapter = new JacksonAdapter();
    private final MSIConfigurationForVirtualMachine configForVM;
    private final MSIConfigurationForAppService configForAppService;
    private final HostType hostType;
    private final int maxRetry;
    private static final int MAX_RETRY_DEFAULT_LIMIT = 20;
    /**
     * Creates MSICredentials for application running on MSI enabled virtual machine.
     *
     * @return MSICredentials
     */
    public static MSICredentials forVirtualMachine() {
        return new MSICredentials(new MSIConfigurationForVirtualMachine());
    }

    /**
     * Creates MSICredentials for application running on MSI enabled virtual machine.
     *
     * @param config the configuration to be used for token request.
     * @return MSICredentials
     */
    public static MSICredentials forVirtualMachine(MSIConfigurationForVirtualMachine config) {
        return new MSICredentials(config.clone());
    }

    /**
     * Creates MSICredentials for application running on MSI enabled app service.
     *
     * @return MSICredentials
     */
    public static MSICredentials forAppService() {
        return new MSICredentials(new MSIConfigurationForAppService());
    }

    /**
     * Creates MSICredentials for application running on MSI enabled app service.
     *
     * @param config the configuration to be used for token request.
     * @return MSICredentials
     */
    public static MSICredentials forAppService(MSIConfigurationForAppService config) {
        return new MSICredentials(config.clone());
    }

    private MSICredentials(MSIConfigurationForVirtualMachine config) {
        super(config.azureEnvironment(), null /** retrieving MSI token does not require tenant **/);
        this.configForVM = config;
        this.configForAppService = null;
        this.hostType = HostType.VIRTUAL_MACHINE;
        this.maxRetry = config.maxRetry() < 0 ? MAX_RETRY_DEFAULT_LIMIT : config.maxRetry();
        // Simplified variant of https://en.wikipedia.org/wiki/Exponential_backoff
        for (int x = 0; x < this.maxRetry; x++) {
            this.retrySlots.add(500 * ((2 << 1) - 1) / 1000);
        }
    }

    private MSICredentials(MSIConfigurationForAppService config) {
        super(config.azureEnvironment(), null /** retrieving MSI token does not require tenant **/);
        this.configForAppService = config;
        this.configForVM = null;
        this.hostType = HostType.APP_SERVICE;
        this.maxRetry = -1;
    }

    @Override
    public Mono<String> getToken(String tokenAudience) {
        switch (hostType) {
            case VIRTUAL_MACHINE:
                if (this.configForVM.tokenSource() == MSIConfigurationForVirtualMachine.MSITokenSource.MSI_EXTENSION) {
                    return Mono.fromCallable(() -> this.getTokenForVirtualMachineFromMSIExtension(tokenAudience == null ? this.configForVM.resource() : tokenAudience));
                } else {
                    return Mono.fromCallable(() -> this.getTokenForVirtualMachineFromIMDSEndpoint(tokenAudience == null ? this.configForVM.resource() : tokenAudience));
                }
            case APP_SERVICE:
                return Mono.fromCallable(() -> getTokenForAppService(tokenAudience));
            default:
                throw new IllegalArgumentException("unknown host type:" + hostType);
        }
    }

    private String getTokenForAppService(String tokenAudience) throws IOException {
        String urlString = String.format("%s?resource=%s&api-version=2017-09-01", this.configForAppService.msiEndpoint(), tokenAudience == null ? this.configForAppService.resource() : tokenAudience);
        URL url = new URL(urlString);
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Secret", this.configForAppService.msiSecret());
            connection.setRequestProperty("Metadata", "true");

            connection.connect();

            InputStream stream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"), 100);
            String result = reader.readLine();

            MSIToken msiToken = adapter.deserialize(result, MSIToken.class, SerializerEncoding.JSON);
            return msiToken.accessToken();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String getTokenForVirtualMachineFromMSIExtension(String tokenAudience) throws IOException {
        URL url = new URL(String.format("http://localhost:%d/oauth2/token", this.configForVM.msiPort()));
        String postData = String.format("resource=%s", tokenAudience);
        if (this.configForVM.objectId() != null) {
            postData += String.format("&object_id=%s", this.configForVM.objectId());
        } else if (this.configForVM.clientId() != null) {
            postData += String.format("&client_id=%s", this.configForVM.clientId());
        } else if (this.configForVM.identityId() != null) {
            postData += String.format("&msi_res_id=%s", this.configForVM.identityId());
        }
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            connection.setRequestProperty("Metadata", "true");
            connection.setRequestProperty("Content-Length", Integer.toString(postData.length()));
            connection.setDoOutput(true);

            connection.connect();

            OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
            wr.write(postData);
            wr.flush();

            InputStream stream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"), 100);
            String result = reader.readLine();

            MSIToken msiToken = adapter.deserialize(result, MSIToken.class, SerializerEncoding.JSON);
            return msiToken.accessToken();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String getTokenForVirtualMachineFromIMDSEndpoint(String tokenAudience) {
        MSIToken token = cache.get(tokenAudience);
        if (token != null && !token.isExpired()) {
            return token.accessToken();
        }
        lock.lock();
        try {
            token = cache.get(tokenAudience);
            if (token != null && !token.isExpired()) {
                return token.accessToken();
            }
            try {
                token = retrieveTokenFromIDMSWithRetry(tokenAudience);
                if (token != null) {
                    cache.put(tokenAudience, token);
                }
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
            return token.accessToken();
        } finally {
            lock.unlock();
        }
    }

    private MSIToken retrieveTokenFromIDMSWithRetry(String tokenAudience) throws IOException {
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
            if (this.configForVM.objectId() != null) {
                payload.append("&");
                payload.append("object_id");
                payload.append("=");
                payload.append(URLEncoder.encode(this.configForVM.objectId(), "UTF-8"));
            } else if (this.configForVM.clientId() != null) {
                payload.append("&");
                payload.append("client_id");
                payload.append("=");
                payload.append(URLEncoder.encode(this.configForVM.clientId(), "UTF-8"));
            } else if (this.configForVM.identityId() != null) {
                payload.append("&");
                payload.append("msi_res_id");
                payload.append("=");
                payload.append(URLEncoder.encode(this.configForVM.identityId(), "UTF-8"));
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
                InputStream stream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"), 100);
                String result = reader.readLine();
                return adapter.deserialize(result, MSIToken.class, SerializerEncoding.JSON);
            } catch (Exception exception) {
                int responseCode = connection.getResponseCode();
                if (responseCode == 410 || responseCode == 429 || responseCode == 404 || (responseCode >= 500 && responseCode <= 599)) {
                    int retryTimeoutInMs = retrySlots.get(new Random().nextInt(retry));
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

    /**
     * The host in which application is running.
     */
    private enum HostType {
        /**
         * indicate that host is an Azure virtual machine.
         */
        VIRTUAL_MACHINE,
        /**
         * indicate that host is an Azure app-service instance.
         */
        APP_SERVICE
    }
}