// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.test;

import com.azure.search.models.AccessCondition;

import java.lang.reflect.Field;

/**
 * Generic access conditions unit tests for different entity types
 */
public abstract class AccessConditionBase {

    /**
     * Gets the ETag field using reflection
     *
     * @param obj and object with ETag field
     * @return the field
     * @throws NoSuchFieldException
     */
    protected Field getETagField(Object obj) throws NoSuchFieldException {
        Class cls = obj.getClass();
        Field field = cls.getDeclaredField("eTag");
        field.setAccessible(true);
        return field;
    }

    /**
     * Gets the actual etag string from an object using reflection
     *
     * @param obj any object with etag field
     * @return the etag string or empty string on error
     */
    protected String getEtag(Object obj) {
        Field etagField;
        try {
            etagField = getETagField(obj);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return "";
        }

        try {
            return (String) etagField.get(obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Constructs an access condition such that an operation will be performed only if the resource does not exist.
     *
     * @return an AccessCondition object that represents a condition where a resource does not exist
     */
    public static AccessCondition generateIfNotExistsAccessCondition() {
        // Setting this access condition modifies the request to include the HTTP If-None-Match conditional header set to "*"
        return new AccessCondition().setIfNoneMatch("*");
    }

    /**
     * Constructs an access condition such that an operation will be performed only if the resource exists.
     *
     * @return an AccessCondition object that represents a condition where a resource exists
     */
    public static AccessCondition generateIfExistsAccessCondition() {

        return new AccessCondition().setIfMatch("*");
    }

    /**
     * Constructs an access condition such that an operation will be performed only if the resource's current ETag
     * value matches the specified ETag value.
     *
     * @param eTag the ETag value to check against the resource's ETag
     * @return An AccessCondition object that represents the If-Match condition
     */
    public static AccessCondition generateIfNotChangedAccessCondition(String eTag) {
        return new AccessCondition().setIfMatch(eTag);
    }
}
