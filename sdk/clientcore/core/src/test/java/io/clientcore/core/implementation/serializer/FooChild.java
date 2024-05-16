// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.serializer;

import io.clientcore.core.json.JsonReader;

import java.io.IOException;

/**
 * Class for testing serialization.
 */
public class FooChild extends Foo {
    public static FooChild fromJson(JsonReader reader) throws IOException {
        FooChild fooChild = new FooChild();
        Foo foo = Foo.fromJson(reader);

        fooChild.bar(foo.bar());
        fooChild.baz(foo.baz());
        fooChild.qux(foo.qux());
        fooChild.moreProps(foo.moreProps());
        fooChild.empty(foo.empty());
        fooChild.additionalProperties(foo.additionalProperties());

        return fooChild;
    }
}
