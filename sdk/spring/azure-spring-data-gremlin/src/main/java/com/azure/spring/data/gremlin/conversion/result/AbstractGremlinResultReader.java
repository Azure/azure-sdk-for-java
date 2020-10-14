// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.conversion.result;

import com.azure.spring.data.gremlin.common.Constants;
import com.azure.spring.data.gremlin.conversion.source.GremlinSource;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

// TODO: seems only for Vertex.
public abstract class AbstractGremlinResultReader implements GremlinResultsReader {

    /**
     * properties's organization is a little complicated.
     * <p>
     * properties is {@link LinkedHashMap}&lt;K, V> <br>
     * K is {@link String} <br>
     * V is {@link ArrayList}&lt;T> <br>
     * T is {@link LinkedHashMap}&lt;{@link String}, {@link String}>
     */
    private Object readProperty(@NonNull Object value) {
        Assert.isInstanceOf(ArrayList.class, value, "should be instance of ArrayList");

        @SuppressWarnings("unchecked") final ArrayList<LinkedHashMap<String, String>> mapList
                = (ArrayList<LinkedHashMap<String, String>>) value;

        Assert.isTrue(mapList.size() == 1, "should be only 1 element in ArrayList");

        return mapList.get(0).get(Constants.PROPERTY_VALUE);
    }

    protected <T> void readResultProperties(@NonNull Map<String, Object> properties, @NonNull GremlinSource<T> source) {
        source.getProperties().clear();
        properties.forEach((key, value) -> source.setProperty(key, this.readProperty(value)));
    }
}
