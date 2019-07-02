package com.azure.storage.blob;

import com.azure.storage.blob.models.BlobGetPropertiesHeaders;
import com.azure.storage.blob.models.BlobType;
import com.azure.storage.blob.models.Metadata;

import java.time.OffsetDateTime;

/**
 * Representation of properties on a blob including both system and user defined info.
 */
public class BlobProperties {

    private final BlobType blobType;

    private final Metadata metadata;

    private final long blobSize;

    private final byte[] contentMD5;

    private final String contentEncoding;

    private final String contentDisposition;

    private final String contentLanguage;

    private final String cacheControl;

    private final OffsetDateTime lastModifiedTime;


    BlobProperties(BlobGetPropertiesHeaders generatedHeaders) {
        this.blobType = generatedHeaders.blobType();
        this.metadata = new Metadata(generatedHeaders.metadata());
        this.blobSize = generatedHeaders.contentLength() == null ? 0 : generatedHeaders.contentLength();
        this.contentMD5 = generatedHeaders.contentMD5();
        this.contentEncoding = generatedHeaders.contentEncoding();
        this.contentDisposition = generatedHeaders.contentDisposition();
        this.contentLanguage = generatedHeaders.contentLanguage();
        this.cacheControl = generatedHeaders.cacheControl();
        this.lastModifiedTime = generatedHeaders.lastModified();
    }


    /**
     * The type of this blob on the server.
     *
     * @return
     *      Enum of the possible blob types.
     */
    public BlobType blobType() {
        return blobType;
    }

    /**
     * The user-defined metadata on this blob.
     *
     * @return
     *      A map of metadata key/value pairs.
     */
    public Metadata metadata() {
        return metadata;
    }

    /**
     * Size of the blob.
     *
     * @return
     *      Number of bytes in the blob.
     */
    public long blobSize() {
        return blobSize;
    }

    /**
     * The MD5 hash of blob content, if stored on the service.
     *
     * @return
     *      The bytes of the MD5 hash.
     */
    public byte[] contentMD5() {
        return contentMD5;
    }

    /**
     * Content encoding information of the blob data.
     *
     * @return
     *      The value as represented in HTTP headers.
     */
    public String contentEncoding() {
        return contentEncoding;
    }

    /**
     * Content disposition information of the blob data.
     *
     * @return
     *      The value as represented in HTTP headers.
     */
    public String contentDisposition() {
        return contentDisposition;
    }

    /**
     * Content language information of the blob data.
     *
     * @return
     *      The value as represented in HTTP headers.
     */
    public String contentLanguage() {
        return contentLanguage;
    }

    /**
     * Cache control information of the blob data.
     *
     * @return
     *      The value as represented in HTTP headers.
     */
    public String cacheControl() {
        return cacheControl;
    }

    /**
     * Last modified time of the blob data. This encompasses changes to the blob data, metadata, and properties.
     *
     * @return
     *      The timestamp of last modification.
     */
    public OffsetDateTime lastModified() {
        return lastModifiedTime;
    }
}
