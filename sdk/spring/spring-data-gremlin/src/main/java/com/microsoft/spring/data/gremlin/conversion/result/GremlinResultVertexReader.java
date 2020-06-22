// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.spring.data.gremlin.conversion.result;

import com.microsoft.spring.data.gremlin.common.GremlinUtils;
import com.microsoft.spring.data.gremlin.conversion.source.GremlinSource;
import com.microsoft.spring.data.gremlin.conversion.source.GremlinSourceVertex;
import com.microsoft.spring.data.gremlin.exception.GremlinUnexpectedSourceTypeException;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

import static com.microsoft.spring.data.gremlin.common.Constants.GREMLIN_PROPERTY_CLASSNAME;
import static com.microsoft.spring.data.gremlin.common.Constants.PROPERTY_ID;
import static com.microsoft.spring.data.gremlin.common.Constants.PROPERTY_LABEL;
import static com.microsoft.spring.data.gremlin.common.Constants.PROPERTY_PROPERTIES;
import static com.microsoft.spring.data.gremlin.common.Constants.PROPERTY_TYPE;
import static com.microsoft.spring.data.gremlin.common.Constants.RESULT_TYPE_VERTEX;

public class GremlinResultVertexReader extends AbstractGremlinResultReader {

    private <T> void validate(List<Result> results, GremlinSource<T> source) {
        Assert.notNull(results, "Results should not be null.");
        Assert.notNull(source, "GremlinSource should not be null.");
        Assert.isTrue(results.size() == 1, "Vertex should contain only one result.");

        final Result result = results.get(0);

        Assert.isInstanceOf(Map.class, result.getObject(), "should be one instance of Map");

        @SuppressWarnings("unchecked") final Map<String, Object> map = (Map<String, Object>) result.getObject();

        Assert.isTrue(map.containsKey(PROPERTY_ID), "should contain id property");
        Assert.isTrue(map.containsKey(PROPERTY_LABEL), "should contain label property");
        Assert.isTrue(map.containsKey(PROPERTY_TYPE), "should contain type property");
        Assert.isTrue(map.containsKey(PROPERTY_PROPERTIES), "should contain properties property");
        Assert.isTrue(map.get(PROPERTY_TYPE).equals(RESULT_TYPE_VERTEX), "must be vertex type");

        Assert.isInstanceOf(Map.class, map.get(PROPERTY_PROPERTIES), "should be one instance of Map");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void read(@NonNull List<Result> results, @NonNull GremlinSource<T> source) {
        if (!(source instanceof GremlinSourceVertex)) {
            throw new GremlinUnexpectedSourceTypeException("Should be instance of GremlinSourceVertex");
        }

        validate(results, source);

        final Map<String, Object> map = (Map<String, Object>) results.get(0).getObject();
        final Map<String, Object> properties = (Map<String, Object>) map.get(PROPERTY_PROPERTIES);

        super.readResultProperties(properties, source);

        final String className = source.getProperties().get(GREMLIN_PROPERTY_CLASSNAME).toString();

        source.setIdField(GremlinUtils.getIdField(GremlinUtils.toEntityClass(className)));
        source.setId(map.get(PROPERTY_ID));
        source.setLabel(map.get(PROPERTY_LABEL).toString());
    }
}
