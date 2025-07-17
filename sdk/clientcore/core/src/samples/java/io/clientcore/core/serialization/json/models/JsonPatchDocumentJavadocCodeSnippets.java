// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.serialization.json.models;

import java.io.IOException;
import java.util.Collections;

/**
 * Code snippets for {@link JsonPatchDocument}.
 */
public final class JsonPatchDocumentJavadocCodeSnippets {
    /**
     * Codesnippets for {@link JsonPatchDocument#appendAdd(String, Object)}.
     */
    public void appendAdd() throws IOException {
        JsonPatchDocument jsonPatchDocument = new JsonPatchDocument();
        // BEGIN: io.clientcore.core.serialization.json.models.JsonPatchDocument.appendAdd#String-Object
        /*
         * Add an object member to the JSON document { "foo" : "bar" } to get the JSON document
         * { "bar": "foo", "foo": "bar" }.
         */
        jsonPatchDocument.appendAdd("/bar", "foo");

        /*
         * Add an array element to the JSON document { "foo": [ "fizz", "fizzbuzz" ] } to get the JSON document
         * { "foo": [ "fizz", "buzz", "fizzbuzz" ] }.
         */
        jsonPatchDocument.appendAdd("/foo/1", "buzz");

        /*
         * Add a nested member to the JSON document { "foo": "bar" } to get the JSON document
         * { "foo": "bar", "child": { "grandchild": { } } }.
         */
        jsonPatchDocument.appendAdd("/child", Collections.singletonMap("grandchild", Collections.emptyMap()));

        /*
         * Add an array element to the JSON document { "foo": [ "fizz", "buzz" ] } to get the JSON document
         * { "foo": [ "fizz", "buzz", "fizzbuzz" ] }.
         */
        jsonPatchDocument.appendAdd("/foo/-", "fizzbuzz");
        // END: io.clientcore.core.serialization.json.models.JsonPatchDocument.appendAdd#String-Object
    }

    /**
     * Codesnippets for {@link JsonPatchDocument#appendAddRaw(String, String)}.
     */
    public void appendAddRaw() {
        JsonPatchDocument jsonPatchDocument = new JsonPatchDocument();
        // BEGIN: io.clientcore.core.serialization.json.models.JsonPatchDocument.appendAddRaw#String-String
        /*
         * Add an object member to the JSON document { "foo" : "bar" } to get the JSON document
         * { "bar": "foo", "foo": "bar" }.
         */
        jsonPatchDocument.appendAddRaw("/bar", "\"foo\"");

        /*
         * Add an array element to the JSON document { "foo": [ "fizz", "fizzbuzz" ] } to get the JSON document
         * { "foo": [ "fizz", "buzz", "fizzbuzz" ] }.
         */
        jsonPatchDocument.appendAddRaw("/foo/1", "\"buzz\"");

        /*
         * Add a nested member to the JSON document { "foo": "bar" } to get the JSON document
         * { "foo": "bar", "child": { "grandchild": { } } }.
         */
        jsonPatchDocument.appendAddRaw("/child", "\"child\": { \"grandchild\": { } }");

        /*
         * Add an array element to the JSON document { "foo": [ "fizz", "buzz" ] } to get the JSON document
         * { "foo": [ "fizz", "buzz", "fizzbuzz" ] }.
         */
        jsonPatchDocument.appendAddRaw("/foo/-", "\"fizzbuzz\"");
        // END: io.clientcore.core.serialization.json.models.JsonPatchDocument.appendAddRaw#String-String
    }

    /**
     * Codesnippets for {@link JsonPatchDocument#appendReplace(String, Object)}.
     */
    public void appendReplace() throws IOException {
        JsonPatchDocument jsonPatchDocument = new JsonPatchDocument();
        // BEGIN: io.clientcore.core.serialization.json.models.JsonPatchDocument.appendReplace#String-Object
        /*
         * Replace an object member in the JSON document { "bar": "qux", "foo": "bar" } to get the JSON document
         * { "bar": "foo", "foo": "bar" }.
         */
        jsonPatchDocument.appendReplace("/bar", "foo");

        /*
         * Replace an object member in the JSON document { "foo": "fizz" } to get the JSON document
         * { "foo": [ "fizz", "buzz", "fizzbuzz" ]  }.
         */
        jsonPatchDocument.appendReplace("/foo", new String[] {"fizz", "buzz", "fizzbuzz"});

        /*
         * Given the JSON document { "foo": "bar" } the following is an example of an invalid replace operation as the
         * target path doesn't exist in the document.
         */
        jsonPatchDocument.appendReplace("/baz", "foo");
        // END: io.clientcore.core.serialization.json.models.JsonPatchDocument.appendReplace#String-Object
    }

    /**
     * Codesnippets for {@link JsonPatchDocument#appendReplaceRaw(String, String)}.
     */
    public void appendReplaceRaw() {
        JsonPatchDocument jsonPatchDocument = new JsonPatchDocument();
        // BEGIN: io.clientcore.core.serialization.json.models.JsonPatchDocument.appendReplaceRaw#String-String
        /*
         * Replace an object member in the JSON document { "bar": "qux", "foo": "bar" } to get the JSON document
         * { "bar": "foo", "foo": "bar" }.
         */
        jsonPatchDocument.appendReplaceRaw("/bar", "\"foo\"");

        /*
         * Replace an object member in the JSON document { "foo": "fizz" } to get the JSON document
         * { "foo": [ "fizz", "buzz", "fizzbuzz" ]  }.
         */
        jsonPatchDocument.appendReplaceRaw("/foo", "[ \"fizz\", \"buzz\", \"fizzbuzz\" ]");

        /*
         * Given the JSON document { "foo": "bar" } the following is an example of an invalid replace operation as the
         * target path doesn't exist in the document.
         */
        jsonPatchDocument.appendReplaceRaw("/baz", "\"foo\"");
        // END: io.clientcore.core.serialization.json.models.JsonPatchDocument.appendReplaceRaw#String-String
    }

