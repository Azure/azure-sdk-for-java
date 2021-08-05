// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.search.documents.indexes.models.SearchField;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation that indicates the field or method is to be ignored by converting to SearchField. The annotation is
 * useful in situations where a property definition doesn't cleanly map to a {@link SearchField} object, but its values
 * still need to be converted to and from JSON. In that case, ignore annotation in json serializer library can't be used
 * since it would disable JSON conversion. An example of a scenario where this is useful is when mapping between a
 * string field in Azure Cognitive Search and an enum property.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldBuilderIgnore {
}
