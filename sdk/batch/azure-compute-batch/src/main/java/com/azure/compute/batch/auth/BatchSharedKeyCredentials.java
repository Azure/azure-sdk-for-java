package com.azure.compute.batch.auth;

public final class BatchSharedKeyCredentials {
    private String accountName;

    private String keyValue;

    private String baseUrl;

    /**
     * Gets the Batch account name.
     *
     * @return The Batch account name.
     */
    public String accountName() {
        return accountName;
    }

    /**
     * Gets the Base64 encoded account access key.
     *
     * @return The Base64 encoded account access key.
     */
    public String keyValue() {
        return keyValue;
    }

    /**
     * Initializes a new instance of the {@link BatchSharedKeyCredentials} class with the specified Batch service endpoint, account name, and access key.
     *
     * @param baseUrl The Batch service endpoint.
     * @param accountName The Batch account name.
     * @param keyValue The Batch access key.
     */
    public BatchSharedKeyCredentials(String baseUrl, String accountName, String keyValue) {

        if (baseUrl == null) {
            throw new IllegalArgumentException("Parameter baseUrl is required and cannot be null.");
        }
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (keyValue == null) {
            throw new IllegalArgumentException("Parameter keyValue is required and cannot be null.");
        }

        this.baseUrl = baseUrl;
        this.accountName = accountName;
        this.keyValue = keyValue;
    }

    public String baseUrl() {
        return this.baseUrl;
    }


}
