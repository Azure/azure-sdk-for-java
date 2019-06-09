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
package com.azure.data.cosmos;

import org.apache.commons.lang3.StringUtils;

public abstract class CosmosResource {

    private String id;

    public CosmosResource(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    protected CosmosResource id(String id) {
        this.id = id;
        return this;
    }

    protected abstract String URIPathSegment();
    protected abstract String parentLink();

    String getLink() {
        StringBuilder builder = new StringBuilder();
        builder.append(parentLink());
        builder.append("/");
        builder.append(URIPathSegment());
        builder.append("/");
        builder.append(id());
        return builder.toString();
    }

    protected static void validateResource(Resource resource) {
        if (!StringUtils.isEmpty(resource.id())) {
            if (resource.id().indexOf('/') != -1 || resource.id().indexOf('\\') != -1 ||
                    resource.id().indexOf('?') != -1 || resource.id().indexOf('#') != -1) {
                throw new IllegalArgumentException("Id contains illegal chars.");
            }

            if (resource.id().endsWith(" ")) {
                throw new IllegalArgumentException("Id ends with a space.");
            }
        }
    }

}