    /**
     * Codesnippets for {@link JsonPatchDocument#appendCopy(String, String)}.
     */
    public void appendCopy() {
        JsonPatchDocument jsonPatchDocument = new JsonPatchDocument();
        // BEGIN: io.clientcore.core.serialization.json.models.JsonPatchDocument.appendCopy#String-String
        /*
         * Copy an object member in the JSON document { "foo": "bar" } to get the JSON document
         * { "foo": "bar", "copy": "bar" }.
         */
        jsonPatchDocument.appendCopy("/foo", "/copy");

        /*
         * Copy an object member in the JSON document { "foo": { "bar": "baz" } } to get the JSON document
         * { "foo": { "bar": "baz" }, "bar": "baz" }.
         */
        jsonPatchDocument.appendCopy("/foo/bar", "/bar");

        /*
         * Given the JSON document { "foo": "bar" } the following is an example of an invalid copy operation as the
         * target from doesn't exist in the document.
         */
        jsonPatchDocument.appendCopy("/baz", "/fizz");
        // END: io.clientcore.core.serialization.json.models.JsonPatchDocument.appendCopy#String-String
    }

    /**
     * Codesnippets for {@link JsonPatchDocument#appendMove(String, String)}.
     */
    public void appendMove() {
        JsonPatchDocument jsonPatchDocument = new JsonPatchDocument();
        // BEGIN: io.clientcore.core.serialization.json.models.JsonPatchDocument.appendMove#String-String
        /*
         * Move an object member in the JSON document { "foo": "bar", "bar": "foo" } to get the JSON document
         * { "bar": "bar" }.
         */
        jsonPatchDocument.appendMove("/foo", "/bar");

        /*
         * Move an object member in the JSON document { "foo": { "bar": "baz" } } to get the JSON document
         * { "foo": "baz" }.
         */
        jsonPatchDocument.appendMove("/foo/bar", "/foo");

        /*
         * Given the JSON document { "foo": { "bar": "baz" } } the following is an example of an invalid move operation
         * as the target path is a child of the target from.
         */
        jsonPatchDocument.appendMove("/foo", "/foo/bar");

        /*
         * Given the JSON document { "foo": "bar" } the following is an example of an invalid move operation as the
         * target from doesn't exist in the document.
         */
        jsonPatchDocument.appendMove("/baz", "/fizz");
        // END: io.clientcore.core.serialization.json.models.JsonPatchDocument.appendMove#String-String
    }

    /**
     * Codesnippets for {@link JsonPatchDocument#appendRemove(String)}.
     */
    public void appendRemove() {
        JsonPatchDocument jsonPatchDocument = new JsonPatchDocument();
        // BEGIN: io.clientcore.core.serialization.json.models.JsonPatchDocument.appendRemove#String
        /*
         * Remove an object member in the JSON document { "foo": "bar", "bar": "foo" } to get the JSON document
         * { "foo": "bar" }.
         */
        jsonPatchDocument.appendRemove("/bar");

        /*
         * Remove an object member in the JSON document { "foo": { "bar": "baz" } } to get the JSON document
         * { "foo": { } }.
         */
        jsonPatchDocument.appendRemove("/foo/bar");

        /*
         * Given the JSON document { "foo": "bar" } the following is an example of an invalid remove operation as the
         * target from doesn't exist in the document.
         */
        jsonPatchDocument.appendRemove("/baz");
        // END: io.clientcore.core.serialization.json.models.JsonPatchDocument.appendRemove#String
    }

    /**
     * Codesnippets for {@link JsonPatchDocument#appendTest(String, Object)}.
     */
    public void appendTest() throws IOException {
        JsonPatchDocument jsonPatchDocument = new JsonPatchDocument();
        // BEGIN: io.clientcore.core.serialization.json.models.JsonPatchDocument.appendTest#String-Object
        /*
         * Test an object member in the JSON document { "foo": "bar" } to get a successful operation.
         */
        jsonPatchDocument.appendTest("/foo", "bar");

        /*
         * Test an object member in the JSON document { "foo": "bar" } to get a unsuccessful operation.
         */
        jsonPatchDocument.appendTest("/foo", 42);

        /*
         * Given the JSON document { "foo": "bar" } the following is an example of an unsuccessful test operation as
         * the target path doesn't exist in the document.
         */
        jsonPatchDocument.appendTest("/baz", "bar");
        // END: io.clientcore.core.serialization.json.models.JsonPatchDocument.appendTest#String-Object
    }

    /**
     * Codesnippets for {@link JsonPatchDocument#appendTestRaw(String, String)}.
     */
    public void appendTestRaw() {
        JsonPatchDocument jsonPatchDocument = new JsonPatchDocument();
        // BEGIN: io.clientcore.core.serialization.json.models.JsonPatchDocument.appendTestRaw#String-String
        /*
         * Test an object member in the JSON document { "foo": "bar" } to get a successful operation.
         */
        jsonPatchDocument.appendTestRaw("/foo", "\"bar\"");

        /*
         * Test an object member in the JSON document { "foo": "bar" } to get a unsuccessful operation.
         */
        jsonPatchDocument.appendTestRaw("/foo", "42");

        /*
         * Given the JSON document { "foo": "bar" } the following is an example of an unsuccessful test operation as
         * the target path doesn't exist in the document.
         */
        jsonPatchDocument.appendTestRaw("/baz", "\"bar\"");
        // END: io.clientcore.core.serialization.json.models.JsonPatchDocument.appendTestRaw#String-String
    }
}
