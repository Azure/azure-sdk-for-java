package com.azure.spring.data.cosmos.repository.support;

import com.azure.cosmos.models.CompositePath;
import com.azure.cosmos.models.ExcludedPath;
import com.azure.cosmos.models.IncludedPath;
import com.azure.cosmos.models.IndexingMode;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.UniqueKey;
import com.azure.cosmos.models.UniqueKeyPolicy;
import com.azure.spring.data.cosmos.Constants;
import com.azure.spring.data.cosmos.core.mapping.CompositeIndex;
import com.azure.spring.data.cosmos.core.mapping.CompositeIndexPath;
import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.CosmosIndexingPolicy;
import com.azure.spring.data.cosmos.core.mapping.CosmosUniqueKey;
import com.azure.spring.data.cosmos.core.mapping.CosmosUniqueKeyPolicy;
import com.azure.spring.data.cosmos.core.mapping.GeneratedValue;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.azure.spring.data.cosmos.common.ExpressionResolver.resolveExpression;

public class CosmosEntityInformationHelper {

    protected static boolean isIndexingPolicySpecified(Class<?> domainType) {
        return domainType.getAnnotation(CosmosIndexingPolicy.class) != null;
    }

    protected static IndexingPolicy getIndexingPolicy(Class<?> domainType) {
        final IndexingPolicy policy = new IndexingPolicy();

        policy.setAutomatic(getIndexingPolicyAutomatic(domainType));
        policy.setIndexingMode(getIndexingPolicyMode(domainType));
        policy.setIncludedPaths(getIndexingPolicyIncludePaths(domainType));
        policy.setExcludedPaths(getIndexingPolicyExcludePaths(domainType));
        policy.setCompositeIndexes(getIndexingPolicyCompositeIndexes(domainType));

        return policy;
    }

    protected static UniqueKeyPolicy getUniqueKeyPolicy(Class<?> domainType) {
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

    protected static Field getIdField(Class<?> domainType, Class<?> javaType) {
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

    protected static boolean isIdFieldAnnotatedWithGeneratedValue(Field idField) {
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

    protected static String getContainerName(Class<?> domainType) {
        String customContainerName = domainType.getSimpleName();

        final Container annotation = domainType.getAnnotation(Container.class);

        if (annotation != null && !annotation.containerName().isEmpty()) {
            customContainerName = resolveExpression(annotation.containerName());
        }

        return customContainerName;
    }

    protected static String getPartitionKeyPathAnnotationValue(Class<?> domainType) {
        final Container annotation = domainType.getAnnotation(Container.class);

        if (annotation != null && !annotation.partitionKeyPath().isEmpty()) {
            return annotation.partitionKeyPath();
        }
        return null;
    }

    protected static Field getPartitionKeyField(Class<?> domainType) {
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

    protected static Integer getRequestUnit(Class<?> domainType) {
        Integer ru = null;
        final Container annotation = domainType.getAnnotation(Container.class);

        if (annotation != null
            && annotation.ru() != null
            && !annotation.ru().isEmpty()) {
            ru = Integer.parseInt(annotation.ru());
        }
        return ru;
    }

    protected static Integer getTimeToLive(Class<?> domainType) {
        Integer ttl = Constants.DEFAULT_TIME_TO_LIVE;
        final Container annotation = domainType.getAnnotation(Container.class);

        if (annotation != null) {
            ttl = annotation.timeToLive();
        }

        return ttl;
    }

    protected static boolean getIndexingPolicyOverwritePolicy(Class<?> domainType) {
        boolean isOverwritePolicy = Constants.DEFAULT_INDEXING_POLICY_OVERWRITE_POLICY;
        final CosmosIndexingPolicy annotation = domainType.getAnnotation(CosmosIndexingPolicy.class);

        if (annotation != null) {
            isOverwritePolicy = annotation.overwritePolicy();
        }

        return isOverwritePolicy;
    }

    protected static boolean getIndexingPolicyAutomatic(Class<?> domainType) {
        boolean isAutomatic = Constants.DEFAULT_INDEXING_POLICY_AUTOMATIC;
        final CosmosIndexingPolicy annotation = domainType.getAnnotation(CosmosIndexingPolicy.class);

        if (annotation != null) {
            isAutomatic = annotation.automatic();
        }

        return isAutomatic;
    }

    protected static IndexingMode getIndexingPolicyMode(Class<?> domainType) {
        IndexingMode mode = Constants.DEFAULT_INDEXING_POLICY_MODE;
        final CosmosIndexingPolicy annotation = domainType.getAnnotation(CosmosIndexingPolicy.class);

        if (annotation != null) {
            mode = annotation.mode();
        }

        return mode;
    }

    protected static List<IncludedPath> getIndexingPolicyIncludePaths(Class<?> domainType) {
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

    protected static List<ExcludedPath> getIndexingPolicyExcludePaths(Class<?> domainType) {
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

    protected static List<List<CompositePath>> getIndexingPolicyCompositeIndexes(Class<?> domainType) {
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

    protected static List<UniqueKey> getUniqueKeys(Class<?> domainType) {
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

    protected static Field getVersionedField(Class<?> domainClass) {
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

    protected static boolean getIsAutoCreateContainer(Class<?> domainType) {
        final Container annotation = domainType.getAnnotation(Container.class);

        boolean autoCreateContainer = Constants.DEFAULT_AUTO_CREATE_CONTAINER;
        if (annotation != null) {
            autoCreateContainer = annotation.autoCreateContainer();
        }

        return autoCreateContainer;
    }

    protected static boolean getIsAutoScale(Class<?> domainType) {
        final Container annotation = domainType.getAnnotation(Container.class);

        boolean autoScale = Constants.DEFAULT_AUTO_SCALE;
        if (annotation != null) {
            autoScale = annotation.autoScale();
        }

        return autoScale;
    }
}
