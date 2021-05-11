// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.query;

import com.azure.spring.data.gremlin.annotation.EdgeFrom;
import com.azure.spring.data.gremlin.annotation.EdgeTo;
import com.azure.spring.data.gremlin.annotation.GeneratedValue;
import com.azure.spring.data.gremlin.common.GremlinEntityType;
import com.azure.spring.data.gremlin.common.GremlinFactory;
import com.azure.spring.data.gremlin.common.GremlinUtils;
import com.azure.spring.data.gremlin.conversion.MappingGremlinConverter;
import com.azure.spring.data.gremlin.conversion.script.GremlinScriptLiteral;
import com.azure.spring.data.gremlin.conversion.script.GremlinScriptLiteralEdge;
import com.azure.spring.data.gremlin.conversion.script.GremlinScriptLiteralGraph;
import com.azure.spring.data.gremlin.conversion.script.GremlinScriptLiteralVertex;
import com.azure.spring.data.gremlin.conversion.source.GremlinSource;
import com.azure.spring.data.gremlin.conversion.source.GremlinSourceEdge;
import com.azure.spring.data.gremlin.conversion.source.GremlinSourceGraph;
import com.azure.spring.data.gremlin.conversion.source.GremlinSourceVertex;
import com.azure.spring.data.gremlin.exception.GremlinEntityInformationException;
import com.azure.spring.data.gremlin.exception.GremlinInvalidEntityIdFieldException;
import com.azure.spring.data.gremlin.exception.GremlinQueryException;
import com.azure.spring.data.gremlin.exception.GremlinUnexpectedEntityTypeException;
import com.azure.spring.data.gremlin.mapping.GremlinPersistentEntity;
import com.azure.spring.data.gremlin.mapping.GremlinPersistentProperty;
import com.azure.spring.data.gremlin.query.query.GremlinQuery;
import com.azure.spring.data.gremlin.query.query.QueryFindScriptGenerator;
import com.azure.spring.data.gremlin.query.query.QueryScriptGenerator;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.model.ConvertingPropertyAccessor;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static java.util.stream.Collectors.toList;

public class GremlinTemplate implements GremlinOperations, ApplicationContextAware {

    private final GremlinFactory factory;
    private final MappingGremlinConverter mappingConverter;

    private Client gremlinClient;
    private ApplicationContext context;

    public GremlinTemplate(@NonNull GremlinFactory factory, @NonNull MappingGremlinConverter converter) {
        this.factory = factory;
        this.mappingConverter = converter;
    }

    @Override
    public MappingGremlinConverter getMappingConverter() {
        return this.mappingConverter;
    }

    public ApplicationContext getApplicationContext() {
        return this.context;
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext context) throws BeansException {
        this.context = context;
    }

    public Client getGremlinClient() {
        if (this.gremlinClient == null) {
            this.gremlinClient = this.factory.getGremlinClient();
        }

        return this.gremlinClient;
    }

    @NonNull
    private List<Result> executeQuery(@NonNull List<String> queries) {
        final List<List<String>> parallelQueries = GremlinUtils.toParallelQueryList(queries);

        return parallelQueries.stream().flatMap(q -> executeQueryParallel(q).stream()).collect(toList());
    }

    @NonNull
    private List<Result> executeQueryParallel(@NonNull List<String> queries) {
        return queries.parallelStream()
                .map(q -> getGremlinClient().submit(q).all())
                .collect(toList()).parallelStream().flatMap(f -> {
                    try {
                        return f.get().stream();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new GremlinQueryException("unable to complete query from gremlin", e);
                    }
                })
                .collect(toList());
    }

    @Override
    public void deleteAll() {
        final GremlinScriptLiteral script = new GremlinScriptLiteralGraph();
        final List<String> queryList = script.generateDeleteAllScript();

        executeQuery(queryList);
    }

    @Override
    public void deleteAll(GremlinEntityType type) {
        final GremlinSource<?> source = type.createGremlinSource();

        executeQuery(source.getGremlinScriptLiteral().generateDeleteAllScript());
    }

    @Override
    public <T> void deleteAll(GremlinSource<T> source) {
        executeQuery(source.getGremlinScriptLiteral().generateDeleteAllByClassScript(source));
    }

    private <T> List<Result> insertInternal(@NonNull T object, @NonNull GremlinSource<T> source) {
        this.mappingConverter.write(object, source);

        return executeQuery(source.getGremlinScriptLiteral().generateInsertScript(source));
    }

