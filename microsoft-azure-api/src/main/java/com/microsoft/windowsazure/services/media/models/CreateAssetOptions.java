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
 * The Class CreateAssetOptions.
 */
public class CreateAssetOptions {

    /** The name. */
    private String name;

    /** The alternate id. */
    private String alternateId;

    /** The options. */
    private EncryptionOption options;

    /** The state. */
    private AssetState state;

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
     * @return the creates the asset options
     */
    public CreateAssetOptions setAlternateId(String alternateId) {
        this.alternateId = alternateId;
        return this;
    }

    /**
     * Gets the options.
     * 
     * @return the options
     */
    public EncryptionOption getOptions() {
        return options;
    }

    /**
     * Sets the options.
     * 
     * @param encryptionOption
     *            the encryption option
     * @return the creates the asset options
     */
    public CreateAssetOptions setOptions(EncryptionOption encryptionOption) {
        this.options = encryptionOption;
        return this;
    }

    /**
     * Gets the state.
     * 
     * @return the state
     */
    public AssetState getState() {
        return state;
    }

    /**
     * Sets the state.
     * 
     * @param assetState
     *            the asset state
     * @return the creates the asset options
     */
    public CreateAssetOptions setState(AssetState assetState) {
        this.state = assetState;
        return this;
    }

    /**
     * Gets the name.
     * 
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name.
     * 
     * @param name
     *            the name
     * @return the creates the asset options
     */
    public CreateAssetOptions setName(String name) {
        this.name = name;
        return this;
    }

}