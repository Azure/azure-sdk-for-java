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

import java.util.EnumSet;
import java.util.Map;

import com.microsoft.windowsazure.services.media.entityoperations.DefaultDeleteOperation;
import com.microsoft.windowsazure.services.media.entityoperations.DefaultGetOperation;
import com.microsoft.windowsazure.services.media.entityoperations.DefaultListOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityCreateOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityDeleteOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityGetOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityOperationBase;
import com.microsoft.windowsazure.services.media.entityoperations.EntityOperationSingleResultBase;
import com.microsoft.windowsazure.services.media.entityoperations.EntityUpdateOperation;
import com.microsoft.windowsazure.services.media.implementation.content.AssetDeliveryPolicyRestType;
import com.sun.jersey.api.client.GenericType;

/**
 * Class for creating operations to manipulate Asset Delivery Policy entities.
 * 
 */
public final class AssetDeliveryPolicy {

    private static final String ENTITY_SET = "AssetDeliveryPolicies";

    private AssetDeliveryPolicy() {
    }

    /**
     * Creates an operation to create a new AssetDeliveryPolicy
     * 
     * @param name
     *            name of the asset delivery policy
     * @return The operation
     */
    public static Creator create() {
        return new Creator();
    }

    public static class Creator extends EntityOperationSingleResultBase<AssetDeliveryPolicyInfo>
            implements EntityCreateOperation<AssetDeliveryPolicyInfo> {

        private String name;
        private EnumSet<AssetDeliveryProtocol> assetDeliveryProtocol;
        private AssetDeliveryPolicyType assetDeliveryPolicyType;
        private Map<AssetDeliveryPolicyConfigurationKey, String> assetDeliveryConfiguration;

        public Creator() {
            super(ENTITY_SET, AssetDeliveryPolicyInfo.class);
        }

        @Override
        public Object getRequestContents() {
            return new AssetDeliveryPolicyRestType().setName(name)
                    .setAssetDeliveryConfiguration(assetDeliveryConfiguration)
                    .setAssetDeliveryPolicyType(assetDeliveryPolicyType.getCode())
                    .setAssetDeliveryProtocol(AssetDeliveryProtocol.bitsFromProtocols(assetDeliveryProtocol));
        }

        /**
         * Set the name of the Asset Delivery Policy to be created.
         * 
         * @param name
         *            The name
         * @return The creator object (for call chaining)
         */
        public Creator setName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Set the protocol of the Asset Delivery Policy to be created.
         * 
         * @param assetDeliveryProtocol
         *            The protocol
         * @return The creator object (for call chaining)
         */
        public Creator setAssetDeliveryProtocol(EnumSet<AssetDeliveryProtocol> assetDeliveryProtocol) {
            this.assetDeliveryProtocol = assetDeliveryProtocol;
            return this;
        }

        /**
         * Set the type of the Asset Delivery Policy to be created.
         * 
         * @param assetDeliveryPolicyType
         *            The type
         * @return The creator object (for call chaining)
         */
        public Creator setAssetDeliveryPolicyType(AssetDeliveryPolicyType assetDeliveryPolicyType) {
            this.assetDeliveryPolicyType = assetDeliveryPolicyType;
            return this;
        }

        /**
         * Set the configuration of the Asset Delivery Policy to be created.
         * 
         * @param assetDeliveryPolicyConfiguration
         *            The configuration
         * @return The creator object (for call chaining)
         */
        public Creator setAssetDeliveryConfiguration(Map<AssetDeliveryPolicyConfigurationKey, String> assetDeliveryPolicyConfiguration) {
            this.assetDeliveryConfiguration = assetDeliveryPolicyConfiguration;
            return this;
        }

    }

    /**
     * Create an operation that will retrieve the given asset delivery policy
     * 
     * @param assetDeliveryPolicyId
     *            id of asset delivery policy to retrieve
     * @return the operation
     */
    public static EntityGetOperation<AssetDeliveryPolicyInfo> get(String assetDeliveryPolicyId) {
        return new DefaultGetOperation<AssetDeliveryPolicyInfo>(ENTITY_SET, assetDeliveryPolicyId,
                AssetDeliveryPolicyInfo.class);
    }

