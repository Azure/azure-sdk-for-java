/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb;

import org.json.JSONObject;

import com.microsoft.azure.cosmosdb.internal.Constants;

/**
 * Represents a document attachment in the Azure Cosmos DB database service.
 * <p>
 * Each document may contain zero or more attachments. Attachments can be of any MIME type - text, image, binary data.
 * These are stored externally in Azure Blob storage. Attachments are automatically deleted when the parent document
 * is deleted.
 */
@SuppressWarnings("serial")
public class Attachment extends Resource {
    /**
     * Initialize an attachment object.
     */
    public Attachment() {
        super();
    }

    /**
     * Initialize an attachment object from json string.
     *
     * @param source the json string representation of the Attachment.
     */
    public Attachment(String source) {
        super(source);
    }

    /**
     * Initialize an attachment object from json object.
     *
     * @param jsonObject the json object representation of the Attachment.
     */
    public Attachment(JSONObject jsonObject) {
        super(jsonObject);
    }

    /**
     * Gets the MIME content type of the attachment.
     *
     * @return the content type.
     */
    public String getContentType() {
        return super.getString(Constants.Properties.CONTENT_TYPE);
    }

    /**
     * Sets the MIME content type of the attachment.
     *
     * @param contentType the content type to use.
     */
    public void setContentType(String contentType) {
        super.set(Constants.Properties.CONTENT_TYPE, contentType);
    }

    /**
     * Gets the media link associated with the attachment content.
     *
     * @return the media link.
     */
    public String getMediaLink() {
        return super.getString(Constants.Properties.MEDIA_LINK);
    }

    /**
     * Sets the media link associated with the attachment content.
     *
     * @param mediaLink the media link to use.
     */
    public void setMediaLink(String mediaLink) {
        super.set(Constants.Properties.MEDIA_LINK, mediaLink);
    }
}
