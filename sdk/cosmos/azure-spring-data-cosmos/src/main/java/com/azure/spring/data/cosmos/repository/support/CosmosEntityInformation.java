// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.repository.support;

import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.UniqueKeyPolicy;
import com.azure.spring.data.cosmos.common.Memoizer;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import org.springframework.data.domain.Persistable;
import org.springframework.data.repository.core.support.AbstractEntityInformation;
import org.springframework.lang.NonNull;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.function.Function;

/**
 * Class to describe cosmosDb entity
 */
public class CosmosEntityInformation<T, ID> extends AbstractEntityInformation<T, ID> {

    private static final Function<Class<?>, CosmosEntityInformation<?, ?>> ENTITY_INFORMATION_CREATOR =
        Memoizer.memoize(CosmosEntityInformation::getCosmosEntityInformation);

    private static CosmosEntityInformation<?, ?> getCosmosEntityInformation(Class<?> domainClass) {
        return new CosmosEntityInformation<>(domainClass);
    }

    /**
     * Static Factory
     *
     * @param domainClass to specify id field
     * @return new CosmosEntityInformation
     */
    public static CosmosEntityInformation<?, ?> getInstance(Class<?> domainClass) {
        return ENTITY_INFORMATION_CREATOR.apply(domainClass);
    }

    private final Field id;
    private final Field partitionKeyField;
    private final Field versionField;
    private final String containerName;
    private final String partitionKeyPath;
    private final Integer requestUnit;
    private final Integer timeToLive;
    private final IndexingPolicy indexingPolicy;
    private final UniqueKeyPolicy uniqueKeyPolicy;
    private final boolean autoCreateContainer;
    private final boolean autoGenerateId;
    private final boolean persitable;
    private final boolean autoScale;
    private final boolean isIndexingPolicySpecified;
    private final boolean overwriteIndexingPolicy;

    /**
     * Initialization
     *
     * @param domainType to specify id field
     */
    public CosmosEntityInformation(Class<T> domainType) {
        super(domainType);

        this.id = CosmosEntityInformationHelper.getIdField(domainType, getJavaType());
        ReflectionUtils.makeAccessible(this.id);

        this.autoGenerateId = CosmosEntityInformationHelper.isIdFieldAnnotatedWithGeneratedValue(this.id);

        this.containerName = CosmosEntityInformationHelper.getContainerName(domainType);
        this.partitionKeyPath = CosmosEntityInformationHelper.getPartitionKeyPathAnnotationValue(domainType);

        this.partitionKeyField = CosmosEntityInformationHelper.getPartitionKeyField(domainType);
        if (this.partitionKeyField != null) {
            ReflectionUtils.makeAccessible(this.partitionKeyField);
        }

        this.versionField = CosmosEntityInformationHelper.getVersionedField(domainType);
        if (this.versionField != null) {
            ReflectionUtils.makeAccessible(this.versionField);
        }

        this.requestUnit = CosmosEntityInformationHelper.getRequestUnit(domainType);
        this.timeToLive = CosmosEntityInformationHelper.getTimeToLive(domainType);
        this.indexingPolicy = CosmosEntityInformationHelper.getIndexingPolicy(domainType);
        this.uniqueKeyPolicy = CosmosEntityInformationHelper.getUniqueKeyPolicy(domainType);
        this.autoCreateContainer = CosmosEntityInformationHelper.getIsAutoCreateContainer(domainType);
        this.persitable = Persistable.class.isAssignableFrom(domainType);
        this.autoScale = CosmosEntityInformationHelper.getIsAutoScale(domainType);
        this.isIndexingPolicySpecified = CosmosEntityInformationHelper.isIndexingPolicySpecified(domainType);
        this.overwriteIndexingPolicy = CosmosEntityInformationHelper.getIndexingPolicyOverwritePolicy(domainType);
    }

    @Override
    public boolean isNew(T entity) {
        if (persitable) {
            return ((Persistable) entity).isNew();
        } else {
            return super.isNew(entity);
        }
    }

