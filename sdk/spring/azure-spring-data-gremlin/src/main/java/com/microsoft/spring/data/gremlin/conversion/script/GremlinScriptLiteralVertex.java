// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.spring.data.gremlin.conversion.script;

import com.microsoft.spring.data.gremlin.common.Constants;
import com.microsoft.spring.data.gremlin.common.GremlinEntityType;
import com.microsoft.spring.data.gremlin.conversion.source.GremlinSource;
import com.microsoft.spring.data.gremlin.conversion.source.GremlinSourceVertex;
import com.microsoft.spring.data.gremlin.exception.GremlinUnexpectedSourceTypeException;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GremlinScriptLiteralVertex implements GremlinScriptLiteral {

    @Override
    public <T> List<String> generateInsertScript(@NonNull GremlinSource<T> source) {
        Assert.notNull(source, "source should not be null");
        if (!(source instanceof GremlinSourceVertex)) {
            throw new GremlinUnexpectedSourceTypeException("should be the instance of GremlinSourceVertex");
        }

        final List<String> scriptList = new ArrayList<>();

        scriptList.add(Constants.GREMLIN_PRIMITIVE_GRAPH);                                            // g
        scriptList.add(GremlinScriptLiteralHelper.generateAddEntityWithLabel(source.getLabel(), GremlinEntityType.VERTEX));              // addV('label')

        source.getId().ifPresent(id -> scriptList.add(GremlinScriptLiteralHelper.generatePropertyWithRequiredId(id))); // property(id, xxx)

        scriptList.addAll(GremlinScriptLiteralHelper.generateProperties(source.getProperties()));

        return GremlinScriptLiteralHelper.completeScript(scriptList);
    }

    @Override
    public List<String> generateDeleteAllScript() {
        return Collections.singletonList(Constants.GREMLIN_SCRIPT_VERTEX_DROP_ALL);
    }

    @Override
    public <T> List<String> generateDeleteAllByClassScript(@NonNull GremlinSource<T> source) {
        if (!(source instanceof GremlinSourceVertex)) {
            throw new GremlinUnexpectedSourceTypeException("should be the instance of GremlinSourceVertex");
        }

        final List<String> scriptList = Arrays.asList(
                Constants.GREMLIN_PRIMITIVE_GRAPH,             // g
                Constants.GREMLIN_PRIMITIVE_VERTEX_ALL,        // V()
                GremlinScriptLiteralHelper.generateHasLabel(source.getLabel()), // has(label, 'label')
                Constants.GREMLIN_PRIMITIVE_DROP               // drop()
        );

        return GremlinScriptLiteralHelper.completeScript(scriptList);
    }

    @Override
    public <T> List<String> generateFindByIdScript(@NonNull GremlinSource<T> source) {
        if (!(source instanceof GremlinSourceVertex)) {
            throw new GremlinUnexpectedSourceTypeException("should be the instance of GremlinSourceVertex");
        }

        Assert.isTrue(source.getId().isPresent(), "GremlinSource should contain id.");

        final List<String> scriptList = Arrays.asList(
                Constants.GREMLIN_PRIMITIVE_GRAPH,                                 // g
                Constants.GREMLIN_PRIMITIVE_VERTEX_ALL,                            // V()
                GremlinScriptLiteralHelper.generateHasId(source.getId().get(), source.getIdField()) // hasId(xxx)
        );

        return GremlinScriptLiteralHelper.completeScript(scriptList);
    }

    @Override
    public <T> List<String> generateUpdateScript(@NonNull GremlinSource<T> source) {
        if (!(source instanceof GremlinSourceVertex)) {
            throw new GremlinUnexpectedSourceTypeException("should be the instance of GremlinSourceVertex");
        }

        final List<String> scriptList = new ArrayList<>();

        Assert.isTrue(source.getId().isPresent(), "GremlinSource should contain id.");

        scriptList.add(Constants.GREMLIN_PRIMITIVE_GRAPH);                                    // g
        scriptList.add(GremlinScriptLiteralHelper.generateEntityWithRequiredId(source.getId().get(), GremlinEntityType.VERTEX)); // V(id)
        scriptList.addAll(GremlinScriptLiteralHelper.generateProperties(source.getProperties()));

        return GremlinScriptLiteralHelper.completeScript(scriptList);
    }

    @Override
    public <T> List<String> generateFindAllScript(@NonNull GremlinSource<T> source) {
        if (!(source instanceof GremlinSourceVertex)) {
            throw new GremlinUnexpectedSourceTypeException("should be the instance of GremlinSourceVertex");
        }

        final String classname = source.getProperties().get(Constants.GREMLIN_PROPERTY_CLASSNAME).toString();
        Assert.notNull(classname, "GremlinSource should contain predefined classname");

        final List<String> scriptList = Arrays.asList(
                Constants.GREMLIN_PRIMITIVE_GRAPH,                           // g
                Constants.GREMLIN_PRIMITIVE_VERTEX_ALL,                      // V()
                GremlinScriptLiteralHelper.generateHasLabel(source.getLabel()),               // has(label, 'label')
                GremlinScriptLiteralHelper.generateHas(Constants.GREMLIN_PROPERTY_CLASSNAME, classname) // has(_classname, 'xxxxxx')
        );

        return GremlinScriptLiteralHelper.completeScript(scriptList);
    }

    @Override
    public <T> List<String> generateDeleteByIdScript(@NonNull GremlinSource<T> source) {
        if (!(source instanceof GremlinSourceVertex)) {
            throw new GremlinUnexpectedSourceTypeException("should be the instance of GremlinSourceVertex");
        }

        Assert.isTrue(source.getId().isPresent(), "GremlinSource should contain id.");

        final List<String> scriptList = Arrays.asList(
                Constants.GREMLIN_PRIMITIVE_GRAPH,                                  // g
                Constants.GREMLIN_PRIMITIVE_VERTEX_ALL,                             // E()
                GremlinScriptLiteralHelper.generateHasId(source.getId().get(), source.getIdField()), // hasId(xxx)
                Constants.GREMLIN_PRIMITIVE_DROP                                    // drop()
        );

        return GremlinScriptLiteralHelper.completeScript(scriptList);
    }

    @Override
    public <T> List<String> generateCountScript(@NonNull GremlinSource<T> source) {
        if (!(source instanceof GremlinSourceVertex)) {
            throw new GremlinUnexpectedSourceTypeException("should be the instance of GremlinSourceVertex");
        }

        return Collections.singletonList(Constants.GREMLIN_SCRIPT_VERTEX_ALL);
    }
}