    @Override
    public <T> T insert(@NonNull T object, GremlinSource<T> source) {
        final boolean entityGraph = source instanceof GremlinSourceGraph;

        if (!entityGraph && source.getIdField().isAnnotationPresent(GeneratedValue.class)
                && source.getId().isPresent()) {
            throw new GremlinInvalidEntityIdFieldException("The entity meant to be created has a non-null id "
                    + "that is marked as @GeneratedValue");
        }

        // The current implementation doesn't support creating graphs that contain both edges
        // and vertices that have null (generated) ids. In this case, vertex and edge creation
        // need to be performed in two consecutive steps.
        // TODO(SOON) Add this verification in the GremlinSourceGraphWriter

        final List<Result> results = insertInternal(object, source);

        if (!results.isEmpty()) {
            if (entityGraph) {
                return recoverGraphDomain((GremlinSourceGraph<T>) source, results);
            } else {
                return recoverDomain(source, results);
            }
        }

        return null;
    }

    @Override
    public <T> T findVertexById(@NonNull Object id, GremlinSource<T> source) {
        if (source instanceof GremlinSourceVertex) {
            source.setId(id);
            return this.findByIdInternal(source);
        }

        throw new GremlinUnexpectedEntityTypeException("should be vertex domain for findVertexById");
    }

    private Object getEdgeAnnotatedFieldValue(@NonNull Field field, @NonNull Object vertexId) {
        if (field.getType() == String.class || field.getType() == Long.class || field.getType() == Integer.class) {
            return vertexId;
        } else if (field.getType().isPrimitive()) {
            throw new GremlinUnexpectedEntityTypeException("only String/Long/Integer type of Id Field is allowed");
        } else {
            return this.findVertexById(vertexId, GremlinUtils.toGremlinSource(field.getType()));
        }
    }

    @NonNull
    private Field getEdgeAnnotatedField(@NonNull Class<?> domainClass,
                                        @NonNull Class<? extends Annotation> annotationClass) {
        final List<Field> fields = FieldUtils.getFieldsListWithAnnotation(domainClass, annotationClass);

        if (fields.size() != 1) {
            throw new GremlinEntityInformationException("should be only one Annotation");
        }

        return fields.get(0);
    }

    /**
     * Find Edge need another two query to obtain edgeFrom and edgeTo.
     * This function will do that and make edge domain completion.
     */
    @SuppressWarnings("unchecked")
    private <T> void completeEdge(@NonNull T domain, @NonNull GremlinSourceEdge<T> source) {
        final ConvertingPropertyAccessor<T> accessor = this.mappingConverter.getPropertyAccessor(domain);
        final GremlinPersistentEntity<T> persistentEntity = (GremlinPersistentEntity<T>) this.mappingConverter
            .getPersistentEntity(domain.getClass());

        final Field fromField = this.getEdgeAnnotatedField(domain.getClass(), EdgeFrom.class);
        final Field toField = this.getEdgeAnnotatedField(domain.getClass(), EdgeTo.class);

        final PersistentProperty<GremlinPersistentProperty> propertyFrom = persistentEntity
            .getPersistentProperty(fromField.getName());
        final PersistentProperty<GremlinPersistentProperty> propertyTo = persistentEntity
            .getPersistentProperty(toField.getName());

        Assert.notNull(propertyFrom, "persistence property should not be null");
        Assert.notNull(propertyTo, "persistence property should not be null");

        accessor.setProperty(propertyFrom, this.getEdgeAnnotatedFieldValue(fromField, source.getVertexIdFrom()));
        accessor.setProperty(propertyTo, this.getEdgeAnnotatedFieldValue(toField, source.getVertexIdTo()));
    }

    @Override
    public <T> T findEdgeById(@NonNull Object id, @NonNull GremlinSource<T> source) {
        if (source instanceof GremlinSourceEdge) {
            return this.findById(id, source);
        }

        throw new GremlinUnexpectedEntityTypeException("should be edge domain for findEdge");
    }

    private <T> T findByIdInternal(@NonNull GremlinSource<T> source) {
        final List<String> queryList = source.getGremlinScriptLiteral().generateFindByIdScript(source);
        final List<Result> results = this.executeQuery(queryList);

        if (results.isEmpty()) {
            return null;
        }

        return recoverDomain(source, results);
    }

    @Override
    public <T> T findById(@NonNull Object id, @NonNull GremlinSource<T> source) {
        if (source instanceof GremlinSourceGraph) {
            throw new UnsupportedOperationException("Gremlin graph cannot be findById.");
        }

        source.setId(id);

        return findByIdInternal(source);
    }

    private <T> T updateInternal(@NonNull T object, @NonNull GremlinSource<T> source) {
        this.mappingConverter.write(object, source);

        final List<String> queryList = source.getGremlinScriptLiteral().generateUpdateScript(source);

        executeQuery(queryList);

        return object;
    }

