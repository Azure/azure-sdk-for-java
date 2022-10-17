package com.azure.storage.common;

import java.util.Objects;

/**
 * Options for additional content integrity checks on download.
 */
public class DownloadTransferValidationOptions {
    private StorageChecksumAlgorithm checksumAlgorithm = StorageChecksumAlgorithm.Auto;
    private boolean autoValidateChecksum = true;

    /**
     * Creates a new {@link DownloadTransferValidationOptions} with default parameters applied.
     */
    public DownloadTransferValidationOptions() {
    }

    /**
     * Gets the identifier of the checksum algorithm to use.
     * @return Checksum algorithm ID.
     */
    public StorageChecksumAlgorithm getChecksumAlgorithm() {
        return checksumAlgorithm;
    }

    public StorageChecksumAlgorithm getChecksumAlgorithmResolveAuto() {
        return checksumAlgorithm == StorageChecksumAlgorithm.Auto
            ? StorageChecksumAlgorithm.StorageCrc64
            : checksumAlgorithm;
    }

    /**
     * Sets the identifier of the checksum algorithm to use.
     * @param checksumAlgorithm Checksum algorithm ID.
     */
    public DownloadTransferValidationOptions setChecksumAlgorithm(StorageChecksumAlgorithm checksumAlgorithm) {
        Objects.requireNonNull(checksumAlgorithm, "'checksumAlgorithm' cannot be null.");
        this.checksumAlgorithm = checksumAlgorithm;
        return this;
    }

    /**
     * Defaults to true. False can only be specified on specific operations and not at the client level.
     * Indicates whether the SDK should validate the content body against the content hash before returning contents to
     * the caller. If set to false, caller is responsible for extracting the hash out of the
     * {@link com.azure.core.http.rest.Response} and validating the hash themselves.
     * @return Behavior flag.
     */
    public boolean getAutoValidateChecksum() {
        return autoValidateChecksum;
    }

    /**
     * Defaults to true. False can only be specified on specific operations and not at the client level.
     * Indicates whether the SDK should validate the content body against the content hash before returning contents to
     * the caller. If set to false, caller is responsible for extracting the hash out of the
     * {@link com.azure.core.http.rest.Response} and validating the hash themselves.
     * @param  autoValidateChecksum Behavior flag.
     */
    public DownloadTransferValidationOptions setAutoValidateChecksum(boolean autoValidateChecksum) {
        this.autoValidateChecksum = autoValidateChecksum;
        return this;
    }
}
