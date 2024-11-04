// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.repository.support;

import com.azure.cosmos.models.CompositePath;
import com.azure.cosmos.models.ExcludedPath;
import com.azure.cosmos.models.IncludedPath;
import com.azure.cosmos.models.IndexingMode;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.UniqueKey;
import com.azure.cosmos.models.UniqueKeyPolicy;
import com.azure.spring.data.cosmos.Constants;
import com.azure.spring.data.cosmos.common.Memoizer;
import com.azure.spring.data.cosmos.core.mapping.CompositeIndex;
import com.azure.spring.data.cosmos.core.mapping.CompositeIndexPath;
import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.CosmosIndexingPolicy;
import com.azure.spring.data.cosmos.core.mapping.CosmosUniqueKey;
import com.azure.spring.data.cosmos.core.mapping.CosmosUniqueKeyPolicy;
import com.azure.spring.data.cosmos.core.mapping.GeneratedValue;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Persistable;
import org.springframework.data.repository.core.support.AbstractEntityInformation;
import org.springframework.lang.NonNull;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.spring.data.cosmos.common.ExpressionResolver.resolveExpression;

/**
 * Class to describe cosmosDb entity
 *
 * @param <T> domain type.
 * @param <ID> id type.
 */
public class CosmosEntityInformation<T, ID> extends AbstractEntityInformation<T, ID> {

