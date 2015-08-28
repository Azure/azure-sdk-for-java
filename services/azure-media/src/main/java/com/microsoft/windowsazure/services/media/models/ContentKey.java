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

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidParameterException;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.microsoft.windowsazure.core.pipeline.PipelineHelpers;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.media.entityoperations.DefaultDeleteOperation;
import com.microsoft.windowsazure.services.media.entityoperations.DefaultEntityTypeActionOperation;
import com.microsoft.windowsazure.services.media.entityoperations.DefaultGetOperation;
import com.microsoft.windowsazure.services.media.entityoperations.DefaultListOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityCreateOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityDeleteOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityGetOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityOperationBase;
import com.microsoft.windowsazure.services.media.entityoperations.EntityOperationSingleResultBase;
import com.microsoft.windowsazure.services.media.entityoperations.EntityTypeActionOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityUpdateOperation;
import com.microsoft.windowsazure.services.media.implementation.content.ContentKeyRestType;
import com.microsoft.windowsazure.services.media.implementation.content.RebindContentKeyType;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;

/**
 * Class for creating operations to manipulate content key entities.
 * 
 */
public final class ContentKey {

    /** The Constant ENTITY_SET. */
    private static final String ENTITY_SET = "ContentKeys";

    /**
     * Instantiates a new content key.
     */
    private ContentKey() {
    }

    /**
     * Creates an operation to create a new content key.
     * 
     * @param id
     *            the id
     * @param contentKeyType
     *            the content key type
     * @param encryptedContentKey
     *            the encrypted content key
     * @return The operation
     */
    public static Creator create(String id, ContentKeyType contentKeyType,
            String encryptedContentKey) {
        return new Creator(id, contentKeyType, encryptedContentKey);
    }

