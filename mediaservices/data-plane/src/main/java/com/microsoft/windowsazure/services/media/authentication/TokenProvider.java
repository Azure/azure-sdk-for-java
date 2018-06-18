package com.microsoft.windowsazure.services.media.authentication;

public interface TokenProvider {

    /**
     * Acquire an access token
     *
     * @return a valid access token
     * @throws Exception
     */
    AzureAdAccessToken acquireAccessToken() throws Exception;

}
