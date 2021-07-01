// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.repository.support;

import com.azure.cosmos.models.CompositePath;
import com.azure.cosmos.models.ExcludedPath;
import com.azure.cosmos.models.IncludedPath;
import com.azure.cosmos.models.IndexingMode;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.spring.data.cosmos.Constants;
import com.azure.spring.data.cosmos.core.mapping.CompositeIndex;
import com.azure.spring.data.cosmos.core.mapping.CompositeIndexPath;
import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.CosmosIndexingPolicy;
import com.azure.spring.data.cosmos.common.Memoizer;
import com.azure.spring.data.cosmos.core.mapping.GeneratedValue;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Persistable;
import org.springframework.data.repository.core.support.AbstractEntityInformation;
import org.springframework.lang.NonNull;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static com.azure.spring.data.cosmos.common.ExpressionResolver.resolveExpression;

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
    private final boolean autoCreateContainer;
    private final boolean autoGenerateId;
    private final boolean persitable;
    private final boolean autoScale;
    private final boolean isIndexingPolicySpecified;


    /**
     * Initialization
     *
     * @param domainType to specify id field
     */
    public CosmosEntityInformation(Class<T> domainType) {
        super(domainType);

        this.id = getIdField(domainType);
        ReflectionUtils.makeAccessible(this.id);

        this.autoGenerateId = isIdFieldAnnotatedWithGeneratedValue(this.id);

        this.containerName = getContainerName(domainType);
        this.partitionKeyPath = getPartitionKeyPathAnnotationValue(domainType);

        this.partitionKeyField = getPartitionKeyField(domainType);
        if (this.partitionKeyField != null) {
            ReflectionUtils.makeAccessible(this.partitionKeyField);
        }

        this.versionField = getVersionedField(domainType);
        if (this.versionField != null) {
            ReflectionUtils.makeAccessible(this.versionField);
        }

        this.requestUnit = getRequestUnit(domainType);
        this.timeToLive = getTimeToLive(domainType);
        this.indexingPolicy = getIndexingPolicy(domainType);
        this.autoCreateContainer = getIsAutoCreateContainer(domainType);
        this.persitable = Persistable.class.isAssignableFrom(domainType);
        this.autoScale = getIsAutoScale(domainType);
        this.isIndexingPolicySpecified = isIndexingPolicySpecified(domainType);
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
     * Check if container should use autoscale for resource units
     *
     * @return boolean
     */
    public boolean isAutoScale() {
        return autoScale;
    }

    public boolean isIndexingPolicySpecified() {
        return this.isIndexingPolicySpecified;
    }

    private boolean isIndexingPolicySpecified(Class<?> domainType) {
        return domainType.getAnnotation(CosmosIndexingPolicy.class) != null;
    }

    private IndexingPolicy getIndexingPolicy(Class<?> domainType) {
        final IndexingPolicy policy = new IndexingPolicy();

        policy.setAutomatic(this.getIndexingPolicyAutomatic(domainType));
        policy.setIndexingMode(this.getIndexingPolicyMode(domainType));
        policy.setIncludedPaths(this.getIndexingPolicyIncludePaths(domainType));
        policy.setExcludedPaths(this.getIndexingPolicyExcludePaths(domainType));
        policy.setCompositeIndexes(this.getIndexingPolicyCompositeIndexes(domainType));

        return policy;
    }

    private Field getIdField(Class<?> domainType) {
        final Field idField;
        final List<Field> fields = FieldUtils.getFieldsListWithAnnotation(domainType, Id.class);

        if (fields.isEmpty()) {
            idField = ReflectionUtils.findField(getJavaType(), Constants.ID_PROPERTY_NAME);
        } else if (fields.size() == 1) {
            idField = fields.get(0);
        } else {
            throw new IllegalArgumentException("only one field with @Id annotation!");
        }

        if (idField == null) {
            throw new IllegalArgumentException("domain should contain @Id field or field named id");
        } else if (idField.getType() != String.class
                && idField.getType() != Integer.class
                && idField.getType() != int.class
                && idField.getType() != Long.class
                && idField.getType() != long.class) {
            throw new IllegalArgumentException("type of id field must be String, Integer or Long");
        }

        return idField;
    }

    private boolean isIdFieldAnnotatedWithGeneratedValue(Field idField) {
        if (id.getAnnotation(GeneratedValue.class) != null) {
            if (idField.getType() == String.class) {
                return true;
            } else {
                throw new IllegalArgumentException("id field must be of type String if "
                    + "GeneratedValue annotation is present");
            }
        }
        return false;
    }

    private String getContainerName(Class<?> domainType) {
        String customContainerName = domainType.getSimpleName();

        final Container annotation = domainType.getAnnotation(Container.class);

        if (annotation != null && !annotation.containerName().isEmpty()) {
            customContainerName = resolveExpression(annotation.containerName());
        }

        return customContainerName;
    }

    private String getPartitionKeyPathAnnotationValue(Class<?> domainType) {
        final Container annotation = domainType.getAnnotation(Container.class);

        if (annotation != null && !annotation.partitionKeyPath().isEmpty()) {
            return annotation.partitionKeyPath();
        }
        return null;
    }

    private Field getPartitionKeyField(Class<?> domainType) {
        Field partitionKey = null;

        final List<Field> fields = FieldUtils.getFieldsListWithAnnotation(domainType, PartitionKey.class);

        if (fields.size() == 1) {
            partitionKey = fields.get(0);
        } else if (fields.size() > 1) {
            throw new IllegalArgumentException("Azure Cosmos DB supports only one partition key, "
                + "only one field with @PartitionKey annotation!");
        }
        return partitionKey;
    }

    private Integer getRequestUnit(Class<?> domainType) {
        Integer ru = null;
        final Container annotation = domainType.getAnnotation(Container.class);

        if (annotation != null
                && annotation.ru() != null
                && !annotation.ru().isEmpty()) {
            ru = Integer.parseInt(annotation.ru());
        }
        return ru;
    }

    private Integer getTimeToLive(Class<T> domainType) {
        Integer ttl = Constants.DEFAULT_TIME_TO_LIVE;
        final Container annotation = domainType.getAnnotation(Container.class);

        if (annotation != null) {
            ttl = annotation.timeToLive();
        }

        return ttl;
    }


    private Boolean getIndexingPolicyAutomatic(Class<?> domainType) {
        Boolean isAutomatic = Boolean.valueOf(Constants.DEFAULT_INDEXING_POLICY_AUTOMATIC);
        final CosmosIndexingPolicy annotation = domainType.getAnnotation(CosmosIndexingPolicy.class);

        if (annotation != null) {
            isAutomatic = Boolean.valueOf(annotation.automatic());
        }

        return isAutomatic;
    }

    private IndexingMode getIndexingPolicyMode(Class<?> domainType) {
        IndexingMode mode = Constants.DEFAULT_INDEXING_POLICY_MODE;
        final CosmosIndexingPolicy annotation = domainType.getAnnotation(CosmosIndexingPolicy.class);

        if (annotation != null) {
            mode = annotation.mode();
        }

        return mode;
    }

    private List<IncludedPath> getIndexingPolicyIncludePaths(Class<?> domainType) {
        final List<IncludedPath> pathArrayList = new ArrayList<>();
        final CosmosIndexingPolicy annotation = domainType.getAnnotation(CosmosIndexingPolicy.class);

        if (annotation == null || annotation.includePaths().length == 0) {
            return null; // Align the default value of IndexingPolicy
        }

        final String[] rawPaths = annotation.includePaths();

        for (final String path : rawPaths) {
            pathArrayList.add(new IncludedPath(path));
        }

        return pathArrayList;
    }

    private List<ExcludedPath> getIndexingPolicyExcludePaths(Class<?> domainType) {
        final List<ExcludedPath> pathArrayList = new ArrayList<>();
        final CosmosIndexingPolicy annotation = domainType.getAnnotation(CosmosIndexingPolicy.class);

        if (annotation == null || annotation.excludePaths().length == 0) {
            return null; // Align the default value of IndexingPolicy
        }

        final String[] rawPaths = annotation.excludePaths();
        for (final String path : rawPaths) {
            pathArrayList.add(new ExcludedPath(path));
        }

        return pathArrayList;
    }

    private List<List<CompositePath>> getIndexingPolicyCompositeIndexes(Class<?> domainType) {
        final List<List<CompositePath>> compositePathList = new ArrayList<>();
        final CosmosIndexingPolicy annotation = domainType.getAnnotation(CosmosIndexingPolicy.class);

        if (annotation == null || annotation.compositeIndexes().length == 0) {
            return Collections.emptyList();
        }

        final CompositeIndex[] compositeIndexes = annotation.compositeIndexes();
        for (final CompositeIndex index: compositeIndexes) {
            final List<CompositePath> paths = new ArrayList<>();
            compositePathList.add(paths);
            for (final CompositeIndexPath path : index.paths()) {
                CompositePath compositePath = new CompositePath();
                compositePath.setPath(path.path());
                compositePath.setOrder(path.order());
                paths.add(compositePath);
            }
        }

        return compositePathList;
    }

    private Field getVersionedField(Class<T> domainClass) {
        Field version = null;
        final List<Field> fields = FieldUtils.getFieldsListWithAnnotation(domainClass, Version.class);

        if (fields.size() == 1) {
            version = fields.get(0);
        } else if (fields.size() > 1) {
            throw new IllegalArgumentException("Azure Cosmos DB supports only one field with @Version annotation!");
        }

        if (version != null && version.getType() != String.class) {
            throw new IllegalArgumentException("type of Version field must be String");
        }
        return version;
    }

    private boolean getIsAutoCreateContainer(Class<T> domainType) {
        final Container annotation = domainType.getAnnotation(Container.class);

        boolean autoCreateContainer = Constants.DEFAULT_AUTO_CREATE_CONTAINER;
        if (annotation != null) {
            autoCreateContainer = annotation.autoCreateContainer();
        }

        return autoCreateContainer;
    }

    private boolean getIsAutoScale(Class<T> domainType) {
        final Container annotation = domainType.getAnnotation(Container.class);

        boolean autoScale = Constants.DEFAULT_AUTO_SCALE;
        if (annotation != null) {
            autoScale = annotation.autoScale();
        }

        return autoScale;
    }
}

