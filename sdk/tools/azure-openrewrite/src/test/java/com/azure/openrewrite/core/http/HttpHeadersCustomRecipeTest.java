// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.openrewrite.core.http;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class HttpHeadersCustomRecipeTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new HttpHeadersCustomRecipe());
    }

    @Test
    void testSetAllMapTransformation() {
        rewriteRun(
            java(
                "import com.azure.core.http.HttpHeaders;\n" +
                "import java.util.Map;\n" +
                "import java.util.List;\n" +
                "import java.util.HashMap;\n" +
                "import java.util.Arrays;\n" +
                "\n" +
                "public class Test {\n" +
                "    public void example() {\n" +
                "        HttpHeaders headers = new HttpHeaders();\n" +
                "        Map<String, List<String>> headerMap = new HashMap<>();\n" +
                "        headerMap.put(\"Content-Type\", Arrays.asList(\"application/json\"));\n" +
                "        headerMap.put(\"Authorization\", Arrays.asList(\"Bearer token\"));\n" +
                "        \n" +
                "        headers.setAll(headerMap);\n" +
                "    }\n" +
                "}",
                "import io.clientcore.core.http.models.HttpHeaders;\n" +
                "import io.clientcore.core.http.models.HttpHeaderName;\n" +
                "import java.util.Map;\n" +
                "import java.util.List;\n" +
                "import java.util.HashMap;\n" +
                "import java.util.Arrays;\n" +
                "\n" +
                "public class Test {\n" +
                "    public void example() {\n" +
                "        HttpHeaders headers = new HttpHeaders();\n" +
                "        Map<String, List<String>> headerMap = new HashMap<>();\n" +
                "        headerMap.put(\"Content-Type\", Arrays.asList(\"application/json\"));\n" +
                "        headerMap.put(\"Authorization\", Arrays.asList(\"Bearer token\"));\n" +
                "        \n" +
                "        headers.setAll(headerMap.entrySet().stream().collect(HttpHeaders::new, (newHeaders, entry) -> newHeaders.set(HttpHeaderName.fromString(entry.getKey()), entry.getValue() instanceof java.util.List ? (java.util.List<String>) entry.getValue() : java.util.Collections.singletonList(entry.getValue().toString())), HttpHeaders::setAll));\n" +
                "    }\n" +
                "}"
            )
        );
    }
}