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

import java.util.ArrayList;
import java.util.List;

import com.microsoft.windowsazure.services.media.entityoperations.DefaultDeleteOperation;
import com.microsoft.windowsazure.services.media.entityoperations.DefaultGetOperation;
import com.microsoft.windowsazure.services.media.entityoperations.DefaultListOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityCreateOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityDeleteOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityGetOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityOperationBase;
import com.microsoft.windowsazure.services.media.entityoperations.EntityOperationSingleResultBase;
import com.microsoft.windowsazure.services.media.entityoperations.EntityUpdateOperation;
import com.microsoft.windowsazure.services.media.implementation.content.ContentKeyAuthorizationPolicyOptionType;
import com.microsoft.windowsazure.services.media.implementation.content.ContentKeyAuthorizationPolicyRestrictionType;
import com.sun.jersey.api.client.GenericType;

/**
 * Class for creating operations to manipulate Access Policy entities.
 * 
 */
public final class ContentKeyAuthorizationPolicyOption {

    private static final String ENTITY_SET = "ContentKeyAuthorizationPolicyOptions";

    private ContentKeyAuthorizationPolicyOption() {
    }

    /**
     * Creates an operation to create a new content key authorization options
     * 
     * @param name
     *            Friendly name of the authorization policy
     * @param keyDeliveryType
     *            Delivery method of the content key to the client
     * @param keyDeliveryConfiguration
     *            Xml data, specific to the key delivery type that defines how
     *            the key is delivered to the client
     * @param restrictions
     *            Requirements defined in each restriction must be met in order
     *            to deliver the key using the key delivery data
     * @return The operation
     */
    public static EntityCreateOperation<ContentKeyAuthorizationPolicyOptionInfo> create(String name,
            int keyDeliveryType, String keyDeliveryConfiguration,
            List<ContentKeyAuthorizationPolicyRestriction> restrictions) {
        return new Creator(name, keyDeliveryType, keyDeliveryConfiguration, restrictions);
    }

    private static class Creator extends EntityOperationSingleResultBase<ContentKeyAuthorizationPolicyOptionInfo>
            implements EntityCreateOperation<ContentKeyAuthorizationPolicyOptionInfo> {

        private String name;
        private int keyDeliveryType;
        private String keyDeliveryConfiguration;
        private List<ContentKeyAuthorizationPolicyRestrictionType> restrictions;

        public Creator(String name, int keyDeliveryType, String keyDeliveryConfiguration,
                List<ContentKeyAuthorizationPolicyRestriction> restrictions) {

            super(ENTITY_SET, ContentKeyAuthorizationPolicyOptionInfo.class);

            this.name = name;
            this.keyDeliveryType = keyDeliveryType;
            this.keyDeliveryConfiguration = keyDeliveryConfiguration;
            this.restrictions = new ArrayList<ContentKeyAuthorizationPolicyRestrictionType>();
            for (ContentKeyAuthorizationPolicyRestriction restriction : restrictions) {
                this.restrictions.add(new ContentKeyAuthorizationPolicyRestrictionType().setName(restriction.getName())
                        .setKeyRestrictionType(restriction.getKeyRestrictionType())
                        .setRequirements(restriction.getRequirements()));
            }
        }

        @Override
        public Object getRequestContents() {
            return new ContentKeyAuthorizationPolicyOptionType().setName(name).setKeyDeliveryType(keyDeliveryType)
                    .setKeyDeliveryConfiguration(keyDeliveryConfiguration).setRestrictions(restrictions);
        }
    }

    /**
     * 
     * 
     * Create an operation that will retrieve the given content key
     * authorization policy option
     * 
     * @param contentKeyAuthorizationPolicyOptionId
     *            id of content key authorization policy option to retrieve
     * @return the operation
     */
    public static EntityGetOperation<ContentKeyAuthorizationPolicyOptionInfo> get(
            String contentKeyAuthorizationPolicyOptionId) {
        return new DefaultGetOperation<ContentKeyAuthorizationPolicyOptionInfo>(ENTITY_SET,
                contentKeyAuthorizationPolicyOptionId, ContentKeyAuthorizationPolicyOptionInfo.class);
    }

