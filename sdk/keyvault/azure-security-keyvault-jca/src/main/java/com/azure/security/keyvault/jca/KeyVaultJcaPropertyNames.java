// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

/**
 * System property names supported by the Azure Key Vault JCA provider.
 */
public final class KeyVaultJcaPropertyNames {

    /**
     * The Azure Key Vault endpoint property name.
     */
    public static final String KEYVAULT_URI = "azure.keyvault.uri";

    /**
     * The Microsoft Entra tenant ID property name.
     */
    public static final String KEYVAULT_TENANT_ID = "azure.keyvault.tenant-id";

    /**
     * The client ID property name.
     */
    public static final String KEYVAULT_CLIENT_ID = "azure.keyvault.client-id";

    /**
     * The client secret property name.
     */
    public static final String KEYVAULT_CLIENT_SECRET = "azure.keyvault.client-secret";

    /**
     * The managed identity property name.
     */
    public static final String KEYVAULT_MANAGED_IDENTITY = "azure.keyvault.managed-identity";

    /**
     * The access token property name.
     */
    public static final String KEYVAULT_ACCESS_TOKEN = "azure.keyvault.access-token";

    /**
     * The property name used to disable challenge resource verification.
     */
    public static final String KEYVAULT_DISABLE_CHALLENGE_RESOURCE_VERIFICATION
        = "azure.keyvault.disable-challenge-resource-verification";

    /**
     * The well-known certificate path property name.
     */
    public static final String CERT_PATH_WELL_KNOWN = "azure.cert-path.well-known";

    /**
     * The custom certificate path property name.
     */
    public static final String CERT_PATH_CUSTOM = "azure.cert-path.custom";

    /**
     * The certificate refresh interval property name.
     */
    public static final String KEYVAULT_JCA_CERTIFICATES_REFRESH_INTERVAL
        = "azure.keyvault.jca.certificates-refresh-interval";

    /**
     * The certificate refresh interval in milliseconds property name.
     */
    public static final String KEYVAULT_JCA_CERTIFICATES_REFRESH_INTERVAL_IN_MS
        = "azure.keyvault.jca.certificates-refresh-interval-in-ms";

    /**
     * The property name used to refresh certificates when an untrusted certificate is encountered.
     */
    public static final String KEYVAULT_JCA_REFRESH_CERTIFICATES_WHEN_HAVE_UNTRUST_CERTIFICATE
        = "azure.keyvault.jca.refresh-certificates-when-have-un-trust-certificate";

    /**
     * The certificate alias filter pattern property name.
     */
    public static final String KEYVAULT_JCA_CERTIFICATE_ALIAS_FILTER_PATTERN
        = "azure.keyvault.jca.certificate-alias-filter-pattern";

    /**
     * The property name used to disable Authority Information Access (AIA) certificate downloads.
     */
    public static final String KEYVAULT_JCA_DISABLE_AIA_DOWNLOAD = "azure.keyvault.jca.disable-aia-download";

    private KeyVaultJcaPropertyNames() {
    }
}
