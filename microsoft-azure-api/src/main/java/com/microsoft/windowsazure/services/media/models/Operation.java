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

package com.microsoft.windowsazure.services.media.models;

import com.microsoft.windowsazure.services.media.entityoperations.DefaultGetOperation;
import com.microsoft.windowsazure.services.media.entityoperations.DefaultListOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityGetOperation;
import com.sun.jersey.api.client.GenericType;

/**
 * Class for creating operations to manipulate Operation entities.
 * 
 */
public class Operation {

    /** The Constant ENTITY_SET. */
    private static final String ENTITY_SET = "Operations";

    // Prevent instantiation
    /**
     * Instantiates a new asset.
     */
    private Operation() {
    }

    /**
     * Create an operation object that will get the state of the given asset.
     * 
     * @param assetId
     *            id of asset to retrieve
     * @return the get operation
     */
    @SuppressWarnings("rawtypes")
    public static EntityGetOperation<OperationInfo> get(String operationId) {
        return new DefaultGetOperation<OperationInfo>(ENTITY_SET, operationId, OperationInfo.class);
    }

    /**
     * Create an operation that will list all the assets.
     * 
     * @return The list operation
     */
    @SuppressWarnings("rawtypes")
    public static DefaultListOperation<OperationInfo> list() {
        return new DefaultListOperation<OperationInfo>(ENTITY_SET, new GenericType<ListResult<OperationInfo>>() {
        });
    }
}
