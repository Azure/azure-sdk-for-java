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

package com.microsoft.windowsazure.services.media.implementation.content;

import javax.xml.bind.annotation.XmlRegistry;

/**
 * Class used by JAXB to instantiate the types in this package.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    /**
     * Create a new ObjectFactory that can be used to create new instances of
     * schema derived classes for package:
     * com.microsoft.windowsazure.services.media.implementation.atom
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link AssetType }.
     * 
     * @return a new AssetType instance.
     */
    public AssetType createAssetType() {
        return new AssetType();
    }

    /**
     * Create an instance of {@link ODataActionType }.
     * 
     * @return a new ODataActionType instance.
     */
    public ODataActionType createODataActionType() {
        return new ODataActionType();
    }

    /**
     * Create an instance of {@link AccessPolicyType }.
     * 
     * @return a new AccessPolicyType instance.
     */
    public AccessPolicyType createAccessPolicyType() {
        return new AccessPolicyType();
    }

    /**
     * Create an instance of {@link LocatorRestType }.
     * 
     * @return a new LocatorRestType instance.
     */
    public LocatorRestType createLocatorRestType() {
        return new LocatorRestType();
    }

    /**
     * Create an instance of {@link MediaProcessorType }.
     * 
     * @return a new MediaProcessorType instance.
     */
    public MediaProcessorType createMediaProcessorType() {
        return new MediaProcessorType();
    }

    /**
     * Create an instance of {@link JobType}.
     * 
     * @return a new JobType instance.
     */
    public JobType createJobType() {
        return new JobType();
    }

    /**
     * Create an instance of {@link TaskType}.
     * 
     * @return a new TaskType instance.
     */
    public TaskType createTaskType() {
        return new TaskType();
    }

    /**
     * Creates a new Object object.
     * 
     * @return the content key rest type
     */
    public ContentKeyRestType createContentKeyRestType() {
        return new ContentKeyRestType();
    }

    /**
     * Create an instance of {@link AssetFileType}.
     * 
     * @return a new AssetFileType instance.
     */
    public AssetFileType createAssetFileType() {
        return new AssetFileType();
    }

    /**
     * Creates an instance of (@link RebindContentKeyType).
     * 
     * @return the rebind content key type instance.
     */
    public RebindContentKeyType createRebindContentKeyType() {
        return new RebindContentKeyType();
    }

    /**
     * Creates an instance of (@link ChannelType).
     * 
     * @return the channel type.
     */
    public ChannelType createChannelType() {
        return new ChannelType();
    }

    /**
     * Creates an instance of (@link JobNotificationSubscriptionType).
     * 
     * @return the job notification subscription type.
     */
    public JobNotificationSubscriptionType createJobNotificationSubscriptionType() {
        return new JobNotificationSubscriptionType();
    }

    /**
     * Creates an instance of (@link NotificationEndPointType).
     * 
     * @return the notification end point type.
     */
    public NotificationEndPointType createNotificationEndPointType() {
        return new NotificationEndPointType();
    }

    /**
     * Creates an instance of (@link OperationType).
     * 
     * @return the operation type
     */
    public OperationType createOperationType() {
        return new OperationType();
    }

    /**
     * Creates a instance of (@link @ProgramType).
     * 
     * @return the program type
     */
    public ProgramType createProgramType() {
        return new ProgramType();
    }
    
    /**
     * Creates a instance of (@link ContentKeyAuthorizationPolicyOptionType).
     * 
     * @return the content key authorization policy option type
     */
    public ContentKeyAuthorizationPolicyOptionType createContentKeyAuthorizationPolicyOptionType() {
        return new ContentKeyAuthorizationPolicyOptionType();
    }
    
    /**
     * Creates a instance of (@link ContentKeyAuthorizationPolicyRestrictionType).
     * 
     * @return the content key authorization policy restriction type
     */
    public ContentKeyAuthorizationPolicyRestrictionType createContentKeyAuthorizationPolicyRestrictionType() {
        return new ContentKeyAuthorizationPolicyRestrictionType();
    }
    
    /**
     * Creates a instance of (@link ContentKeyAuthorizationPolicyType).
     * 
     * @return the content key authorization policy type
     */
    public ContentKeyAuthorizationPolicyType createContentKeyAuthorizationPolicyType() {
        return new ContentKeyAuthorizationPolicyType();
    }
    
    /**
     * Creates a instance of (@link AssetDeliveryPolicyType).
     * 
     * @return the asset delivery policy type
     */
    public AssetDeliveryPolicyRestType createAssetDeliveryPolicyType() {
        return new AssetDeliveryPolicyRestType();
    }
    

    /**
     * Creates a instance of (@link StreamingEndpointType).
     * 
     * @return the streaming endpoint type
     */
    public StreamingEndpointType createStreamingEndpointType() {
        return new StreamingEndpointType();
    }
    
    /**
     * Creates a instance of (@link EncodingReservedUnitType).
     * 
     * @return the encoding reserved unit type
     */
    public EncodingReservedUnitRestType createEncodingReservedUnitType() {
        return new EncodingReservedUnitRestType();
    }
    
    /**
     * Creates a instance of (@link StorageAccountType).
     * 
     * @return the storage account type
     */
    public StorageAccountType createStorageAccountType() {
        return new StorageAccountType();
    }
    
    
}
