package com.azure.common.implementation.serializer;

import com.azure.common.http.rest.Page;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.List;

/**
 * Base class that is able to deserialize a Page JSON response. The JSON formats that it understands are:
 * {
 *      "nextLink": "",
 *      "value": [{ serialized(T) }, ... ]
 * }
 * or
 * {
 *      "nextPageLink": "",
 *      "items": [{ serialized(T) }, ... ]
 * }
 * or any other cases where the property names of that type are swapped
 * @param <T>
 */
class ItemPage<T> implements Page<T> {
    private List<T> items;

    private String nextLink;

    @Override
    @JsonGetter()
    public List<T> items() {
        return items;
    }

    @Override
    @JsonGetter()
    public String nextLink() {
        return nextLink;
    }

    @JsonSetter()
    public void setNextLink(String nextLink) {
        this.nextLink = nextLink;
    }

    @JsonSetter()
    public void setValue(List<T> items) {
        this.items = items;
    }

    /**
     * This private method is to support deserialization where the "nextLink" property is the "nextPageLink" property
     */
    @JsonSetter()
    private void setNextPageLink(String nextLink) {
        this.nextLink = nextLink;
    }

    /**
     * This private setter is to support deserialization cases where the list of T uses "items" and not "value".
     */
    @JsonSetter()
    private void setItems(List<T> items) {
        this.items = items;
    }
}
