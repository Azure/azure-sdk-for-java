package com.azure.compute.batch.models;

import java.time.OffsetDateTime;

public class GetBatchNodeFileOptions extends BatchBaseOptions {
    private OffsetDateTime ifModifiedSince;
    private OffsetDateTime ifUnmodifiedSince;
    private String ocpRange;

    /**
     * Gets a timestamp indicating the last modified time of the resource known to the client. The operation will be
     * performed only if the resource on the service has been modified since the specified time.
     *
     * @return A timestamp indicating the last modified time of the resource.
     */
    public OffsetDateTime getIfModifiedSince() {
        return ifModifiedSince;
    }

    /**
     * Sets a timestamp indicating the last modified time of the resource known to the client. The operation will be
     * performed only if the resource on the service has been modified since the specified time.
     *
     * @param ifModifiedSince A timestamp indicating the last modified time of the resource.
     */
    public void setIfModifiedSince(OffsetDateTime ifModifiedSince) {
        this.ifModifiedSince = ifModifiedSince;
    }

    /**
     * Gets a timestamp indicating the last modified time of the resource known to the client. The operation will be
     * performed only if the resource on the service has not been modified since the specified time.
     *
     * @return A timestamp indicating the last modified time of the resource.
     */
    public OffsetDateTime getIfUnmodifiedSince() {
        return ifUnmodifiedSince;
    }

    /**
     * Sets a timestamp indicating the last modified time of the resource known to the client. The operation will be
     * performed only if the resource on the service has not been modified since the specified time.
     *
     * @param ifUnmodifiedSince A timestamp indicating the last modified time of the resource.
     */
    public void setIfUnmodifiedSince(OffsetDateTime ifUnmodifiedSince) {
        this.ifUnmodifiedSince = ifUnmodifiedSince;
    }

    /**
     * Gets the byte range to be retrieved. The default is to retrieve the entire file. The format is bytes=startRange-endRange.
     *
     * @return The byte range to be retrieved.
     */
    public String getOcpRange() {
        return ocpRange;
    }

    /**
     * Sets the byte range to be retrieved. The default is to retrieve the entire file. The format is bytes=startRange-endRange.
     *
     * @param ocpRange The byte range to be retrieved.
     */
    public void setOcpRange(String ocpRange) {
        this.ocpRange = ocpRange;
    }

}
