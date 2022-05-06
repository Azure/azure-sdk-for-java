// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;

/**
 * Class for testing serialization.
 */
public class FooChild extends Foo {
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        return toJsonInternal(jsonWriter, "foochild");
    }

    public static FooChild fromJson(JsonReader jsonReader) {
        return (FooChild) fromJsonInternal(jsonReader, "foochild");
    }
}
