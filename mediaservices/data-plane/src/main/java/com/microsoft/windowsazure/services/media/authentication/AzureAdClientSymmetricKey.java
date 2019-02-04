package com.microsoft.windowsazure.services.media.authentication;

/**
 * Represents a symmetric key pair of ClientId & ClientKey
 */
public class AzureAdClientSymmetricKey {

    private final String clientId;
    private final String clientKey;

    /**
     * Gets the client ID.
     * @return the client ID.
     */
    public String getClientId() {
        return this.clientId;
    }

    /**
     * Gets the client key.
     * @return the client key.
     */
    public String getClientKey() {
        return this.clientKey;
    }

    /**
     * Initializes a new instance of the AzureAdClientSymmetricKey class.
     * @param clientId The client ID.
     * @param clientKey The client key.
     */
    public AzureAdClientSymmetricKey(String clientId, String clientKey) {
        if (clientId == null || clientId.trim().isEmpty()) {
            throw new IllegalArgumentException("clientId");
        }

        if (clientKey == null || clientKey.trim().isEmpty()) {
            throw new IllegalArgumentException("clientKey");
        }

        this.clientId = clientId;
        this.clientKey = clientKey;
    }
}
