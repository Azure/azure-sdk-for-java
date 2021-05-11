// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.conversion.script;

import static com.azure.spring.data.gremlin.common.Constants.GREMLIN_PRIMITIVE_DROP;
import static com.azure.spring.data.gremlin.common.Constants.GREMLIN_PRIMITIVE_GRAPH;
import static com.azure.spring.data.gremlin.common.Constants.GREMLIN_PRIMITIVE_VERTEX_ALL;
import static com.azure.spring.data.gremlin.common.Constants.GREMLIN_PROPERTY_CLASSNAME;
import static com.azure.spring.data.gremlin.common.Constants.GREMLIN_SCRIPT_VERTEX_ALL;
import static com.azure.spring.data.gremlin.common.Constants.GREMLIN_SCRIPT_VERTEX_DROP_ALL;
import static com.azure.spring.data.gremlin.common.GremlinEntityType.VERTEX;
import static com.azure.spring.data.gremlin.conversion.script.GremlinScriptLiteralHelper.completeScript;
import static com.azure.spring.data.gremlin.conversion.script.GremlinScriptLiteralHelper.generateAddEntityWithLabel;
import static com.azure.spring.data.gremlin.conversion.script.GremlinScriptLiteralHelper.generateEntityWithRequiredId;
import static com.azure.spring.data.gremlin.conversion.script.GremlinScriptLiteralHelper.generateHas;
import static com.azure.spring.data.gremlin.conversion.script.GremlinScriptLiteralHelper.generateHasId;
import static com.azure.spring.data.gremlin.conversion.script.GremlinScriptLiteralHelper.generateHasLabel;
import static com.azure.spring.data.gremlin.conversion.script.GremlinScriptLiteralHelper.generateProperties;
import static com.azure.spring.data.gremlin.conversion.script.GremlinScriptLiteralHelper.generatePropertyWithRequiredId;

import com.azure.spring.data.gremlin.conversion.source.GremlinSource;
import com.azure.spring.data.gremlin.conversion.source.GremlinSourceVertex;
import com.azure.spring.data.gremlin.exception.GremlinUnexpectedSourceTypeException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

public class GremlinScriptLiteralVertex implements GremlinScriptLiteral {

    @Override
    public <T> List<String> generateInsertScript(@NonNull GremlinSource<T> source) {
        Assert.notNull(source, "source should not be null");
        if (!(source instanceof GremlinSourceVertex)) {
            throw new GremlinUnexpectedSourceTypeException("should be the instance of GremlinSourceVertex");
        }

        final List<String> scriptList = new ArrayList<>();

        scriptList.add(GREMLIN_PRIMITIVE_GRAPH);                                            // g
        scriptList.add(generateAddEntityWithLabel(source.getLabel(), VERTEX));              // addV('label')

        source.getId().ifPresent(id -> scriptList.add(generatePropertyWithRequiredId(id))); // property(id, xxx)

        scriptList.addAll(generateProperties(source.getProperties()));

        return completeScript(scriptList);
    }

    @Override
    public List<String> generateDeleteAllScript() {
        return Collections.singletonList(GREMLIN_SCRIPT_VERTEX_DROP_ALL);
    }

    @Override
    public <T> List<String> generateDeleteAllByClassScript(@NonNull GremlinSource<T> source) {
        if (!(source instanceof GremlinSourceVertex)) {
            throw new GremlinUnexpectedSourceTypeException("should be the instance of GremlinSourceVertex");
        }

        final List<String> scriptList = Arrays.asList(
            GREMLIN_PRIMITIVE_GRAPH,             // g
            GREMLIN_PRIMITIVE_VERTEX_ALL,        // V()
            generateHasLabel(source.getLabel()), // has(label, 'label')
            GREMLIN_PRIMITIVE_DROP               // drop()
        );

        return completeScript(scriptList);
    }

    @Override
    public <T> List<String> generateFindByIdScript(@NonNull GremlinSource<T> source) {
        if (!(source instanceof GremlinSourceVertex)) {
            throw new GremlinUnexpectedSourceTypeException("should be the instance of GremlinSourceVertex");
        }

        Assert.isTrue(source.getId().isPresent(), "GremlinSource should contain id.");

        final List<String> scriptList = Arrays.asList(
            GREMLIN_PRIMITIVE_GRAPH,                                 // g
            GREMLIN_PRIMITIVE_VERTEX_ALL,                            // V()
            generateHasId(source.getId().get(), source.getIdField()) // hasId(xxx)
        );

        return completeScript(scriptList);
    }

    @Override
    public <T> List<String> generateUpdateScript(@NonNull GremlinSource<T> source) {
        if (!(source instanceof GremlinSourceVertex)) {
            throw new GremlinUnexpectedSourceTypeException("should be the instance of GremlinSourceVertex");
        }

        final List<String> scriptList = new ArrayList<>();

        Assert.isTrue(source.getId().isPresent(), "GremlinSource should contain id.");

        scriptList.add(GREMLIN_PRIMITIVE_GRAPH);                                    // g
        scriptList.add(generateEntityWithRequiredId(source.getId().get(), VERTEX)); // V(id)
        scriptList.addAll(generateProperties(source.getProperties()));

        return completeScript(scriptList);
    }

    @Override
    public <T> List<String> generateFindAllScript(@NonNull GremlinSource<T> source) {
        if (!(source instanceof GremlinSourceVertex)) {
            throw new GremlinUnexpectedSourceTypeException("should be the instance of GremlinSourceVertex");
        }

        final String classname = source.getProperties().get(GREMLIN_PROPERTY_CLASSNAME).toString();
        Assert.notNull(classname, "GremlinSource should contain predefined classname");

        final List<String> scriptList = Arrays.asList(
            GREMLIN_PRIMITIVE_GRAPH,                           // g
            GREMLIN_PRIMITIVE_VERTEX_ALL,                      // V()
            generateHasLabel(source.getLabel()),               // has(label, 'label')
            generateHas(GREMLIN_PROPERTY_CLASSNAME, classname) // has(_classname, 'xxxxxx')
        );

        return completeScript(scriptList);
    }

    @Override
    public <T> List<String> generateDeleteByIdScript(@NonNull GremlinSource<T> source) {
        if (!(source instanceof GremlinSourceVertex)) {
            throw new GremlinUnexpectedSourceTypeException("should be the instance of GremlinSourceVertex");
        }

        Assert.isTrue(source.getId().isPresent(), "GremlinSource should contain id.");

        final List<String> scriptList = Arrays.asList(
            GREMLIN_PRIMITIVE_GRAPH,                                  // g
            GREMLIN_PRIMITIVE_VERTEX_ALL,                             // E()
            generateHasId(source.getId().get(), source.getIdField()), // hasId(xxx)
            GREMLIN_PRIMITIVE_DROP                                    // drop()
        );

        return completeScript(scriptList);
    }

    @Override
    public <T> List<String> generateCountScript(@NonNull GremlinSource<T> source) {
        if (!(source instanceof GremlinSourceVertex)) {
            throw new GremlinUnexpectedSourceTypeException("should be the instance of GremlinSourceVertex");
        }

        return Collections.singletonList(GREMLIN_SCRIPT_VERTEX_ALL);
    }
}

