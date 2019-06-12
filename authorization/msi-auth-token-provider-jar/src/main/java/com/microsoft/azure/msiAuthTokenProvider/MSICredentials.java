/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.msiAuthTokenProvider;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Managed Service Identity token based credentials for use with a REST Service Client.
 */
public final class MSICredentials{
    public static final String DEFAULT_AZURE_MANAGEMENT_ENDPOINT = "https://management.core.windows.net/";
    //
    private final List<Integer> retrySlots = new ArrayList<>();
    //
    private final Lock lock = new ReentrantLock();

    private final ConcurrentHashMap<String, MSIToken> cache = new ConcurrentHashMap<>();
    private final MSIConfigurationForVirtualMachine configForVM;
    private final MSIConfigurationForAppService configForAppService;
    private final HostType hostType;
    private final int maxRetry;
    private final int customTimeout;
    private static final int MAX_RETRY_DEFAULT_LIMIT = 20;


    class ActiveDirectoryAuthentication {
        static final String AZURE_REST_MSI_URL = "http://169.254.169.254/metadata/identity/oauth2/token?api-version=2018-02-01";
        static final String ACCESS_TOKEN_IDENTIFIER = "\"access_token\":\"";
        static final String ACCESS_TOKEN_TYPE_IDENTIFIER = "\"token_type\":\"";
        static final String ACCESS_TOKEN_EXPIRES_IN_IDENTIFIER = "\"expires_in\":\"";
        static final String ACCESS_TOKEN_EXPIRES_ON_IDENTIFIER = "\"expires_on\":\"";
        static final String ACCESS_TOKEN_EXPIRES_ON_DATE_FORMAT = "M/d/yyyy h:mm:ss a X";
    }

    /**
     * This method checks if the env vars "MSI_ENDPOINT" and "MSI_SECRET" exist. If they do, we return the msi creds class for APP Svcs
     * otherwise we return one for VM
     *
     * @return MSICredentials
     */
    public static MSICredentials getMSICredentials() {
        return getMSICredentials(DEFAULT_AZURE_MANAGEMENT_ENDPOINT);
    }