    private static final Function<Class<?>, CosmosEntityInformation<?, ?>> ENTITY_INFORMATION_CREATOR =
        Memoizer.memoize(CosmosEntityInformation::getCosmosEntityInformation);

    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosEntityInformation.class);

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
    private final List<String> transientFields;
    private final String containerName;
    private final String partitionKeyPath;
    private final String[] hierarchicalPartitionKeyPaths;
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
        this.hierarchicalPartitionKeyPaths = CosmosEntityInformationHelper.getHierarchicalPartitionKeyPathsAnnotationValue(domainType);

        this.partitionKeyField = CosmosEntityInformationHelper.getPartitionKeyField(domainType);
        this.transientFields = CosmosEntityInformationHelper.getTransientFields(domainType);
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
     * Get transient field list
     * @return fields with @Transient annotation
     */
    public List<String> getTransientFields() {
        return transientFields;
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
        if (partitionKeyField != null) {
            final PartitionKey partitionKey = partitionKeyField.getAnnotation(PartitionKey.class);
            return partitionKey.value().equals("") ? "/" + partitionKeyField.getName() : "/" + partitionKey.value();
        } else if (partitionKeyPath != null) {
            return partitionKeyPath;
        } else if (hierarchicalPartitionKeyPaths != null && hierarchicalPartitionKeyPaths.length > 0) {
            String hierarchicalPartitionKeyPath = "";
            for (final String path : hierarchicalPartitionKeyPaths) {
                hierarchicalPartitionKeyPath = hierarchicalPartitionKeyPath == "" ? path
                    : hierarchicalPartitionKeyPath + ", " + path;
            }
            return hierarchicalPartitionKeyPath;
        } else {
            return "/null";
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
     * @throws RuntimeException thrown if field is not found
     */
    public Object getPartitionKeyFieldValue(T entity) {
        if (partitionKeyField != null) {
            return ReflectionUtils.getField(partitionKeyField, entity);
        } else if (partitionKeyPath != null) {
            List<String> parts = Arrays.stream(partitionKeyPath.split("/")).collect(Collectors.toList());
            final Object[] currentObject = {entity};
            parts.forEach(part -> {
                if (!part.isEmpty()) {
                    Field f = null;
                    try {
                        f = currentObject[0].getClass().getDeclaredField(part);
                    } catch (NoSuchFieldException e) {
                        throw new RuntimeException(e);
                    }
                    ReflectionUtils.makeAccessible(f);
                    currentObject[0] = ReflectionUtils.getField(f, currentObject[0]);
                }
            });
            return currentObject[0];
        } else if (hierarchicalPartitionKeyPaths != null && hierarchicalPartitionKeyPaths.length > 0) {
            ArrayList<Object> pkValues = new ArrayList<>();
            for (final String path : hierarchicalPartitionKeyPaths) {
                Field f = null;
                try {
                    f = entity.getClass().getDeclaredField(path.substring(1));
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
                ReflectionUtils.makeAccessible(f);
                pkValues.add(ReflectionUtils.getField(f, entity));
            }
            return pkValues;
        } else {
            return null;
        }
    }

    /**
     * Return the partition key field name.
     * @return the partition key field name
     */
    public String getPartitionKeyFieldName() {
        if (partitionKeyField != null) {
            return partitionKeyField.getName();
        } else if (partitionKeyPath != null) {
            return partitionKeyPath.substring(1).replace("/", ".");
        } else if (hierarchicalPartitionKeyPaths != null && hierarchicalPartitionKeyPaths.length > 0) {
            String hierarchicalPartitionKeyFiledName = "";
            for (final String path : hierarchicalPartitionKeyPaths) {
                hierarchicalPartitionKeyFiledName = hierarchicalPartitionKeyFiledName == "" ? path.substring(1)
                    : hierarchicalPartitionKeyFiledName + ", " + path.substring(1);
            }
            return hierarchicalPartitionKeyFiledName;
        } else {
            return null;
        }
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
     * Return whether indexing policy is specified.
     * @return whether indexing policy is specified
     */
    public boolean isIndexingPolicySpecified() {
        return this.isIndexingPolicySpecified;
    }

    /**
     * Class of helper functions for CosmosEntityInformation to call from the constructor
     */
    static class CosmosEntityInformationHelper {

        /**
         * Gets if the indexing policy is specified for the entity
         *
         * @param domainType the domain type
         * @return boolean
         */
        private static boolean isIndexingPolicySpecified(Class<?> domainType) {
            return domainType.getAnnotation(CosmosIndexingPolicy.class) != null;
        }

        /**
         * Gets the indexing policy of the entity
         *
         * @param domainType the domain type
         * @return IndexingPolicy
         */
        private static IndexingPolicy getIndexingPolicy(Class<?> domainType) {
            final IndexingPolicy policy = new IndexingPolicy();

            policy.setAutomatic(getIndexingPolicyAutomatic(domainType));
            policy.setIndexingMode(getIndexingPolicyMode(domainType));
            policy.setIncludedPaths(getIndexingPolicyIncludePaths(domainType));
            policy.setExcludedPaths(getIndexingPolicyExcludePaths(domainType));
            policy.setCompositeIndexes(getIndexingPolicyCompositeIndexes(domainType));

            return policy;
        }

        /**
         * Gets the unique key policy of the entity
         *
         * @param domainType the domain type
         * @return UniqueKeyPolicy
         */
        private static UniqueKeyPolicy getUniqueKeyPolicy(Class<?> domainType) {
            CosmosUniqueKeyPolicy annotation = domainType.getAnnotation(CosmosUniqueKeyPolicy.class);
            if (annotation == null) {
                return null;
            }

            List<UniqueKey> uniqueKeys = getUniqueKeys(domainType);
            if (uniqueKeys.isEmpty()) {
                return null;
            }
            return new UniqueKeyPolicy().setUniqueKeys(uniqueKeys);
        }

        /**
         * Gets the id field of the entity
         *
         * @param domainType the domain type
         * @param javaType the java type of the domain class
         * @return Field id
         * @throws IllegalArgumentException if the id field fails validation
         */
        private static Field getIdField(Class<?> domainType, Class<?> javaType) {
            final Field idField;
            final List<Field> fields = FieldUtils.getFieldsListWithAnnotation(domainType, Id.class);

            if (fields.isEmpty()) {
                idField = ReflectionUtils.findField(javaType, Constants.ID_PROPERTY_NAME);
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
                && idField.getType() != long.class
                && idField.getType() != UUID.class) {
                throw new IllegalArgumentException("type of id field must be String, Integer, Long or UUID");
            }

            return idField;
        }

        /**
         * Gets if the id field is annotated with generated value for the entity
         *
         * @param idField the id of the entity
         * @return boolean
         * @throws IllegalArgumentException if the id field fails validation
         */
        private static boolean isIdFieldAnnotatedWithGeneratedValue(Field idField) {
            if (idField.getAnnotation(GeneratedValue.class) != null) {
                if (idField.getType() == String.class) {
                    return true;
                } else {
                    throw new IllegalArgumentException("id field must be of type String if "
                        + "GeneratedValue annotation is present");
                }
            }
            return false;
        }

        /**
         * Gets the container name of the entity
         *
         * @param domainType the domain type
         * @return String container name
         */
        private static String getContainerName(Class<?> domainType) {
            String customContainerName = domainType.getSimpleName();

            final Container annotation = domainType.getAnnotation(Container.class);

            if (annotation != null && !annotation.containerName().isEmpty()) {
                customContainerName = resolveExpression(annotation.containerName());
            }

            return customContainerName;
        }

        /**
         * Gets the partition key path of the entity
         *
         * @param domainType the domain type
         * @return String partition key path
         */
        private static String getPartitionKeyPathAnnotationValue(Class<?> domainType) {
            final Container annotation = domainType.getAnnotation(Container.class);

            if (annotation != null && !annotation.partitionKeyPath().isEmpty()) {
                return annotation.partitionKeyPath();
            }
            return null;
        }

        /**
         * Gets the hierarchical partition key paths of the entity
         *
         * @param domainType the domain type
         * @return String[] of hierarchical partition key paths
         */
        private static String[] getHierarchicalPartitionKeyPathsAnnotationValue(Class<?> domainType) {
            final Container annotation = domainType.getAnnotation(Container.class);

            if (annotation != null && annotation.hierarchicalPartitionKeyPaths().length > 0) {
                return annotation.hierarchicalPartitionKeyPaths();
            }
            return null;
        }

        /**
         * Gets the partition key of the entity
         *
         * @param domainType the domain type
         * @return Field partition key
         * @throws IllegalArgumentException if the partition key field fails validation
         */
        private static Field getPartitionKeyField(Class<?> domainType) {
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

        private static List<String> getTransientFields(Class<?> domainType) {
            final Field partitionKeyField = getPartitionKeyField(domainType);
            final List<Field> fields = FieldUtils.getFieldsListWithAnnotation(domainType, Transient.class);
            List<String> transientFieldNames = new ArrayList<>();
            Iterator<Field> iterator = fields.iterator();
            while (iterator.hasNext()) {
                Field field = iterator.next();
                if (field.equals(partitionKeyField) || field.getName().equalsIgnoreCase("id") || field.getName().equalsIgnoreCase("_etag")) {
                    //throw exception if partition key or id field is declared transient
                    throw new IllegalArgumentException("Field cannot be declared transient: " + field.getName());
                }
                LOGGER.warn("Transient field will not be persisted: {}", field);
                transientFieldNames.add(field.getName());
            }
            return transientFieldNames;
        }

        /**
         * Gets the request units for the entity
         *
         * @param domainType the domain type
         * @return Integer ru's
         */
        private static Integer getRequestUnit(Class<?> domainType) {
            Integer ru = null;
            final Container annotation = domainType.getAnnotation(Container.class);

            if (annotation != null
                && annotation.ru() != null
                && !annotation.ru().isEmpty()) {
                ru = Integer.parseInt(annotation.ru());
            }
            return ru;
        }

        /**
         * Gets the time to live for the entity
         *
         * @param domainType the domain type
         * @return Integer ttl
         */
        private static Integer getTimeToLive(Class<?> domainType) {
            Integer ttl = Constants.DEFAULT_TIME_TO_LIVE;
            final Container annotation = domainType.getAnnotation(Container.class);

            if (annotation != null) {
                ttl = annotation.timeToLive();
            }

            return ttl;
        }

        /**
         * Gets if we overwrite the indexing policy in the portal for the entity
         *
         * @param domainType the domain type
         * @return boolean
         */
        private static boolean getIndexingPolicyOverwritePolicy(Class<?> domainType) {
            boolean isOverwritePolicy = Constants.DEFAULT_INDEXING_POLICY_OVERWRITE_POLICY;
            final CosmosIndexingPolicy annotation = domainType.getAnnotation(CosmosIndexingPolicy.class);

            if (annotation != null) {
                isOverwritePolicy = annotation.overwritePolicy();
            }

            return isOverwritePolicy;
        }

        /**
         * Gets if automatic is defined on the indexing policy for the entity
         *
         * @param domainType the domain type
         * @return boolean
         */
        private static boolean getIndexingPolicyAutomatic(Class<?> domainType) {
            boolean isAutomatic = Constants.DEFAULT_INDEXING_POLICY_AUTOMATIC;
            final CosmosIndexingPolicy annotation = domainType.getAnnotation(CosmosIndexingPolicy.class);

            if (annotation != null) {
                isAutomatic = annotation.automatic();
            }

            return isAutomatic;
        }

        /**
         * Gets the indexing policy mode for the entity
         *
         * @param domainType the domain type
         * @return IndexingMode
         */
        private static IndexingMode getIndexingPolicyMode(Class<?> domainType) {
            IndexingMode mode = Constants.DEFAULT_INDEXING_POLICY_MODE;
            final CosmosIndexingPolicy annotation = domainType.getAnnotation(CosmosIndexingPolicy.class);

            if (annotation != null) {
                mode = annotation.mode();
            }

            return mode;
        }

        /**
         * Gets the include paths from the indexing policy for the entity
         *
         * @param domainType the domain type
         * @return List list of IncludePath's
         */
        private static List<IncludedPath> getIndexingPolicyIncludePaths(Class<?> domainType) {
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

        /**
         * Gets the exclude paths from the indexing policy for the entity
         *
         * @param domainType the domain type
         * @return List list of ExcludePath's
         */
        private static List<ExcludedPath> getIndexingPolicyExcludePaths(Class<?> domainType) {
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

        /**
         * Gets the composite indexes from the indexing policy for the entity
         *
         * @param domainType the domain type
         * @return List List of compositepath's
         */
        private static List<List<CompositePath>> getIndexingPolicyCompositeIndexes(Class<?> domainType) {
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

        /**
         * Gets the unique keys for the entity
         *
         * @param domainType the domain type
         * @return List list of UniqueKey's
         */
        private static List<UniqueKey> getUniqueKeys(Class<?> domainType) {
            CosmosUniqueKeyPolicy annotation = domainType.getAnnotation(CosmosUniqueKeyPolicy.class);
            assert annotation != null;
            if (annotation.uniqueKeys().length == 0) {
                return Collections.emptyList();
            }
            final CosmosUniqueKey[] uniqueKeysPath = annotation.uniqueKeys();
            final List<UniqueKey> uniqueKeys = new ArrayList<>();
            for (final CosmosUniqueKey uniqueKey : uniqueKeysPath) {
                UniqueKey key = new UniqueKey(Arrays.asList(uniqueKey.paths()));
                uniqueKeys.add(key);
            }
            return uniqueKeys;
        }

        /**
         * Gets the versioned field from the entity
         *
         * @param domainClass the domain class
         * @return Field version
         * @throws IllegalArgumentException if the version field fails validation
         */
        private static Field getVersionedField(Class<?> domainClass) {
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

        /**
         * Gets if the entity is set to auto create
         *
         * @param domainType the domain type
         * @return boolean
         */
        private static boolean getIsAutoCreateContainer(Class<?> domainType) {
            final Container annotation = domainType.getAnnotation(Container.class);

            boolean autoCreateContainer = Constants.DEFAULT_AUTO_CREATE_CONTAINER;
            if (annotation != null) {
                autoCreateContainer = annotation.autoCreateContainer();
            }

            return autoCreateContainer;
        }

        /**
         * Gets if the entity is set to auto scale
         *
         * @param domainType the domain type
         * @return boolean
         */
        private static boolean getIsAutoScale(Class<?> domainType) {
            final Container annotation = domainType.getAnnotation(Container.class);

            boolean autoScale = Constants.DEFAULT_AUTO_SCALE;
            if (annotation != null) {
                autoScale = annotation.autoScale();
            }

            return autoScale;
        }
    }
}

