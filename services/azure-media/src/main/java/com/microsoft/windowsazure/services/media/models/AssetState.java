/*
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

package com.microsoft.windowsazure.services.media.models;

import java.security.InvalidParameterException;

/**
 * Specifies the states of the asset.
 */
public enum AssetState {

    /** The Initialized. */
    Initialized(0),
    /** The Published. */
    Published(1),
    /** The Deleted. */
    Deleted(2);

    /** The asset state code. */
    private int assetStateCode;

    /**
     * Instantiates a new asset state.
     * 
     * @param assetStateCode
     *            the asset state code
     */
    private AssetState(int assetStateCode) {
        this.assetStateCode = assetStateCode;
    }

    /**
     * Gets the code.
     * 
     * @return the code
     */
    public int getCode() {
        return assetStateCode;
    }

    /**
     * Create an AssetState instance from the corresponding int.
     * 
     * @param state
     *            state as integer
     * @return new AssetState instance
     */
    public static AssetState fromCode(int state) {
        switch (state) {
        case 0:
            return AssetState.Initialized;
        case 1:
            return AssetState.Published;
        case 2:
            return AssetState.Deleted;
        default:
            throw new InvalidParameterException("state");
        }
    }
}