    /**
     * This method checks if the env vars "MSI_ENDPOINT" and "MSI_SECRET" exist. If they do, we return the msi creds class for APP Svcs
     * otherwise we return one for VM
     *
     * @param managementEndpoint Management endpoint in Azure
     * @return MSICredentials
     */
    public static MSICredentials getMSICredentials(String managementEndpoint) {
        //check if we are running in a web app
        String websiteName = System.getenv("WEBSITE_SITE_NAME");

        if (websiteName != null && !websiteName.isEmpty()) {
            // We are in a web app...
            MSIConfigurationForAppService config = new MSIConfigurationForAppService(managementEndpoint);
            return forAppService(config);
        } else {
            //We are in a vm/container
            MSIConfigurationForVirtualMachine config = new MSIConfigurationForVirtualMachine(managementEndpoint);
            return forVirtualMachine(config);
        }
    }

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
        this.configForVM = config;
        this.configForAppService = null;
        this.hostType = HostType.VIRTUAL_MACHINE;
        this.maxRetry = config.maxRetry() < 0 ? MAX_RETRY_DEFAULT_LIMIT : config.maxRetry();
        this.customTimeout = config.customTimeout();
        // Simplified variant of https://en.wikipedia.org/wiki/Exponential_backoff
        for (int x = 0; x < this.maxRetry; x++) {
            this.retrySlots.add(500 * ((2 << x) - 1) / 1000);
        }
    }

    private MSICredentials(MSIConfigurationForAppService config) {
        this.configForAppService = config;
        this.configForVM = null;
        this.hostType = HostType.APP_SERVICE;
        this.maxRetry = -1;
        this.customTimeout = -1;
    }

    /**
     * Updates the client Id for the associated config (vm or app)
     * Specifying a null value will clear out the old value
     * @param clientId
     */
    public void updateClientId(String clientId) {
        if (configForVM != null) {
            configForVM.withClientId(clientId);
        } else {
            configForAppService.withClientId(clientId);
        }
    }

    /**
     * Updates the object Id for the associated config (vm or app)
     * Specifying a null value will clear out the old value
     * @param objectId
     */
    public void updateObjectId(String objectId) {
        if (configForVM != null) {
            configForVM.withObjectId(objectId);
        } else {
            configForAppService.withObjectId(objectId);
        }
    }

    public MSIToken getToken(String tokenAudience) throws IOException, AzureMSICredentialException{
        switch (hostType) {
            case VIRTUAL_MACHINE:
                    return this.getTokenForVirtualMachineFromIMDSEndpoint(tokenAudience == null ? this.configForVM.resource() : tokenAudience);
            case APP_SERVICE:
                return this.getTokenForAppService(tokenAudience);
            default:
                throw new IllegalArgumentException("unknown host type:" + hostType);
        }
    }

    private MSIToken getTokenForAppService(String tokenAudience) throws IOException, AzureMSICredentialException {
        String urlString = null;

        if (this.configForAppService.msiEndpoint() == null || this.configForAppService.msiEndpoint().isEmpty()) {
            //the web app does not have MSI set, return file not found
            throw new FileNotFoundException("Managed identity not found/configured");
        }

        if (this.configForAppService.msiClientId() != null && !this.configForAppService.msiClientId().isEmpty()) {
            urlString = String.format("%s?resource=%s&clientid=%s&api-version=2017-09-01", this.configForAppService.msiEndpoint(),
                    tokenAudience == null ? this.configForAppService.resource() : tokenAudience,
                    this.configForAppService.msiClientId());
        } else if (this.configForAppService.msiObjectId() != null && !this.configForAppService.msiObjectId().isEmpty()) {
            urlString = String.format("%s?resource=%s&objectid=%s&api-version=2017-09-01", this.configForAppService.msiEndpoint(),
                    tokenAudience == null ? this.configForAppService.resource() : tokenAudience,
                    this.configForAppService.msiObjectId());
        } else {
            urlString = String.format("%s?resource=%s&api-version=2017-09-01", this.configForAppService.msiEndpoint(),
                    tokenAudience == null ? this.configForAppService.resource() : tokenAudience);
        }
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

            return getMsiTokenFromResult(result, HostType.APP_SERVICE);
        } catch (IOException ioEx){
            if  (ioEx.getMessage().contains("Server returned HTTP response code: 400 for URL")) {
                throw new AzureMSICredentialException("Managed identity not found/configured", ioEx);
            } else throw ioEx;
        } catch (Exception e){
            if (e.getCause()!= null && e.getCause() instanceof SocketException && e.getCause().getMessage().contains("Permission denied: connect")) {
                throw new AzureMSICredentialException("Managed identity not found/configured", e);
            } else throw e;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private MSIToken getTokenForVirtualMachineFromIMDSEndpoint(String tokenAudience) throws AzureMSICredentialException {
        String tokenIdentifier = tokenAudience;

        String extraIdentifier = null;
        if (this.configForVM.objectId() != null)
        {
            extraIdentifier = configForVM.objectId();
        } else if (this.configForVM.clientId() != null) {
            extraIdentifier = configForVM.clientId();
        } else if (this.configForVM.identityId() != null) {
            extraIdentifier = configForVM.identityId();
        }

        if (extraIdentifier != null) {
            tokenIdentifier = tokenIdentifier + "#" + extraIdentifier;
        }

        MSIToken token = cache.get(tokenIdentifier);
        if (token != null && !token.isExpired()) {
            return token;
        }

        lock.lock();

        try {
            token = cache.get(tokenIdentifier);
            if (token != null && !token.isExpired()) {
                return token;
            }

            try {
                token = retrieveTokenFromIDMSWithRetry(tokenAudience);
                if (token != null) {
                    cache.put(tokenIdentifier, token);
                }
            } catch (IOException exception) {
                throw new AzureMSICredentialException(exception);
            }

            return token;
        } finally {
            lock.unlock();
        }
    }

    private MSIToken retrieveTokenFromIDMSWithRetry(String tokenAudience) throws AzureMSICredentialException, IOException {
        StringBuilder payload = new StringBuilder();
        final int imdsUpgradeTimeInMs = 70 * 1000;
        boolean hasTimedout = false;

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
            throw new AzureMSICredentialException(exception);
        }

        //A 0 custom timeout implies only 1 try... no more
        hasTimedout = this.customTimeout == 0;
        int retry = 1;
        while (retry <= maxRetry) {
            URL url = new URL(String.format("http://169.254.169.254/metadata/identity/oauth2/token?%s", payload.toString()));
            //
            HttpURLConnection connection = null;
            //
            long startTime = Calendar.getInstance().getTime().getTime();
            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Metadata", "true");
                connection.connect();
                InputStream stream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"), 100);
                String result = reader.readLine();
                return getMsiTokenFromResult(result, HostType.VIRTUAL_MACHINE);
            } catch (Exception exception) {
                int responseCode = connection.getResponseCode();
                if (responseCode == 410 || responseCode == 429 || responseCode == 404 || (responseCode >= 500 && responseCode <= 599)) {
                    if (hasTimedout) {
                        throw new AzureMSICredentialException("Couldn't acquire access token from IMDS within the specified timeout : " + this.customTimeout + " milliseconds");
                    }
                    int retryTimeoutInMs = retrySlots.get(new Random().nextInt(retry));
                    // Error code 410 indicates IMDS upgrade is in progress, which can take up to 70s
                    //
                    retryTimeoutInMs = (responseCode == 410 && retryTimeoutInMs < imdsUpgradeTimeInMs) ? imdsUpgradeTimeInMs : retryTimeoutInMs;
                    retry++;
                    if (retry > maxRetry) {
                        break;
                    } else {
                        hasTimedout = sleep(retryTimeoutInMs, startTime);
                    }
                } else {
                    throw new AzureMSICredentialException("Couldn't acquire access token from IMDS, verify your objectId, clientId or msiResourceId", exception);
                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
        //
        if (retry > maxRetry) {
            throw new AzureMSICredentialException(String.format("MSI: Failed to acquire tokens after retrying %s times", maxRetry));
        }
        return null;
    }

    /**
     * Sleep for timeToWait or time remaining until timeout reached.
     * @param timeToWaitinMs Time to wait in milliseconds
     * @param startTime Abcolute tim in milliseconds
     * @return true if we used the custom timeout.
     */
    private boolean sleep(int timeToWaitinMs, long startTime) {
        long timeToSleep = 0;

        if (this.customTimeout > -1) {
            //timeToSleep = ;
            long timeRemainingToTimeout = (startTime + this.customTimeout - Calendar.getInstance().getTime().getTime());
            timeRemainingToTimeout = (timeToWaitinMs < timeRemainingToTimeout) ? timeToWaitinMs : timeRemainingToTimeout;
            timeToSleep = (timeRemainingToTimeout > 0) ? timeRemainingToTimeout : 0;
        } else {
            timeToSleep = timeToWaitinMs;
        }

        sleep(timeToSleep);
        return (timeToSleep != timeToWaitinMs);
    }

    private static MSIToken getMsiTokenFromResult(String result, HostType hostType) throws AzureMSICredentialException{
        try {
            return new MSIToken(getTokenFromResult(result), getTokenTypeFromResult(result), getExpiryTimeFromResult(result, hostType));
        } catch (ParseException pe) {
            throw new AzureMSICredentialException(pe.getMessage(), pe);
        }
    }

    private static String getTokenFromResult(String result) {
        int startIndex_AT = result.indexOf(ActiveDirectoryAuthentication.ACCESS_TOKEN_IDENTIFIER)
                + ActiveDirectoryAuthentication.ACCESS_TOKEN_IDENTIFIER.length();

        return (result.substring(startIndex_AT, result.indexOf("\"", startIndex_AT + 1)));
    }

    private static String getTokenTypeFromResult(String result) {
        int startIndex_AT = result.indexOf(ActiveDirectoryAuthentication.ACCESS_TOKEN_TYPE_IDENTIFIER)
                + ActiveDirectoryAuthentication.ACCESS_TOKEN_TYPE_IDENTIFIER.length();

        return (result.substring(startIndex_AT, result.indexOf("\"", startIndex_AT + 1)));
    }

    private int getIndexInString(String sourceText, String subString) throws ParseException {
        int index = sourceText.indexOf(subString);

        if (index < 0) {
            throw new ParseException("Text to search not found in source text", 0);
        }

        return index;
    }

    private static Date getExpiryTimeFromResult(String result, HostType hostType) throws ParseException {
        Calendar cal = new Calendar.Builder().setInstant(new Date()).build();
        if (hostType == HostType.VIRTUAL_MACHINE) {
            int startIndex_ATX = result
                    .indexOf(ActiveDirectoryAuthentication.ACCESS_TOKEN_EXPIRES_IN_IDENTIFIER)
                    + ActiveDirectoryAuthentication.ACCESS_TOKEN_EXPIRES_IN_IDENTIFIER.length();
            String accessTokenExpiry = result.substring(startIndex_ATX,
                    result.indexOf("\"", startIndex_ATX + 1));
            cal.add(Calendar.SECOND, Integer.parseInt(accessTokenExpiry));
        } else {
            int startIndex_ATX = result
                    .indexOf(ActiveDirectoryAuthentication.ACCESS_TOKEN_EXPIRES_ON_IDENTIFIER)
                    + ActiveDirectoryAuthentication.ACCESS_TOKEN_EXPIRES_ON_IDENTIFIER.length();
            String accessTokenExpiry = result.substring(startIndex_ATX,
                    result.indexOf("\"", startIndex_ATX + 1));

            DateFormat df = new SimpleDateFormat(
                    ActiveDirectoryAuthentication.ACCESS_TOKEN_EXPIRES_ON_DATE_FORMAT);
            cal = new Calendar.Builder().setInstant(df.parse(accessTokenExpiry)).build();
        }

        return cal.getTime();
    }

    private static void sleep(long millis) {
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