    /**
     * Create an operation that will list all the content keys authorization
     * policy options at the given link.
     * 
     * @param link
     *            Link to request content keys authorization policy options
     *            from.
     * @return The list operation.
     */
    public static DefaultListOperation<ContentKeyAuthorizationPolicyOptionInfo> list(
            LinkInfo<ContentKeyAuthorizationPolicyOptionInfo> link) {
        return new DefaultListOperation<ContentKeyAuthorizationPolicyOptionInfo>(link.getHref(),
                new GenericType<ListResult<ContentKeyAuthorizationPolicyOptionInfo>>() {
                });
    }

    /**
     * Create an operation that will retrieve all content key authorization
     * policy options
     * 
     * @return the operation
     */

    public static DefaultListOperation<ContentKeyAuthorizationPolicyOptionInfo> list() {
        return new DefaultListOperation<ContentKeyAuthorizationPolicyOptionInfo>(ENTITY_SET,
                new GenericType<ListResult<ContentKeyAuthorizationPolicyOptionInfo>>() {
                });
    }

    /**
     * Create an operation to delete the given content key authorization policy
     * option
     * 
     * @param contentKeyAuthorizationPolicyOptionId
     *            id of content key authorization policy option to delete
     * @return the delete operation
     */

    public static EntityDeleteOperation delete(String contentKeyAuthorizationPolicyOptionId) {
        return new DefaultDeleteOperation(ENTITY_SET, contentKeyAuthorizationPolicyOptionId);
    }
    
    /**
     * Create an operation that will update the given content key authorization policy option.
     * 
     * @param contentKeyAuthorizationPolicyOptionId
     *            id of the acontent key authorization policy option to update
     * @return the update operation
     */
    public static Updater update(String contentKeyAuthorizationPolicyOptionId) {
        return new Updater(contentKeyAuthorizationPolicyOptionId);
    }
    
    /**
     * The Class Updater.
     */
    public static class Updater extends EntityOperationBase implements EntityUpdateOperation {
        
        private int keyDeliveryType;
        private String keyDeliveryConfiguration;
        private List<ContentKeyAuthorizationPolicyRestrictionType> restrictions;

        protected Updater(String contentKeyAuthorizationPolicyOptionId) {
          super(new EntityOperationBase.EntityIdUriBuilder(ENTITY_SET, 
              contentKeyAuthorizationPolicyOptionId));

        }

        @Override
        public Object getRequestContents() {
          return new ContentKeyAuthorizationPolicyOptionType().setKeyDeliveryType(keyDeliveryType)
              .setKeyDeliveryConfiguration(keyDeliveryConfiguration).setRestrictions(restrictions);

        }

        /**
         * Set the protocol of the Asset Delivery Policy to be created.
         * 
         * @param assetDeliveryProtocol
         *          The protocol
         * @return The creator object (for call chaining)
         */
        public Updater setKeyDeliveryType(int keyDeliveryType) {
          this.keyDeliveryType = keyDeliveryType;
          return this;
        }

        /**
         * Set the type of the Asset Delivery Policy to be created.
         * 
         * @param assetDeliveryPolicyType
         *          The type
         * @return The creator object (for call chaining)
         */
        public Updater setKeyDeliveryConfiguration(String keyDeliveryConfiguration) {
          this.keyDeliveryConfiguration = keyDeliveryConfiguration;
          return this;
        }

        /**
         * Set the configuration of the Asset Delivery Policy to be created.
         * 
         * @param assetDeliveryPolicyConfiguration
         *          The configuration
         * @return The creator object (for call chaining)
         */
        public Updater setRestrictions(
            List<ContentKeyAuthorizationPolicyRestriction> restrictions) {
          
          this.restrictions = new ArrayList<ContentKeyAuthorizationPolicyRestrictionType>();
          for (ContentKeyAuthorizationPolicyRestriction restriction : restrictions) {
            this.restrictions.add(new ContentKeyAuthorizationPolicyRestrictionType()
                .setName(restriction.getName())
                .setKeyRestrictionType(restriction.getKeyRestrictionType())
                .setRequirements(restriction.getRequirements()));
          }
          return this;
        }
      }
}
