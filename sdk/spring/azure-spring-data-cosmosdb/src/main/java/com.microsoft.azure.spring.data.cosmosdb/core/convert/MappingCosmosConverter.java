// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.core.convert;

import com.azure.data.cosmos.CosmosItemProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.spring.data.cosmosdb.Constants;
import com.microsoft.azure.spring.data.cosmosdb.core.mapping.CosmosPersistentEntity;
import com.microsoft.azure.spring.data.cosmosdb.core.mapping.CosmosPersistentProperty;
import com.microsoft.azure.spring.data.cosmosdb.exception.CosmosDBAccessException;
import org.json.JSONObject;
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

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static com.microsoft.azure.spring.data.cosmosdb.Constants.ISO_8601_COMPATIBLE_DATE_PATTERN;

/**
 * A converter class between common types and cosmosItemProperties
 */
@SuppressWarnings("unchecked")
public class MappingCosmosConverter
    implements EntityConverter<CosmosPersistentEntity<?>, CosmosPersistentProperty,
    Object, CosmosItemProperties>,
    ApplicationContextAware {

    protected final MappingContext<? extends CosmosPersistentEntity<?>,
                                          CosmosPersistentProperty> mappingContext;
    protected GenericConversionService conversionService;
    private ApplicationContext applicationContext;
    private ObjectMapper objectMapper;

    /**
     * Initialization
     * @param mappingContext must not be {@literal null}
     * @param objectMapper must not be {@literal null}
     */
    public MappingCosmosConverter(
        MappingContext<? extends CosmosPersistentEntity<?>, CosmosPersistentProperty> mappingContext,
        @Qualifier(Constants.OBJECTMAPPER_BEAN_NAME) ObjectMapper objectMapper) {
        this.mappingContext = mappingContext;
        this.conversionService = new GenericConversionService();
        this.objectMapper = objectMapper == null ? ObjectMapperFactory.getObjectMapper()
            : objectMapper;
    }

    @Override
    public <R> R read(Class<R> type, CosmosItemProperties cosmosItemProperties) {
        if (cosmosItemProperties == null) {
            return null;
        }

        final CosmosPersistentEntity<?> entity = mappingContext.getPersistentEntity(type);
        Assert.notNull(entity, "Entity is null.");

        return readInternal(entity, type, cosmosItemProperties);
    }

    private <R> R readInternal(final CosmosPersistentEntity<?> entity, Class<R> type,
                               final CosmosItemProperties cosmosItemProperties) {

        try {
            final CosmosPersistentProperty idProperty = entity.getIdProperty();
            final Object idValue = cosmosItemProperties.id();
            final JSONObject jsonObject = new JSONObject(cosmosItemProperties.toJson());

            if (idProperty != null) {
                // Replace the key id to the actual id field name in domain
                jsonObject.remove(Constants.ID_PROPERTY_NAME);
                jsonObject.put(idProperty.getName(), idValue);
            }

            return objectMapper.readValue(jsonObject.toString(), type);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read the source document "
                + cosmosItemProperties.toJson()
                + "  to target type "
                + type, e);
        }
    }

    @Override
    @Deprecated
    public void write(Object sourceEntity, CosmosItemProperties document) {
        throw new UnsupportedOperationException("The feature is not implemented yet");
    }

    /**
     * To write source entity as a cosmos item
     * @param sourceEntity must not be {@literal null}
     * @return CosmosItemProperties
     * @throws MappingException no mapping metadata for entity type
     * @throws CosmosDBAccessException fail to map document value
     */
    public CosmosItemProperties writeCosmosItemProperties(Object sourceEntity) {
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
        final CosmosItemProperties cosmosItemProperties;

        try {
            cosmosItemProperties =
                new CosmosItemProperties(objectMapper.writeValueAsString(sourceEntity));
        } catch (JsonProcessingException e) {
            throw new CosmosDBAccessException("Failed to map document value.", e);
        }

        if (idProperty != null) {
            final Object value = accessor.getProperty(idProperty);
            final String id = value == null ? null : value.toString();
            cosmosItemProperties.id(id);
        }

        return cosmosItemProperties;
    }

    /**
     * To get application context
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
     * @return MappingContext
     */
    public MappingContext<? extends CosmosPersistentEntity<?>, CosmosPersistentProperty> getMappingContext() {
        return mappingContext;
    }


    private ConvertingPropertyAccessor<?> getPropertyAccessor(Object entity) {
        final CosmosPersistentEntity<?> entityInformation =
            mappingContext.getPersistentEntity(entity.getClass());

        Assert.notNull(entityInformation, "EntityInformation should not be null.");
        final PersistentPropertyAccessor<?> accessor = entityInformation.getPropertyAccessor(entity);
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

        // com.microsoft.azure.data.cosmos.JsonSerializable#set(String, T) cannot set values for Date and Enum correctly

        if (fromPropertyValue instanceof Date) {
            fromPropertyValue = ((Date) fromPropertyValue).getTime();
        } else if (fromPropertyValue instanceof ZonedDateTime) {
            fromPropertyValue = ((ZonedDateTime) fromPropertyValue)
                                        .format(DateTimeFormatter.ofPattern(ISO_8601_COMPATIBLE_DATE_PATTERN));
        } else if (fromPropertyValue instanceof Enum) {
            fromPropertyValue = fromPropertyValue.toString();
        }

        return fromPropertyValue;
    }
}
