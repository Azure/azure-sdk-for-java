// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.jca.http;

/**
 * The JSON converter API.
 */
public interface JsonConverter {

    /**
     * To JSON.
     *
     * @param object the object to transform.
     * @return the JSON string.
     */
    String toJson(Object object);

    /**
     * From JSON.
     *
     * @param string the JSON string to transform.
     * @param resultClass the result class.
     * @return the object, or null if the conversion failed.
     */
    Object fromJson(String string, Class<?> resultClass);
}
