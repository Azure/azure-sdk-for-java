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

public class UpdateAssetOptions {

    private String alternateId;
    private String name;
    private EncryptionOption options;
    private AssetState state;

    public String getAlternateId() {
        return alternateId;
    }

    public UpdateAssetOptions setAlternateId(String alternateId) {
        this.alternateId = alternateId;
        return this;
    }

    public String getName() {
        return name;
    }

    public UpdateAssetOptions setName(String name) {
        this.name = name;
        return this;
    }

    public EncryptionOption getOptions() {
        return options;
    }

    public UpdateAssetOptions setOptions(EncryptionOption options) {
        this.options = options;
        return this;
    }

    public AssetState getState() {
        return state;
    }

    public UpdateAssetOptions setState(AssetState assetState) {
        this.state = assetState;
        return this;
    }

}
