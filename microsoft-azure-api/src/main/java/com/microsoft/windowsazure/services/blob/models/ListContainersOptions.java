/**
 * Copyright 2011 Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.microsoft.windowsazure.services.blob.models;

public class ListContainersOptions extends BlobServiceOptions {
    private String prefix;
    private String marker;
    private int maxResults;
    private boolean includeMetadata;

    @Override
    public ListContainersOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    public String getPrefix() {
        return prefix;
    }

    public ListContainersOptions setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public String getMarker() {
        return marker;
    }

    public ListContainersOptions setMarker(String marker) {
        this.marker = marker;
        return this;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public ListContainersOptions setMaxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    public boolean isIncludeMetadata() {
        return includeMetadata;
    }

    public ListContainersOptions setIncludeMetadata(boolean includeMetadata) {
        this.includeMetadata = includeMetadata;
        return this;
    }
}
