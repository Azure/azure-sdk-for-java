// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;

/**
 * Class for testing serialization.
 */
public class NewFooChild extends NewFoo {
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        return toJsonInternal(jsonWriter, "newfoochild");
    }

    public static NewFooChild fromJson(JsonReader jsonReader) {
        return (NewFooChild) fromJsonInternal(jsonReader, "newfoochild");
    }
}
