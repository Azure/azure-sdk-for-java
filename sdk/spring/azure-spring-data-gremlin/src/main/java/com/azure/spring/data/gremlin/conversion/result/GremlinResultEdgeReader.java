// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.conversion.result;

import com.azure.spring.data.gremlin.common.Constants;
import com.azure.spring.data.gremlin.common.GremlinUtils;
import com.azure.spring.data.gremlin.conversion.source.GremlinSource;
import com.azure.spring.data.gremlin.conversion.source.GremlinSourceEdge;
import com.azure.spring.data.gremlin.exception.GremlinUnexpectedSourceTypeException;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

public class GremlinResultEdgeReader extends AbstractGremlinResultReader {

    private <T> void readProperties(@NonNull GremlinSource<T> source, @Nullable Map<String, Object> properties) {
        Assert.notNull(source, "source should not be null");
        if (properties != null) {
            properties.forEach(source::setProperty);
        }
    }

    private <T> void validate(List<Result> results, GremlinSource<T> source) {
        Assert.notNull(results, "Results should not be null.");
        Assert.notNull(source, "GremlinSource should not be null.");
        Assert.isTrue(results.size() == 1, "Edge should contain only one result.");

        final Result result = results.get(0);

        Assert.isInstanceOf(Map.class, result.getObject(), "should be one instance of Map");

        @SuppressWarnings("unchecked") final Map<String, Object> map = (Map<String, Object>) result.getObject();

        Assert.isTrue(map.containsKey(Constants.PROPERTY_ID), "should contain id property");
        Assert.isTrue(map.containsKey(Constants.PROPERTY_LABEL), "should contain label property");
        Assert.isTrue(map.containsKey(Constants.PROPERTY_TYPE), "should contain type property");
        Assert.isTrue(map.containsKey(Constants.PROPERTY_INV), "should contain inV property");
        Assert.isTrue(map.containsKey(Constants.PROPERTY_OUTV), "should contain outV property");
        Assert.isTrue(map.get(Constants.PROPERTY_TYPE).equals(Constants.RESULT_TYPE_EDGE), "must be vertex type");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void read(@NonNull List<Result> results, @NonNull GremlinSource<T> source) {
        if (!(source instanceof GremlinSourceEdge)) {
            throw new GremlinUnexpectedSourceTypeException("Should be instance of GremlinSourceEdge");
        }

        validate(results, source);

        final GremlinSourceEdge<T> sourceEdge = (GremlinSourceEdge<T>) source;
        final Map<String, Object> map = (Map<String, Object>) results.get(0).getObject();

        this.readProperties(source, (Map<String, Object>) map.get(Constants.PROPERTY_PROPERTIES));

        final String className = source.getProperties().get(Constants.GREMLIN_PROPERTY_CLASSNAME).toString();

        sourceEdge.setIdField(GremlinUtils.getIdField(GremlinUtils.toEntityClass(className)));
        sourceEdge.setId(map.get(Constants.PROPERTY_ID));
        sourceEdge.setLabel(map.get(Constants.PROPERTY_LABEL).toString());
        sourceEdge.setVertexIdFrom(map.get(Constants.PROPERTY_OUTV));
        sourceEdge.setVertexIdTo(map.get(Constants.PROPERTY_INV));
    }
}