    /**
     * Create an operation that will retrieve all asset delivery policies
     * 
     * @return the operation
     */
    public static DefaultListOperation<AssetDeliveryPolicyInfo> list() {
        return new DefaultListOperation<AssetDeliveryPolicyInfo>(ENTITY_SET,
                new GenericType<ListResult<AssetDeliveryPolicyInfo>>() {
                });
    }

    /**
     * Create an operation that will list all the asset delivery policies at the
     * given link.
     * 
     * @param link
     *            Link to request all the asset delivery policies.
     * 
     * @return The list operation.
     */
    public static DefaultListOperation<AssetDeliveryPolicyInfo> list(LinkInfo<AssetDeliveryPolicyInfo> link) {
        return new DefaultListOperation<AssetDeliveryPolicyInfo>(link.getHref(),
                new GenericType<ListResult<AssetDeliveryPolicyInfo>>() {
                });
    }

    /**
     * Create an operation to delete the given asset delivery policy
     * 
     * @param contentKeyAuthorizationPolicyId
     *            id of content key authorization policy to delete
     * @return the delete operation
     */

    public static EntityDeleteOperation delete(String assetDeliveryPolicyId) {
        return new DefaultDeleteOperation(ENTITY_SET, assetDeliveryPolicyId);
    }
    
    /**
     * Create an operation that will update the given asset delivery policy.
     * 
     * @param assetDeliveryPolicyId
     *            id of the asset delivery policy to update
     * @return the update operation
     */
    public static Updater update(String assetDeliveryPolicyId) {
        return new Updater(assetDeliveryPolicyId);
    }
    
    /**
     * The Class Updater.
     */
    public static class Updater extends EntityOperationBase implements EntityUpdateOperation {

        private EnumSet<AssetDeliveryProtocol> assetDeliveryProtocol;
        private AssetDeliveryPolicyType assetDeliveryPolicyType;
        private Map<AssetDeliveryPolicyConfigurationKey, String> assetDeliveryConfiguration;

        protected Updater(String assetDeliveryPolicyId) {
          super(new EntityOperationBase.EntityIdUriBuilder(ENTITY_SET, assetDeliveryPolicyId));

        }

        @Override
        public Object getRequestContents() {
          return new AssetDeliveryPolicyRestType()
              .setAssetDeliveryConfiguration(assetDeliveryConfiguration)
              .setAssetDeliveryPolicyType(assetDeliveryPolicyType.getCode())
              .setAssetDeliveryProtocol(AssetDeliveryProtocol.bitsFromProtocols(assetDeliveryProtocol));
        }

        /**
         * Set the protocol of the Asset Delivery Policy to be created.
         * 
         * @param assetDeliveryProtocol
         *          The protocol
         * @return The creator object (for call chaining)
         */
        public Updater setAssetDeliveryProtocol(EnumSet<AssetDeliveryProtocol> assetDeliveryProtocol) {
          this.assetDeliveryProtocol = assetDeliveryProtocol;
          return this;
        }

        /**
         * Set the type of the Asset Delivery Policy to be created.
         * 
         * @param assetDeliveryPolicyType
         *          The type
         * @return The creator object (for call chaining)
         */
        public Updater setAssetDeliveryPolicyType(AssetDeliveryPolicyType assetDeliveryPolicyType) {
          this.assetDeliveryPolicyType = assetDeliveryPolicyType;
          return this;
        }

        /**
         * Set the configuration of the Asset Delivery Policy to be created.
         * 
         * @param assetDeliveryPolicyConfiguration
         *          The configuration
         * @return The creator object (for call chaining)
         */
        public Updater setAssetDeliveryConfiguration(
            Map<AssetDeliveryPolicyConfigurationKey, String> assetDeliveryPolicyConfiguration) {
          this.assetDeliveryConfiguration = assetDeliveryPolicyConfiguration;
          return this;
        }

      }
}