    /**
     * Get the field represented by the supplied id field on the
     * specified entity.
     *
     * @param entity the target object from which to get the field
     * @return the id's current value
     */
    @SuppressWarnings("unchecked")
    public ID getId(T entity) {
        return (ID) ReflectionUtils.getField(id, entity);
    }

    /**
     * Get id field
     *
     * @return id
     */
    public Field getIdField() {
        return this.id;
    }

    /**
     * Get id field name
     *
     * @return string
     */
    public String getIdFieldName() {
        return id.getName();
    }

    /**
     * Should generate Id field value
     *
     * @return boolean
     */
    public boolean shouldGenerateId() {
        return autoGenerateId;
    }

    /**
     * Get id type
     *
     * @return class of id type
     */
    @SuppressWarnings("unchecked")
    public Class<ID> getIdType() {
        return (Class<ID>) id.getType();
    }

    /**
     * Get container name
     *
     * @return container name
     */
    public String getContainerName() {
        return this.containerName;
    }

    /**
     * Get request unit value
     *
     * @return request unit
     */
    public Integer getRequestUnit() {
        return this.requestUnit;
    }

    /**
     * Get timeToLive value
     *
     * @return timeToLive
     */
    public Integer getTimeToLive() {
        return this.timeToLive;
    }

    /**
     * Get indexing policy
     *
     * @return IndexingPolicy
     */
    @NonNull
    public IndexingPolicy getIndexingPolicy() {
        return this.indexingPolicy;
    }

    /**
     * Gets the UniqueKeyPolicy
     * @return UniqueKeyPolicy
     */
    public UniqueKeyPolicy getUniqueKeyPolicy() {
        return uniqueKeyPolicy;
    }

    /**
     * Check if is versioned
     *
     * @return boolean
     */
    public boolean isVersioned() {
        return versionField != null;
    }

    /**
     * Get name of field annotated with @Version if any
     *
     * @return String
     */
    public String getVersionFieldName() {
        return versionField == null ? null : versionField.getName();
    }

    /**
     * Get the computed partition key path for container
     *
     * @return partition key path
     */
    public String getPartitionKeyPath() {
        if (partitionKeyField == null) {
            return partitionKeyPath == null ? "/null" : partitionKeyPath;
        } else {
            final PartitionKey partitionKey = partitionKeyField.getAnnotation(PartitionKey.class);
            return partitionKey.value().equals("") ? "/" + partitionKeyField.getName() : "/" + partitionKey.value();
        }
    }

    /**
     * Get the value of the field marked as the version field
     *
     * @param entity the object to get the value from
     * @return the value of the version field
     */
    public String getVersionFieldValue(Object entity) {
        return versionField == null ? null : (String) ReflectionUtils.getField(versionField, entity);
    }

    /**
     * Get the field value represented by the supplied partitionKeyField object on the
     * specified entity object.
     *
     * @param entity the target object from which to get the field
     * @return partition key field
     */
    public Object getPartitionKeyFieldValue(T entity) {
        return partitionKeyField == null ? null : ReflectionUtils.getField(partitionKeyField, entity);
    }

    /**
     * @return the partition key field name
     */
    public String getPartitionKeyFieldName() {
        return partitionKeyField == null ? null : partitionKeyField.getName();
    }

    /**
     * Check if auto creating container is allowed
     *
     * @return boolean
     */
    public boolean isAutoCreateContainer() {
        return autoCreateContainer;
    }

    /**
     * Check if overwrite indexing policy is enabled
     *
     * @return boolean
     */
    public boolean isOverwriteIndexingPolicy() {
        return overwriteIndexingPolicy;
    }

    /**
     * Check if container should use autoscale for resource units
     *
     * @return boolean
     */
    public boolean isAutoScale() {
        return autoScale;
    }

    /**
     * @return whether indexing policy is specified
     */
    public boolean isIndexingPolicySpecified() {
        return this.isIndexingPolicySpecified;
    }
}