    @Override
    public <T> T update(@NonNull T object, @NonNull GremlinSource<T> source) {
        final Optional<Object> optional = source.getId();

        if (!(source instanceof GremlinSourceGraph)
                && (!optional.isPresent() || notExistsById(optional.get(), source))) {
            throw new GremlinQueryException("cannot update the object doesn't exist");
        }

        return this.updateInternal(object, source);
    }

    @Override
    public <T> T save(@NonNull T object, @NonNull GremlinSource<T> source) {
        final Optional<Object> optional = source.getId();
        final boolean entityGraph = source instanceof GremlinSourceGraph;

        if (entityGraph && this.isEmptyGraph(source)) {
            return insert(object, source);
        } else if (!entityGraph && (!optional.isPresent() || notExistsById(optional.get(), source))) {
            return insert(object, source);
        } else {
            return updateInternal(object, source);
        }
    }

    @Override
    public <T> List<T> findAll(@NonNull GremlinSource<T> source) {
        if (source instanceof GremlinSourceGraph) {
            throw new UnsupportedOperationException("Gremlin graph cannot be findAll.");
        }

        final List<String> queryList = source.getGremlinScriptLiteral().generateFindAllScript(source);
        final List<Result> results = executeQuery(queryList);

        if (results.isEmpty()) {
            return Collections.emptyList();
        }

        return recoverDomainList(source, results);
    }

    @Override
    public <T> void deleteById(@NonNull Object id, @NonNull GremlinSource<T> source) {
        source.setId(id);

        final List<String> queryList = source.getGremlinScriptLiteral().generateDeleteByIdScript(source);

        executeQuery(queryList);
    }

    @Override
    public <T> boolean isEmptyGraph(@NonNull GremlinSource<T> source) {
        if (source instanceof GremlinSourceGraph) {
            final GremlinScriptLiteralGraph literalGraph = (GremlinScriptLiteralGraph) source.getGremlinScriptLiteral();
            final List<String> queryList = literalGraph.generateIsEmptyScript();
            final List<Result> results = this.executeQuery(queryList);

            return results.isEmpty();
        }

        throw new GremlinQueryException("only graph domain is allowed.");
    }

    @Override
    public long vertexCount() {
        final GremlinScriptLiteral script = new GremlinScriptLiteralVertex();
        final List<String> queryList = script.generateCountScript(new GremlinSourceVertex<>());
        final List<Result> results = this.executeQuery(queryList);

        return results.size();
    }

    @Override
    public long edgeCount() {
        final GremlinScriptLiteral script = new GremlinScriptLiteralEdge();
        final List<String> queryList = script.generateCountScript(new GremlinSourceEdge<>());
        final List<Result> results = this.executeQuery(queryList);

        return results.size();
    }

    private <T> T recoverDomain(@NonNull GremlinSource<T> source, @NonNull List<Result> results) {
        final T domain;
        final Class<T> domainClass = source.getDomainClass();

        source.doGremlinResultRead(results);
        domain = this.mappingConverter.read(domainClass, source);

        if (source instanceof GremlinSourceEdge) {
            this.completeEdge(domain, (GremlinSourceEdge<T>) source);
        }

        return domain;
    }

    private <T> List<T> recoverDomainList(@NonNull GremlinSource<T> source, @NonNull List<Result> results) {
        return results.stream().map(r -> recoverDomain(source, Collections.singletonList(r))).collect(toList());
    }

    private <T> T recoverGraphDomain(@NonNull GremlinSourceGraph<T> source, @NonNull List<Result> results) {
        final T domain;
        final Class<T> domainClass = source.getDomainClass();

        source.getResultsReader().read(results, source);
        domain = source.doGremlinSourceRead(domainClass, mappingConverter);
        return domain;
    }

    private <T> boolean notExistsById(@NonNull Object id, @NonNull GremlinSource<T> source) {
        return !existsById(id, source);
    }

    @Override
    public <T> boolean existsById(@NonNull Object id, @NonNull GremlinSource<T> source) {
        return findById(id, source) != null;
    }

    @Override
    public <T> List<T> find(@NonNull GremlinQuery query, @NonNull GremlinSource<T> source) {
        final QueryScriptGenerator generator = new QueryFindScriptGenerator<>(source);
        final List<String> queryList = generator.generate(query);
        final List<Result> results = this.executeQuery(queryList);

        if (results.isEmpty()) {
            return Collections.emptyList();
        }

        return this.recoverDomainList(source, results);
    }
}