    /**
     * The Class Creator.
     */
    public static class Creator extends
            EntityOperationSingleResultBase<ContentKeyInfo> implements
            EntityCreateOperation<ContentKeyInfo> {

        /** The id. */
        private final String id;

        /** The content key type. */
        private final ContentKeyType contentKeyType;

        /** The encrypted content key. */
        private final String encryptedContentKey;

        /** The name. */
        private String name;

        /** The checksum. */
        private String checksum;

        /** The protection key id. */
        private String protectionKeyId;

        /** The protection key type. */
        private ProtectionKeyType protectionKeyType;

        /**
         * Instantiates a new creator.
         * 
         * @param id
         *            the id
         * @param contentKeyType
         *            the content key type
         * @param encryptedContentKey
         *            the encrypted content key
         */
        public Creator(String id, ContentKeyType contentKeyType,
                String encryptedContentKey) {
            super(ENTITY_SET, ContentKeyInfo.class);

            this.id = id;
            this.contentKeyType = contentKeyType;
            this.encryptedContentKey = encryptedContentKey;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.microsoft.windowsazure.services.media.entityoperations.
         * EntityCreateOperation#getRequestContents()
         */
        @Override
        public Object getRequestContents() {
            ContentKeyRestType contentKeyRestType = new ContentKeyRestType();
            contentKeyRestType.setId(id);
            if (contentKeyType != null) {
                contentKeyRestType.setContentKeyType(contentKeyType.getCode());
            }
            if (protectionKeyType != null) {
                contentKeyRestType.setProtectionKeyType(protectionKeyType
                        .getCode());
            }
            contentKeyRestType.setEncryptedContentKey(encryptedContentKey);
            contentKeyRestType.setName(name);
            contentKeyRestType.setChecksum(checksum);
            contentKeyRestType.setProtectionKeyId(protectionKeyId);
            return contentKeyRestType;
        }

        /**
         * Sets the name.
         * 
         * @param name
         *            the name
         * @return the creator
         */
        public Creator setName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the checksum.
         * 
         * @param checksum
         *            the checksum
         * @return the creator
         */
        public Creator setChecksum(String checksum) {
            this.checksum = checksum;
            return this;
        }

        /**
         * Sets the protection key id.
         * 
         * @param protectionKeyId
         *            the protection key id
         * @return the creator
         */
        public Creator setProtectionKeyId(String protectionKeyId) {
            this.protectionKeyId = protectionKeyId;
            return this;
        }

        /**
         * Sets the protection key type.
         * 
         * @param protectionKeyType
         *            the protection key type
         * @return the creator
         */
        public Creator setProtectionKeyType(ProtectionKeyType protectionKeyType) {
            this.protectionKeyType = protectionKeyType;
            return this;
        }
    }

    /**
     * Create an operation that will retrieve the given content key.
     * 
     * @param contentKeyId
     *            id of content key to retrieve
     * @return the operation
     */
    public static EntityGetOperation<ContentKeyInfo> get(String contentKeyId) {
        return new DefaultGetOperation<ContentKeyInfo>(ENTITY_SET,
                contentKeyId, ContentKeyInfo.class);
    }

    /**
     * Create an operation that will retrieve all access policies.
     * 
     * @return the operation
     */
    public static DefaultListOperation<ContentKeyInfo> list() {
        return new DefaultListOperation<ContentKeyInfo>(ENTITY_SET,
                new GenericType<ListResult<ContentKeyInfo>>() {
                });
    }

    /**
     * Create an operation that will list all the content keys at the given
     * link.
     * 
     * @param link
     *            Link to request content keys from.
     * @return The list operation.
     */
    public static DefaultListOperation<ContentKeyInfo> list(
            LinkInfo<ContentKeyInfo> link) {
        return new DefaultListOperation<ContentKeyInfo>(link.getHref(),
                new GenericType<ListResult<ContentKeyInfo>>() {
                });
    }

    /**
     * Create an operation to delete the given content key.
     * 
     * @param contentKeyId
     *            id of content key to delete
     * @return the delete operation
     */
    public static EntityDeleteOperation delete(String contentKeyId) {
        return new DefaultDeleteOperation(ENTITY_SET, contentKeyId);
    }

    /**
     * Rebind content key with specified content key and X509 Certificate.
     * 
     * @param contentKeyId
     *            the content key id
     * @param x509Certificate
     *            the x509 certificate
     * @return the entity action operation
     */
    public static EntityTypeActionOperation<String> rebind(String contentKeyId,
            String x509Certificate) {
        return new RebindContentKeyActionOperation(contentKeyId,
                x509Certificate);
    }

    /**
     * Rebind content key with specified content key Id.
     * 
     * @param contentKeyId
     *            the content key id
     * @return the entity action operation
     */
    public static EntityTypeActionOperation<String> rebind(String contentKeyId) {
        return rebind(contentKeyId, "");
    }

    private static class RebindContentKeyActionOperation extends
            DefaultEntityTypeActionOperation<String> {
        private final JAXBContext jaxbContext;

        private final Unmarshaller unmarshaller;

        public RebindContentKeyActionOperation(String contentKeyId,
                String x509Certificate) {
            super("RebindContentKey");

            String escapedContentKeyId;
            try {
                escapedContentKeyId = URLEncoder.encode(contentKeyId, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new InvalidParameterException(
                        "UTF-8 encoding is not supported.");
            }
            this.addQueryParameter("x509Certificate", "'" + x509Certificate
                    + "'");
            this.addQueryParameter("id", "'" + escapedContentKeyId + "'");

            try {
                jaxbContext = JAXBContext
                        .newInstance(RebindContentKeyType.class);
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }

            try {
                unmarshaller = jaxbContext.createUnmarshaller();
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String processTypeResponse(ClientResponse clientResponse) {
            PipelineHelpers.throwIfNotSuccess(clientResponse);
            RebindContentKeyType rebindContentKeyType = parseResponse(clientResponse);
            return rebindContentKeyType.getContentKey();
        }

        private RebindContentKeyType parseResponse(ClientResponse clientResponse) {
            InputStream inputStream = clientResponse.getEntityInputStream();
            JAXBElement<RebindContentKeyType> rebindContentKeyTypeJaxbElement;
            try {
                rebindContentKeyTypeJaxbElement = unmarshaller.unmarshal(
                        new StreamSource(inputStream),
                        RebindContentKeyType.class);
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
            return rebindContentKeyTypeJaxbElement.getValue();
        }

    }
    
    
    public static KeyDeliveryUrlGetter getKeyDeliveryUrl(String contentKeyId, ContentKeyDeliveryType contentKeyDeliveryType) {
        return new KeyDeliveryUrlGetter(contentKeyId, contentKeyDeliveryType);
    }
    
    private static class KeyDeliveryUrlGetter extends
            EntityOperationSingleResultBase<String> implements
            EntityCreateOperation<String> {
        /** The contentKeyId */
        private final String contentKeyId;
     
        /** content Key delivery type */
        private final ContentKeyDeliveryType contentKeyDeliveryType;
        
        public KeyDeliveryUrlGetter(String contentKeyId,
                ContentKeyDeliveryType contentKeyDeliveryType) {
            super(ENTITY_SET, String.class);
            
            this.contentKeyId = contentKeyId;
            this.contentKeyDeliveryType = contentKeyDeliveryType;
        }
                
        @Override
        public String getUri() {
            String escapedEntityId;
            try {
                escapedEntityId = URLEncoder.encode(contentKeyId, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new InvalidParameterException(
                        "UTF-8 encoding is not supported.");
            }
            return String.format("%s('%s')/GetKeyDeliveryUrl", ENTITY_SET, escapedEntityId);
        }

        @Override
        public Object getRequestContents() throws ServiceException {
            JSONObject document = new JSONObject();
            try {
                document.put("keyDeliveryType", contentKeyDeliveryType.getCode());
            } catch (JSONException e) {
                throw new ServiceException("JSON Exception", e);
            }
            return document.toString();
        }
        
        @Override
        public MediaType getContentType() {
            return MediaType.APPLICATION_JSON_TYPE;
        }
        
        @Override
        public MediaType getAcceptType() {
            return MediaType.APPLICATION_JSON_TYPE;
        }

        @Override
        public Object processResponse(Object rawResponse) throws ServiceException {
            try {
                JSONObject object = new JSONObject(rawResponse.toString());
                return object.getString("value");
            } catch (JSONException e) {
                throw new ServiceException(e);
            }
        }
    }    
    
    /** Updates a ContentKey with an ContentKeyAuthorizationPolicyId
     * 
     * @param contentKeyId  The id of the ContentKey to be updated.
     * @param contentKeyAuthorizationPolicyId The id of the ContentKeyAuthorizationPolicy
     * @return Entity Operation
     */
    public static Updater update(String contentKeyId, String contentKeyAuthorizationPolicyId) {
        return new Updater(contentKeyId, contentKeyAuthorizationPolicyId);
    }
    
    public static class Updater extends EntityOperationBase implements
        EntityUpdateOperation {
        
        private String contentKeyAuthorizationPolicyId;
        
        protected Updater(String contentKeyId, String contentKeyAuthorizationPolicyId) {
            super(new EntityOperationBase.EntityIdUriBuilder(ENTITY_SET,
                    contentKeyId));            
            this.contentKeyAuthorizationPolicyId = contentKeyAuthorizationPolicyId;
        }
        
        @Override
        public MediaType getContentType() {
            return MediaType.APPLICATION_JSON_TYPE;
        }
        
        @Override
        public Object getRequestContents() {
            JSONObject document = new JSONObject();
            try {
                document.put("AuthorizationPolicyId", contentKeyAuthorizationPolicyId);
            } catch (JSONException e) {
                throw new RuntimeException("JSON Exception", e);
            }
            return document.toString();
        }

    }
}
