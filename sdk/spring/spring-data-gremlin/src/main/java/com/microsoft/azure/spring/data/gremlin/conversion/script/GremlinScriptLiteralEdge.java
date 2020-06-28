// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.data.gremlin.conversion.script;

import com.microsoft.azure.spring.data.gremlin.common.Constants;
import com.microsoft.azure.spring.data.gremlin.common.GremlinEntityType;
import com.microsoft.azure.spring.data.gremlin.conversion.source.GremlinSource;
import com.microsoft.azure.spring.data.gremlin.conversion.source.GremlinSourceEdge;
import com.microsoft.azure.spring.data.gremlin.exception.GremlinUnexpectedSourceTypeException;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GremlinScriptLiteralEdge implements GremlinScriptLiteral {

    private static final String FROM_ALIAS = "from";
    private static final String TO_ALIAS = "to";

    private String generateEdgeDirection(@NonNull String from, @NonNull String to) {
        Assert.notNull(from, "from should not be null");
        Assert.notNull(to, "to should not be null");
        return String.format("from('%s').to('%s')", from, to);
    }

    @Override
    public <T> List<String> generateInsertScript(@NonNull GremlinSource<T> source) {
        if (!(source instanceof GremlinSourceEdge)) {
            throw new GremlinUnexpectedSourceTypeException("should be the instance of GremlinSourceEdge");
        }

        final GremlinSourceEdge<T> sourceEdge = (GremlinSourceEdge<T>) source;
        final List<String> scriptList = new ArrayList<>();

        scriptList.add(Constants.GREMLIN_PRIMITIVE_GRAPH);                                            // g
        scriptList.add(GremlinScriptLiteralHelper.generateEntityWithRequiredId(sourceEdge.getVertexIdFrom(), GremlinEntityType.VERTEX)); // V(id)
        scriptList.add(GremlinScriptLiteralHelper.generateAsWithAlias(FROM_ALIAS));                                    // from('from')
        scriptList.add(GremlinScriptLiteralHelper.generateEntityWithRequiredId(sourceEdge.getVertexIdTo(), GremlinEntityType.VERTEX));   // V(id)
        scriptList.add(GremlinScriptLiteralHelper.generateAsWithAlias(TO_ALIAS));                                      // to('to')
        scriptList.add(GremlinScriptLiteralHelper.generateAddEntityWithLabel(sourceEdge.getLabel(), GremlinEntityType.EDGE));            // addE(label)
        scriptList.add(generateEdgeDirection(FROM_ALIAS, TO_ALIAS));                        // from('from').to('to')

        source.getId().ifPresent(id -> scriptList.add(GremlinScriptLiteralHelper.generatePropertyWithRequiredId(id))); // property(id, xxx)

        scriptList.addAll(GremlinScriptLiteralHelper.generateProperties(source.getProperties()));

        return GremlinScriptLiteralHelper.completeScript(scriptList);
    }

    @Override
    public List<String> generateDeleteAllScript() {
        return Collections.singletonList(Constants.GREMLIN_SCRIPT_EDGE_DROP_ALL);
    }

    @Override
    public <T> List<String> generateDeleteAllByClassScript(@NonNull GremlinSource<T> source) {
        if (!(source instanceof GremlinSourceEdge)) {
            throw new GremlinUnexpectedSourceTypeException("should be the instance of GremlinSourceEdge");
        }

        final List<String> scriptList = Arrays.asList(
                Constants.GREMLIN_PRIMITIVE_GRAPH,             // g
                Constants.GREMLIN_PRIMITIVE_EDGE_ALL,          // E()
                GremlinScriptLiteralHelper.generateHasLabel(source.getLabel()), // has(label, 'label')
                Constants.GREMLIN_PRIMITIVE_DROP               // drop()
        );

        return GremlinScriptLiteralHelper.completeScript(scriptList);
    }

    @Override
    public <T> List<String> generateFindByIdScript(@NonNull GremlinSource<T> source) {
        if (!(source instanceof GremlinSourceEdge)) {
            throw new GremlinUnexpectedSourceTypeException("should be the instance of GremlinSourceEdge");
        }

        Assert.isTrue(source.getId().isPresent(), "GremlinSource should contain id.");

        final List<String> scriptList = Arrays.asList(
                Constants.GREMLIN_PRIMITIVE_GRAPH,                                 // g
                Constants.GREMLIN_PRIMITIVE_EDGE_ALL,                              // E()
                GremlinScriptLiteralHelper.generateHasId(source.getId().get(), source.getIdField()) // hasId(xxx)
        );

        return GremlinScriptLiteralHelper.completeScript(scriptList);
    }

    @Override
    public <T> List<String> generateUpdateScript(@NonNull GremlinSource<T> source) {
        if (!(source instanceof GremlinSourceEdge)) {
            throw new GremlinUnexpectedSourceTypeException("should be the instance of GremlinSourceEdge");
        }

        final List<String> scriptList = new ArrayList<>();

        Assert.isTrue(source.getId().isPresent(), "GremlinSource should contain id.");

        scriptList.add(Constants.GREMLIN_PRIMITIVE_GRAPH);                                  // g
        scriptList.add(GremlinScriptLiteralHelper.generateEntityWithRequiredId(source.getId().get(), GremlinEntityType.EDGE)); // E(id)

        scriptList.addAll(GremlinScriptLiteralHelper.generateProperties(source.getProperties()));

        return GremlinScriptLiteralHelper.completeScript(scriptList);
    }

    @Override
    public <T> List<String> generateFindAllScript(@NonNull GremlinSource<T> source) {
        if (!(source instanceof GremlinSourceEdge)) {
            throw new GremlinUnexpectedSourceTypeException("should be the instance of GremlinSourceEdge");
        }

        final String className = source.getProperties().get(Constants.GREMLIN_PROPERTY_CLASSNAME).toString();
        Assert.notNull(className, "GremlinSource should contain predefined className");

        final List<String> scriptList = Arrays.asList(
                Constants.GREMLIN_PRIMITIVE_GRAPH,                           // g
                Constants.GREMLIN_PRIMITIVE_EDGE_ALL,                        // E()
                GremlinScriptLiteralHelper.generateHasLabel(source.getLabel()),               // has(label, 'label')
                GremlinScriptLiteralHelper.generateHas(Constants.GREMLIN_PROPERTY_CLASSNAME, className) // has(_classname, 'xxxxxx')
        );

        return GremlinScriptLiteralHelper.completeScript(scriptList);
    }

    @Override
    public <T> List<String> generateDeleteByIdScript(@NonNull GremlinSource<T> source) {
        if (!(source instanceof GremlinSourceEdge)) {
            throw new GremlinUnexpectedSourceTypeException("should be the instance of GremlinSourceEdge");
        }

        Assert.isTrue(source.getId().isPresent(), "GremlinSource should contain id.");

        final List<String> scriptList = Arrays.asList(
                Constants.GREMLIN_PRIMITIVE_GRAPH,                                  // g
                Constants.GREMLIN_PRIMITIVE_EDGE_ALL,                               // E()
                GremlinScriptLiteralHelper.generateHasId(source.getId().get(), source.getIdField()), // hasId(xxx)
                Constants.GREMLIN_PRIMITIVE_DROP                                    // drop()
        );

        return GremlinScriptLiteralHelper.completeScript(scriptList);
    }

    @Override
    public <T> List<String> generateCountScript(@NonNull GremlinSource<T> source) {
        if (!(source instanceof GremlinSourceEdge)) {
            throw new GremlinUnexpectedSourceTypeException("should be the instance of GremlinSourceEdge");
        }

        return Collections.singletonList(Constants.GREMLIN_SCRIPT_EDGE_ALL);
    }
}

