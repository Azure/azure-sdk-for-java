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
        spec.recipe(new HttpHeadersCustomRecipe())
            .parser(JavaParser.fromJavaVersion()
                .classpath("azure-core", "core", "jackson-core"));
    }

    @Test
    void testSetAllMapTransformation() {
        rewriteRun(
            java(
                """
                import com.azure.core.http.HttpHeaders;
                import java.util.Map;
                import java.util.List;
                import java.util.HashMap;
                import java.util.Arrays;
                
                public class Test {
                    public void example() {
                        HttpHeaders headers = new HttpHeaders();
                        Map<String, List<String>> headerMap = new HashMap<>();
                        headerMap.put("Content-Type", Arrays.asList("application/json"));
                        headerMap.put("Authorization", Arrays.asList("Bearer token"));
                        
                        headers.setAll(headerMap);
                    }
                }
                """,
                """
                import io.clientcore.core.http.models.HttpHeaders;
                import io.clientcore.core.http.models.HttpHeaderName;
                import java.util.Map;
                import java.util.List;
                import java.util.HashMap;
                import java.util.Arrays;
                
                public class Test {
                    public void example() {
                        HttpHeaders headers = new HttpHeaders();
                        Map<String, List<String>> headerMap = new HashMap<>();
                        headerMap.put("Content-Type", Arrays.asList("application/json"));
                        headerMap.put("Authorization", Arrays.asList("Bearer token"));
                        
                        headers.setAll(headerMap.entrySet().stream().collect(() -> new HttpHeaders(), (h, entry) -> h.set(HttpHeaderName.fromString(entry.getKey()), entry.getValue()), (h1, h2) -> h1.setAll(h2)));
                    }
                }
                """
            )
        );
    }
}