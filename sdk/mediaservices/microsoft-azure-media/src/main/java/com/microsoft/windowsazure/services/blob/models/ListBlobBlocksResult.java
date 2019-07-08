/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.services.blob.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.microsoft.windowsazure.core.pipeline.Base64StringAdapter;

/**
 * A wrapper class for the response returned from a Blob Service REST API Get
 * Block List operation. This is returned by calls to implementations of
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#listBlobBlocks(String, String)} and
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#listBlobBlocks(String, String, ListBlobBlocksOptions)}.
 * <p>
 * See the <a
 * href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179400.aspx">Get
 * Block List</a> documentation on MSDN for details of the underlying Blob
 * Service REST API operation.
 */
@XmlRootElement(name = "BlockList")
public class ListBlobBlocksResult {
    private Date lastModified;
    private String etag;
    private String contentType;
    private long contentLength;
    private List<Entry> committedBlocks = new ArrayList<Entry>();
    private List<Entry> uncommittedBlocks = new ArrayList<Entry>();

    /**
     * Gets the last modified time of the block blob. This value is returned
     * only if the blob has committed blocks.
     * <p>
     * Any operation that modifies the blob, including updates to the blob's
     * metadata or properties, changes the last modified time of the blob. This
     * value can be used in an access condition when updating or deleting a blob
     * to prevent the client from modifying data that has been changed by
     * another client.
     * 
     * @return A {@link java.util.Date} containing the last modified time of the
     *         blob.
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * Reserved for internal use. Sets the last modified time of the blob from
     * the <strong>Last-Modified</strong> header returned in the response.
     * <p>
     * This method is invoked by the API to set the value from the Blob Service
     * REST API operation response returned by the server.
     * 
     * @param lastModified
     *            A {@link java.util.Date} containing the last modified time of
     *            the blob.
     */
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Gets the ETag of the blob. This value is returned only if the blob has
     * committed blocks.
     * <p>
     * This value can be used in an access condition when updating or deleting a
     * blob to prevent the client from modifying data that has been changed by
     * another client.
     * 
     * @return A {@link String} containing the server-assigned ETag value for
     *         the blob.
     */
    public String getEtag() {
        return etag;
    }

    /**
     * Reserved for internal use. Sets the ETag of the blob from the
     * <code>ETag</code> header returned in the response.
     * <p>
     * This method is invoked by the API to set the value from the Blob Service
     * REST API operation response returned by the server.
     * 
     * @param etag
     *            A {@link String} containing the server-assigned ETag value for
     *            the blob.
     */
    public void setEtag(String etag) {
        this.etag = etag;
    }

    /**
     * Gets the MIME content type of the blob. This value defaults to
     * <strong>application/xml</strong>.
     * 
     * @return A {@link String} containing the MIME content type value for the
     *         blob.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Reserved for internal use. Sets the MIME content type of the blob from
     * the <code>Content-Type</code> header returned in the response.
     * <p>
     * This method is invoked by the API to set the value from the Blob Service
     * REST API operation response returned by the server.
     * 
     * @param contentType
     *            A {@link String} containing the MIME content type value for
     *            the blob.
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Gets the size of the blob in bytes. For blobs with no committed blocks,
     * this value is 0.
     * 
     * @return The size of the blob in bytes.
     */
    public long getContentLength() {
        return contentLength;
    }

    /**
     * Reserved for internal use. Sets the content length of the blob from the
     * <code>x-ms-blob-content-length</code> header returned in the response.
     * <p>
     * This method is invoked by the API to set the value from the Blob Service
     * REST API operation response returned by the server.
     * 
     * @param contentLength
     *            The size of the blob in bytes.
     */
    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    /**
     * Gets a list of the committed blocks of the blob. This list may be empty
     * if no blocks have been committed or if committed blocks were not
     * specified in the request.
     * 
     * @return A {@link List} of {@link Entry} instances representing the
     *         committed blocks of the blob.
     */
    @XmlElementWrapper(name = "CommittedBlocks")
    @XmlElement(name = "Block")
    public List<Entry> getCommittedBlocks() {
        return committedBlocks;
    }

    /**
     * Reserved for internal use. Sets the list of the committed blocks of the
     * blob from the <strong>Block</strong> elements in the
     * <strong>CommittedBlocks</strong> element of the
     * <strong>BlockList</strong> element in the response body returned by the
     * server.
     * 
     * @param committedBlocks
     *            A {@link List} of {@link Entry} instances representing the
     *            committed blocks of the blob.
     */
    public void setCommittedBlocks(List<Entry> committedBlocks) {
        this.committedBlocks = committedBlocks;
    }

    /**
     * Gets a list of the uncommitted blocks of the blob. This list may be empty
     * if no uncommitted blocks are associated with the blob, or if uncommitted
     * blocks were not specified in the {@link ListBlobBlocksOptions options}
     * parameter of the request.
     * 
     * @return A {@link List} of {@link Entry} instances representing the
     *         uncommitted blocks of the blob.
     */
    @XmlElementWrapper(name = "UncommittedBlocks")
    @XmlElement(name = "Block")
    public List<Entry> getUncommittedBlocks() {
        return uncommittedBlocks;
    }

    /**
     * Reserved for internal use. Sets the list of the uncommitted blocks of the
     * blob from the <strong>Block</strong> elements in the
     * <strong>UncommittedBlocks</strong> element of the
     * <strong>BlockList</strong> element in the response body returned by the
     * server.
     * 
     * @param uncommittedBlocks
     *            A {@link List} of {@link Entry} instances representing the
     *            uncommitted blocks of the blob.
     */
    public void setUncommittedBlocks(List<Entry> uncommittedBlocks) {
        this.uncommittedBlocks = uncommittedBlocks;
    }

    /**
     * The class for an entry in a list of blocks, representing a committed or
     * uncommitted block.
     */
    public static class Entry {
        private String blockId;
        private long blockLength;

        /**
         * Gets the client-specified block ID for a block list entry.
         * 
         * @return A {@link String} containing the client-specified block ID for
         *         a block.
         */
        @XmlElement(name = "Name")
        @XmlJavaTypeAdapter(Base64StringAdapter.class)
        public String getBlockId() {
            return blockId;
        }

        /**
         * Reserved for internal use. Sets the client-specified block ID for a
         * block list entry from the <strong>Name</strong> element in the
         * <strong>Block</strong> element in the list in the response.
         * <p>
         * This method is invoked by the API to set the value from the Blob
         * Service REST API operation response returned by the server.
         * 
         * @param blockId
         *            A {@link String} containing the client-specified block ID
         *            for the block.
         */
        public void setBlockId(String blockId) {
            this.blockId = blockId;
        }

        /**
         * Gets the length in bytes of a block list entry.
         * 
         * @return The length of the block in bytes.
         */
        @XmlElement(name = "Size")
        public long getBlockLength() {
            return blockLength;
        }

        /**
         * Reserved for internal use. Sets the length in bytes of a block list
         * entry from the <strong>Size</strong> element in the
         * <strong>Block</strong> element in the list in the response.
         * <p>
         * This method is invoked by the API to set the value from the Blob
         * Service REST API operation response returned by the server.
         * 
         * @param blockLength
         *            The length of the block in bytes.
         */
        public void setBlockLength(long blockLength) {
            this.blockLength = blockLength;
        }
    }
}
