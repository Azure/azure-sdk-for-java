// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core.convert;

import com.azure.spring.data.cosmos.Constants;
import com.azure.spring.data.cosmos.core.mapping.CosmosPersistentEntity;
import com.azure.spring.data.cosmos.core.mapping.CosmosPersistentProperty;
import com.azure.spring.data.cosmos.exception.CosmosAccessException;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.convert.EntityConverter;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.ConvertingPropertyAccessor;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.azure.spring.data.cosmos.Constants.ISO_8601_COMPATIBLE_DATE_PATTERN;

/**
 * A converter class between common types and cosmosItemProperties
 */
public class MappingCosmosConverter
    implements EntityConverter<CosmosPersistentEntity<?>, CosmosPersistentProperty, Object,
    JsonNode>,
    ApplicationContextAware {

    /**
     * Mapping context
     */
    protected final MappingContext<? extends CosmosPersistentEntity<?>, CosmosPersistentProperty> mappingContext;

    /**
     * Generic conversion service
     */
    protected GenericConversionService conversionService;
    private ApplicationContext applicationContext;
    private final ObjectMapper objectMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(MappingCosmosConverter.class);

    /**
     * Initialization
     *
     * @param mappingContext must not be {@literal null}
     * @param objectMapper must not be {@literal null}
     */
    public MappingCosmosConverter(
        MappingContext<? extends CosmosPersistentEntity<?>, CosmosPersistentProperty> mappingContext,
        @Qualifier(Constants.OBJECT_MAPPER_BEAN_NAME) ObjectMapper objectMapper) {
        this.mappingContext = mappingContext;
        this.conversionService = new GenericConversionService();
        this.objectMapper = objectMapper == null ? ObjectMapperFactory.getObjectMapper()
            : objectMapper;
    }

    @Override
    public <R> R read(Class<R> type, JsonNode jsonNode) {
        final CosmosPersistentEntity<?> entity = mappingContext.getPersistentEntity(type);
        return readInternal(entity, type, jsonNode);
    }

    @Override
    public void write(Object source, JsonNode sink) {
        throw new UnsupportedOperationException("The feature is not implemented yet");
    }

    private <R> R readInternal(final CosmosPersistentEntity<?> entity, Class<R> type,
                               final JsonNode jsonNode) {
        try {
            if (jsonNode.isValueNode()) {
                return objectMapper.treeToValue(jsonNode, type);
            }

            Assert.notNull(entity, "Entity is null.");
            final ObjectNode objectNode = jsonNode.deepCopy();
            final CosmosPersistentProperty idProperty = entity.getIdProperty();
            final JsonNode idValue = jsonNode.get("id");
            if (idProperty != null) {
                // Replace the key id to the actual id field name in domain
                objectNode.remove(Constants.ID_PROPERTY_NAME);
                objectNode.set(idProperty.getName(), idValue);
            }
            final JsonNode etag = jsonNode.get(Constants.ETAG_PROPERTY_DEFAULT_NAME);
            if (etag != null) {
                mapEtagToVersionField(type, objectNode, etag);
            }
            return objectMapper.treeToValue(objectNode, type);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to read the source document "
                + jsonNode.toPrettyString()
                + "  to target type "
                + type, e);
        }
    }

    /**
     * To write source entity as a cosmos item
     *
     * @param sourceEntity must not be {@literal null}
     * @return CosmosItemProperties
     * @throws MappingException no mapping metadata for entity type
     * @throws CosmosAccessException fail to map document value
     */

    public JsonNode writeJsonNode(Object sourceEntity) {
        return writeJsonNode(sourceEntity, null);
    }

    /**
     * To write source entity as a cosmos item
     *
     * @param sourceEntity must not be {@literal null}
     * @param transientFields transient fields
     * @param <T> type of source entity
     * @return CosmosItemProperties
     * @throws MappingException no mapping metadata for entity type
     * @throws CosmosAccessException fail to map document value
     */
    public <T> JsonNode writeJsonNode(Object sourceEntity, List<String> transientFields) {

        if (sourceEntity == null) {
            return null;
        }

        final CosmosPersistentEntity<?> persistentEntity =
            mappingContext.getPersistentEntity(sourceEntity.getClass());

        if (persistentEntity == null) {
            throw new MappingException("no mapping metadata for entity type: "
                + sourceEntity.getClass().getName());
        }

        final ConvertingPropertyAccessor<?> accessor = getPropertyAccessor(sourceEntity);
        final CosmosPersistentProperty idProperty = persistentEntity.getIdProperty();
        ObjectNode cosmosObjectNode;

        try {
            final String valueAsString = objectMapper.writeValueAsString(sourceEntity);
            cosmosObjectNode = (ObjectNode) objectMapper.readTree(valueAsString);
        } catch (JsonProcessingException e) {
            throw new CosmosAccessException("Failed to map document value.", e);
        }

        if (idProperty != null) {
            final Object value = accessor.getProperty(idProperty);
            final String id = value == null ? null : value.toString();
            cosmosObjectNode.put("id", id);
        }
        if (transientFields != null && !transientFields.isEmpty()) {
            // Strip fields with @Transient annotation from the JsonNode so they are not persisted.
            cosmosObjectNode = stripTransientFields(cosmosObjectNode, transientFields);
        }

        mapVersionFieldToEtag(sourceEntity, cosmosObjectNode);

        return cosmosObjectNode;
    }

    /**
     * To repopulate any transient fields that were stripped from the original object
     * @param <T> type of source entity
     * @param transientValues transient fields and their values
     * @param responseItem must not be {@literal null}
     * @return CosmosItemProperties
     * @throws MappingException no mapping metadata for entity type
     * @throws CosmosAccessException fail to map document value
     */
    public <T> JsonNode repopulateTransientFields(JsonNode responseItem, Map<Field, Object> transientValues) {
        ObjectNode updatedItem = (ObjectNode) responseItem;
        transientValues.forEach((field, value) -> {
            if (value != null) {
                updatedItem.set(field.getName(), objectMapper.valueToTree(value));
            }
        });
        return updatedItem;
    }

    /**
     * To get transient fields and their values
     * @param <T> type of source entity
     * @param object must not be {@literal null}
     * @param transientFields transient fields
     * @throws MappingException no such field found
     * @return HashMap
     */
    public <T> Map<Field, Object> getTransientFieldsMap(T object, List<String> transientFields) {
        Map<Field, Object> transientValuesMap = new HashMap<>();
        transientFields.forEach(fieldName -> {
            try {
                Field field = object.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                transientValuesMap.put(field, field.get(object));
            } catch (NoSuchFieldException e) {
                throw new MappingException("No such field found: " + fieldName, e);
            } catch (IllegalAccessException e) {
                throw new MappingException("Illegal access to field: " + fieldName, e);
            }
        });
        return transientValuesMap;
    }

    /**
     * To get transient fields
     * @param <T> type of source entity
     * @param objectToSave must not be {@literal null}
     * @param entityInfo entity information
     * @return List
     */
    public <T> List<String> getTransientFields(T objectToSave, CosmosEntityInformation<T, ?> entityInfo) {
        @SuppressWarnings("unchecked")
        Class<T> domainType = (Class<T>) objectToSave.getClass();
        if (entityInfo == null) {
            @SuppressWarnings("unchecked")
            CosmosEntityInformation<T, ?> entityInformation = (CosmosEntityInformation<T, Object>) CosmosEntityInformation.getInstance(domainType);
            entityInfo = entityInformation;
        }
        return entityInfo.getTransientFields();
    }

    private ObjectNode stripTransientFields(ObjectNode objectNode, List<String> transientFields) {
        objectNode.remove(transientFields);
        return objectNode;
    }

    //the field on the underlying cosmos document will always be _etag, so we map the field that the
    //user has marked with @version to _etag and remove the @version annotated field from the
    //object if the field is not named _etag
    private void mapVersionFieldToEtag(Object sourceEntity, ObjectNode cosmosObjectNode) {
        final CosmosEntityInformation<?, ?> entityInfo = CosmosEntityInformation.getInstance(sourceEntity.getClass());
        if (entityInfo.isVersioned()) {
            if (!entityInfo.getVersionFieldName().equals(Constants.ETAG_PROPERTY_DEFAULT_NAME)) {
                cosmosObjectNode.remove(entityInfo.getVersionFieldName());
                cosmosObjectNode.put(Constants.ETAG_PROPERTY_DEFAULT_NAME,
                    entityInfo.getVersionFieldValue(sourceEntity));
            }
        }
    }

    private <R> void mapEtagToVersionField(Class<R> type, ObjectNode objectNode, JsonNode etagValue) {
        final CosmosEntityInformation<?, ?> entityInfo = CosmosEntityInformation.getInstance(type);
        if (entityInfo.isVersioned()) {
            objectNode.set(entityInfo.getVersionFieldName(), etagValue);
            if (!entityInfo.getVersionFieldName().equals(Constants.ETAG_PROPERTY_DEFAULT_NAME)) {
                objectNode.remove(Constants.ETAG_PROPERTY_DEFAULT_NAME);
            }
        }
    }

    /**
     * To get application context
     *
     * @return ApplicationContext
     */
    public ApplicationContext getApplicationContext() {
        return this.applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public ConversionService getConversionService() {
        return conversionService;
    }

    /**
     * To get mapping context
     *
     * @return MappingContext
     */
    public MappingContext<? extends CosmosPersistentEntity<?>, CosmosPersistentProperty> getMappingContext() {
        return mappingContext;
    }


    private ConvertingPropertyAccessor<?> getPropertyAccessor(Object entity) {
        final CosmosPersistentEntity<?> entityInformation =
            mappingContext.getPersistentEntity(entity.getClass());

        Assert.notNull(entityInformation, "EntityInformation should not be null.");
        final PersistentPropertyAccessor<?> accessor =
            entityInformation.getPropertyAccessor(entity);
        return new ConvertingPropertyAccessor<>(accessor, conversionService);
    }

    /**
     * Convert a property value to the value stored in CosmosDB
     *
     * @param fromPropertyValue source property value
     * @return fromPropertyValue converted property value stored in CosmosDB
     */
    public static Object toCosmosDbValue(Object fromPropertyValue) {
        if (fromPropertyValue == null) {
            return null;
        }

        // com.microsoft.azure.data.cosmos.JsonSerializable#set(String, T) cannot set values for
        // Date and Enum correctly

        if (fromPropertyValue instanceof Date) {
            fromPropertyValue = ((Date) fromPropertyValue).getTime();
        } else if (fromPropertyValue instanceof ZonedDateTime) {
            fromPropertyValue = ((ZonedDateTime) fromPropertyValue)
                .format(DateTimeFormatter.ofPattern(ISO_8601_COMPATIBLE_DATE_PATTERN));
        } else if (fromPropertyValue instanceof Enum) {
            fromPropertyValue = fromPropertyValue.toString();
        } else if (fromPropertyValue instanceof Optional<?>) {
            fromPropertyValue = ((Optional<?>) fromPropertyValue).get();
        }

        return fromPropertyValue;
    }

}
