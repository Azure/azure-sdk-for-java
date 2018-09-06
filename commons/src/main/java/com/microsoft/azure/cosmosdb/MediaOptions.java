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

/**
 * Options used with attachment content (aka media) creation in the Azure Cosmos DB database service.
 */
public final class MediaOptions {
    private String slug;
    private String contentType;

    /**
     * Gets the HTTP Slug header value.
     *
     * @return the slug.
     */
    public String getSlug() {
        return this.slug;
    }

    /**
     * Sets the HTTP Slug header value.
     *
     * @param slug the slug.
     */
    public void setSlug(String slug) {
        this.slug = slug;
    }

    /**
     * Gets the HTTP ContentType header value.
     *
     * @return the content type.
     */
    public String getContentType() {
        return this.contentType;
    }

    /**
     * Sets the HTTP ContentType header value.
     *
     * @param contentType the content type.
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
