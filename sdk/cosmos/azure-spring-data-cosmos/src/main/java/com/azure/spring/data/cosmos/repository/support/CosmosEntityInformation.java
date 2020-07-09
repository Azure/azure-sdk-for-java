// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.repository.support;

import com.azure.data.cosmos.ExcludedPath;
import com.azure.data.cosmos.IncludedPath;
import com.azure.data.cosmos.IndexingMode;
import com.azure.data.cosmos.IndexingPolicy;
import com.azure.spring.data.cosmos.Constants;
import com.azure.spring.data.cosmos.core.mapping.Document;
import com.azure.spring.data.cosmos.core.mapping.DocumentIndexingPolicy;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import org.apache.commons.lang3.reflect.FieldUtils;

import org.json.JSONObject;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.repository.core.support.AbstractEntityInformation;
import org.springframework.lang.NonNull;
import org.springframework.util.ReflectionUtils;

import static com.azure.spring.data.cosmos.common.ExpressionResolver.resolveExpression;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to describe cosmosdb entity
 */
public class CosmosEntityInformation<T, ID> extends AbstractEntityInformation<T, ID> {

    private static final String ETAG = "_etag";
    private final Field id;
    private final Field partitionKeyField;
    private final String containerName;
    private final Integer requestUnit;
    private final Integer timeToLive;
    private final IndexingPolicy indexingPolicy;
    private final boolean isVersioned;
    private boolean autoCreateContainer;

    /**
     * Initialization
     *
     * @param domainType to specify id field
     */
    public CosmosEntityInformation(Class<T> domainType) {
        super(domainType);

        this.id = getIdField(domainType);
        ReflectionUtils.makeAccessible(this.id);

        this.containerName = getContainerName(domainType);
        this.partitionKeyField = getPartitionKeyField(domainType);
        if (this.partitionKeyField != null) {
            ReflectionUtils.makeAccessible(this.partitionKeyField);
        }

        this.requestUnit = getRequestUnit(domainType);
        this.timeToLive = getTimeToLive(domainType);
        this.indexingPolicy = getIndexingPolicy(domainType);
        this.isVersioned = getIsVersioned(domainType);
        this.autoCreateContainer = getIsAutoCreateContainer(domainType);
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
     * Get id type
     *
     * @return class of id type
     */
    @SuppressWarnings("unchecked")
    public Class<ID> getIdType() {
        return (Class<ID>) id.getType();
    }

    /**
     * Get collection name
     *
     * @return collection name
     * @deprecated Use {@link #getContainerName()} instead
     */
    @Deprecated
    public String getCollectionName() {
        return this.containerName;
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
        return isVersioned;
    }

    /**
     * Get the field name represented by the supplied partitionKeyField object
     *
     * @return partition key field name
     */
    public String getPartitionKeyFieldName() {
        if (partitionKeyField == null) {
            return null;
        } else {
            final PartitionKey partitionKey = partitionKeyField.getAnnotation(PartitionKey.class);
            return partitionKey.value().equals("") ? partitionKeyField.getName() : partitionKey.value();
        }
    }

    /**
     * Get the field value represented by the supplied partitionKeyField object on the
     * specified entity object.
     *
     * @param entity the target object from which to get the field
     * @return partition key field
     */
    public String getPartitionKeyFieldValue(T entity) {
        return partitionKeyField == null ? null : (String) ReflectionUtils.getField(partitionKeyField, entity);
    }

    /**
     * Check if auto creating collection is allowed
     *
     * @return boolean
     * @deprecated Use {@link #isAutoCreateContainer()} instead.
     */
    @Deprecated
    public boolean isAutoCreateCollection() {
        return autoCreateContainer;
    }

    /**
     * Check if auto creating container is allowed
     *
     * @return boolean
     */
    public boolean isAutoCreateContainer() {
        return autoCreateContainer;
    }

    private IndexingPolicy getIndexingPolicy(Class<?> domainType) {
        final IndexingPolicy policy = new IndexingPolicy();

        policy.automatic(this.getIndexingPolicyAutomatic(domainType));
        policy.indexingMode(this.getIndexingPolicyMode(domainType));
        policy.setIncludedPaths(this.getIndexingPolicyIncludePaths(domainType));
        policy.excludedPaths(this.getIndexingPolicyExcludePaths(domainType));

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
                && idField.getType() != int.class) {
            throw new IllegalArgumentException("type of id field must be String or Integer");
        }

        return idField;
    }

