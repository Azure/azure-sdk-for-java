// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.conversion.script;

import static com.azure.spring.data.gremlin.common.Constants.GREMLIN_PRIMITIVE_DROP;
import static com.azure.spring.data.gremlin.common.Constants.GREMLIN_PRIMITIVE_EDGE_ALL;
import static com.azure.spring.data.gremlin.common.Constants.GREMLIN_PRIMITIVE_GRAPH;
import static com.azure.spring.data.gremlin.common.Constants.GREMLIN_PROPERTY_CLASSNAME;
import static com.azure.spring.data.gremlin.common.Constants.GREMLIN_SCRIPT_EDGE_ALL;
import static com.azure.spring.data.gremlin.common.GremlinEntityType.EDGE;
import static com.azure.spring.data.gremlin.common.GremlinEntityType.VERTEX;
import static com.azure.spring.data.gremlin.conversion.script.GremlinScriptLiteralHelper.completeScript;
import static com.azure.spring.data.gremlin.conversion.script.GremlinScriptLiteralHelper.generateAddEntityWithLabel;
import static com.azure.spring.data.gremlin.conversion.script.GremlinScriptLiteralHelper.generateAsWithAlias;
import static com.azure.spring.data.gremlin.conversion.script.GremlinScriptLiteralHelper.generateEntityWithRequiredId;
import static com.azure.spring.data.gremlin.conversion.script.GremlinScriptLiteralHelper.generateHas;
import static com.azure.spring.data.gremlin.conversion.script.GremlinScriptLiteralHelper.generateHasId;
import static com.azure.spring.data.gremlin.conversion.script.GremlinScriptLiteralHelper.generateHasLabel;
import static com.azure.spring.data.gremlin.conversion.script.GremlinScriptLiteralHelper.generateProperties;
import static com.azure.spring.data.gremlin.conversion.script.GremlinScriptLiteralHelper.generatePropertyWithRequiredId;

import com.azure.spring.data.gremlin.common.Constants;
import com.azure.spring.data.gremlin.conversion.source.GremlinSource;
import com.azure.spring.data.gremlin.conversion.source.GremlinSourceEdge;
import com.azure.spring.data.gremlin.exception.GremlinUnexpectedSourceTypeException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

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

        scriptList.add(GREMLIN_PRIMITIVE_GRAPH);                                            // g
        scriptList.add(generateEntityWithRequiredId(sourceEdge.getVertexIdFrom(), VERTEX)); // V(id)
        scriptList.add(generateAsWithAlias(FROM_ALIAS));                                    // from('from')
        scriptList.add(generateEntityWithRequiredId(sourceEdge.getVertexIdTo(), VERTEX));   // V(id)
        scriptList.add(generateAsWithAlias(TO_ALIAS));                                      // to('to')
        scriptList.add(generateAddEntityWithLabel(sourceEdge.getLabel(), EDGE));            // addE(label)
        scriptList.add(generateEdgeDirection(FROM_ALIAS, TO_ALIAS));                        // from('from').to('to')

        source.getId().ifPresent(id -> scriptList.add(generatePropertyWithRequiredId(id))); // property(id, xxx)

        scriptList.addAll(generateProperties(source.getProperties()));

        return completeScript(scriptList);
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
            GREMLIN_PRIMITIVE_GRAPH,             // g
            GREMLIN_PRIMITIVE_EDGE_ALL,          // E()
            generateHasLabel(source.getLabel()), // has(label, 'label')
            GREMLIN_PRIMITIVE_DROP               // drop()
        );

        return completeScript(scriptList);
    }

    @Override
    public <T> List<String> generateFindByIdScript(@NonNull GremlinSource<T> source) {
        if (!(source instanceof GremlinSourceEdge)) {
            throw new GremlinUnexpectedSourceTypeException("should be the instance of GremlinSourceEdge");
        }

        Assert.isTrue(source.getId().isPresent(), "GremlinSource should contain id.");

        final List<String> scriptList = Arrays.asList(
            GREMLIN_PRIMITIVE_GRAPH,                                 // g
            GREMLIN_PRIMITIVE_EDGE_ALL,                              // E()
            generateHasId(source.getId().get(), source.getIdField()) // hasId(xxx)
        );

        return completeScript(scriptList);
    }

    @Override
    public <T> List<String> generateUpdateScript(@NonNull GremlinSource<T> source) {
        if (!(source instanceof GremlinSourceEdge)) {
            throw new GremlinUnexpectedSourceTypeException("should be the instance of GremlinSourceEdge");
        }

        final List<String> scriptList = new ArrayList<>();

        Assert.isTrue(source.getId().isPresent(), "GremlinSource should contain id.");

        scriptList.add(GREMLIN_PRIMITIVE_GRAPH);                                  // g
        scriptList.add(generateEntityWithRequiredId(source.getId().get(), EDGE)); // E(id)

        scriptList.addAll(generateProperties(source.getProperties()));

        return completeScript(scriptList);
    }

    @Override
    public <T> List<String> generateFindAllScript(@NonNull GremlinSource<T> source) {
        if (!(source instanceof GremlinSourceEdge)) {
            throw new GremlinUnexpectedSourceTypeException("should be the instance of GremlinSourceEdge");
        }

        final String className = source.getProperties().get(GREMLIN_PROPERTY_CLASSNAME).toString();
        Assert.notNull(className, "GremlinSource should contain predefined className");

        final List<String> scriptList = Arrays.asList(
            GREMLIN_PRIMITIVE_GRAPH,                           // g
            GREMLIN_PRIMITIVE_EDGE_ALL,                        // E()
            generateHasLabel(source.getLabel()),               // has(label, 'label')
            generateHas(GREMLIN_PROPERTY_CLASSNAME, className) // has(_classname, 'xxxxxx')
        );

        return completeScript(scriptList);
    }

    @Override
    public <T> List<String> generateDeleteByIdScript(@NonNull GremlinSource<T> source) {
        if (!(source instanceof GremlinSourceEdge)) {
            throw new GremlinUnexpectedSourceTypeException("should be the instance of GremlinSourceEdge");
        }

        Assert.isTrue(source.getId().isPresent(), "GremlinSource should contain id.");

        final List<String> scriptList = Arrays.asList(
            GREMLIN_PRIMITIVE_GRAPH,                                  // g
            GREMLIN_PRIMITIVE_EDGE_ALL,                               // E()
            generateHasId(source.getId().get(), source.getIdField()), // hasId(xxx)
            GREMLIN_PRIMITIVE_DROP                                    // drop()
        );

        return completeScript(scriptList);
    }

    @Override
    public <T> List<String> generateCountScript(@NonNull GremlinSource<T> source) {
        if (!(source instanceof GremlinSourceEdge)) {
            throw new GremlinUnexpectedSourceTypeException("should be the instance of GremlinSourceEdge");
        }

        return Collections.singletonList(GREMLIN_SCRIPT_EDGE_ALL);
    }
}

