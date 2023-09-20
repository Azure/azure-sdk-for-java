package com.azure.storage.file.share.models;

/**
 * The audience to be used when requesting a token from Azure Active Directory (AAD).
 * Note: This audience only has an effect when authenticating a TokenCredential.
 */
public class ShareAudience implements Comparable<ShareAudience> {
    private final String audience;

    /**
     * The Azure Active Directory audience to use when forming authorization scopes.
     * For the Language service, this value corresponds to a URL that identifies the Azure cloud where the resource is
     * located. For more information see
     * <a href="https://learn.microsoft.com/en-us/azure/storage/blobs/authorize-access-azure-active-directory">
     *     Authorize access to Azure blobs using Azure Active Directory</a>.
     *
     * @param audience The Azure Active Directory audience to use when forming authorization scopes.
     */
    public ShareAudience(String audience) {
        this.audience = audience;
    }

    /**
     * Gets default Audience used to acquire a token for authorizing requests to any Azure Storage account.
     * If no audience is specified, this resource ID is the default value: "https://storage.azure.com/".
     *
     * @return public default audience.
     */
    public static ShareAudience getPublicAudience() {
        String publicAudience = "https://storage.azure.com/";
        return new ShareAudience(publicAudience);
    }

    /**
     * The service endpoint for a given storage account. Use this method to acquire a token for authorizing requests to
     * that specific Azure Storage account and service only.
     *
     * @param storageAccountName The storage account name used to populate the service endpoint.
     * @return the audience with the file service endpoint.
     */
    public static ShareAudience getShareServiceAccountAudience(String storageAccountName) {
        return new ShareAudience(String.format("https://%s.file.core.windows.net/", storageAccountName));
    }

    /**
     * Get the audience for the file share account.
     *
     * @return the audience.
     */
    public String getAudience() {
        return this.audience;
    }

    @Override
    public int compareTo(ShareAudience other) {
        return this.audience.compareTo(other.audience);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ShareAudience) {
            return this.audience.equals(((ShareAudience) obj).audience);
        }
        return false;
    }

    @Override
    public String toString() {
        return this.audience;
    }

    @Override
    public int hashCode() {
        return this.audience.hashCode();
    }

    /**
     * Creates a scope with the respective audience and the default scope.
     *
     * @return the scope with the respective audience and the default scope.
     */
    public String createDefaultScope() {
        if (this.audience.endsWith("/")) {
            return this.audience + ".default";
        } else {
            return this.audience + "/.default";
        }
    }
}
