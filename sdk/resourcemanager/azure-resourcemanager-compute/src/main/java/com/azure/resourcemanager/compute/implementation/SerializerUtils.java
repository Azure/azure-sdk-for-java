// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.implementation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

public final class SerializerUtils {

    private static ObjectMapper objectMapper;

    public static synchronized ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            JacksonAnnotationIntrospector annotationIntrospector =
                new JacksonAnnotationIntrospector() {
                    @Override
                    public JsonProperty.Access findPropertyAccess(Annotated annotated) {
                        JsonProperty.Access access = super.findPropertyAccess(annotated);
                        if (access == JsonProperty.Access.WRITE_ONLY) {
                            return JsonProperty.Access.AUTO;
                        }
                        return access;
                    }
                };
            objectMapper.setAnnotationIntrospector(annotationIntrospector);
        }
        return objectMapper;
    }
}
