package com.azure.identity.credential.msi;

/**
 * The source of MSI token.
 */
public enum VirtualMachineMSITokenSource {
    /**
     * Indicate that token should be retrieved from MSI extension installed in the VM.
     */
    MSI_EXTENSION,
    /**
     * Indicate that token should be retrieved from IMDS service.
     */
    IMDS_ENDPOINT
}
