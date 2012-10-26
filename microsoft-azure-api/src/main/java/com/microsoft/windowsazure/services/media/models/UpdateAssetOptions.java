/**
 * Copyright 2012 Microsoft Corporation
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
package com.microsoft.windowsazure.services.media.models;

/**
 * The Class UpdateAssetOptions.
 */
public class UpdateAssetOptions {

    /** The alternate id. */
    private String alternateId;

    /** The name. */
    private String name;

    /**
     * Gets the alternate id.
     * 
     * @return the alternate id
     */
    public String getAlternateId() {
        return alternateId;
    }

    /**
     * Sets the alternate id.
     * 
     * @param alternateId
     *            the alternate id
     * @return the update asset options
     */
    public UpdateAssetOptions setAlternateId(String alternateId) {
        this.alternateId = alternateId;
        return this;
    }

    /**
     * Gets the name.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     * 
     * @param name
     *            the name
     * @return the update asset options
     */
    public UpdateAssetOptions setName(String name) {
        this.name = name;
        return this;
    }

}