    private String getContainerName(Class<?> domainType) {
        String customContainerName = domainType.getSimpleName();

        final Document annotation = domainType.getAnnotation(Document.class);

        if (annotation != null
                && annotation.collection() != null
                && !annotation.collection().isEmpty()) {
            customContainerName = resolveExpression(annotation.collection());
        }

        return customContainerName;
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

        if (partitionKey != null
                && partitionKey.getType() != String.class) {
            throw new IllegalArgumentException("type of PartitionKey field must be String");
        }
        return partitionKey;
    }

    private Integer getRequestUnit(Class<?> domainType) {
        Integer ru = Integer.parseInt(Constants.DEFAULT_REQUEST_UNIT);
        final Document annotation = domainType.getAnnotation(Document.class);

        if (annotation != null
                && annotation.ru() != null
                && !annotation.ru().isEmpty()) {
            ru = Integer.parseInt(annotation.ru());
        }
        return ru;
    }

    private Integer getTimeToLive(Class<T> domainType) {
        Integer ttl = Constants.DEFAULT_TIME_TO_LIVE;
        final Document annotation = domainType.getAnnotation(Document.class);

        if (annotation != null) {
            ttl = annotation.timeToLive();
        }

        return ttl;
    }


    private Boolean getIndexingPolicyAutomatic(Class<?> domainType) {
        Boolean isAutomatic = Boolean.valueOf(Constants.DEFAULT_INDEXINGPOLICY_AUTOMATIC);
        final DocumentIndexingPolicy annotation = domainType.getAnnotation(DocumentIndexingPolicy.class);

        if (annotation != null) {
            isAutomatic = Boolean.valueOf(annotation.automatic());
        }

        return isAutomatic;
    }

    private IndexingMode getIndexingPolicyMode(Class<?> domainType) {
        IndexingMode mode = Constants.DEFAULT_INDEXINGPOLICY_MODE;
        final DocumentIndexingPolicy annotation = domainType.getAnnotation(DocumentIndexingPolicy.class);

        if (annotation != null) {
            mode = annotation.mode();
        }

        return mode;
    }

    private List<IncludedPath> getIndexingPolicyIncludePaths(Class<?> domainType) {
        final List<IncludedPath> pathArrayList = new ArrayList<>();
        final DocumentIndexingPolicy annotation = domainType.getAnnotation(DocumentIndexingPolicy.class);

        if (annotation == null || annotation.includePaths() == null || annotation.includePaths().length == 0) {
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
        final DocumentIndexingPolicy annotation = domainType.getAnnotation(DocumentIndexingPolicy.class);

        if (annotation == null || annotation.excludePaths().length == 0) {
            return null; // Align the default value of IndexingPolicy
        }

        final String[] rawPaths = annotation.excludePaths();
        for (final String path : rawPaths) {
            final JSONObject obj = new JSONObject(path);
            pathArrayList.add(new ExcludedPath().path(obj.get("path").toString()));
        }

        return pathArrayList;
    }

    private boolean getIsVersioned(Class<T> domainType) {
        final Field findField = ReflectionUtils.findField(domainType, ETAG);
        return findField != null
                && findField.getType() == String.class
                && findField.isAnnotationPresent(Version.class);
    }

    private boolean getIsAutoCreateContainer(Class<T> domainType) {
        final Document annotation = domainType.getAnnotation(Document.class);

        boolean autoCreateContainer = Constants.DEFAULT_AUTO_CREATE_CONTAINER;
        if (annotation != null) {
            autoCreateContainer = annotation.autoCreateCollection();
        }

        return autoCreateContainer;
    }

}

