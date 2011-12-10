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

import java.util.HashMap;

public class CreateContainerOptions extends BlobServiceOptions {
    private String publicAccess;
    private HashMap<String, String> metadata = new HashMap<String, String>();

    @Override
    public CreateContainerOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    public HashMap<String, String> getMetadata() {
        return metadata;
    }

    public CreateContainerOptions setMetadata(HashMap<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    public CreateContainerOptions addMetadata(String key, String value) {
        this.getMetadata().put(key, value);
        return this;
    }

    public String getPublicAccess() {
        return publicAccess;
    }

    public CreateContainerOptions setPublicAccess(String publicAccess) {
        this.publicAccess = publicAccess;
        return this;
    }
}
