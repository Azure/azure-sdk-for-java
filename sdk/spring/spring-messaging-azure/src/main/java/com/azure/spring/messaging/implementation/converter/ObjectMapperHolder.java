// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.implementation.converter;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class ObjectMapperHolder {

    private ObjectMapperHolder() {

    }

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

}
