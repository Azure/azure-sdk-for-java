// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.openrewrite.recipe;

import com.azure.openrewrite.TransformSetHeaderToAddHeaderRecipe;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import static org.openrewrite.java.Assertions.java;

/**
 * Tests for {@link com.azure.openrewrite.TransformSetHeaderToAddHeaderRecipe}.
 */
public class TransformSetHeaderToAddHeaderTest extends RecipeTestBase {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new TransformSetHeaderToAddHeaderRecipe());
    }

    @Test
    public void testTransformSetHeaderToAddHeader() {
        @Language("java") String before = "import com.azure.core.http.policy.RequestContext;\n"
            + "\n"
            + "public class TestSetHeader {\n"
            + "    public void test() {\n"
            + "        RequestContext.Builder builder = new RequestContext.Builder();\n"
            + "        builder.setHeader(\"Content-Type\", \"application/json\");\n"
            + "    }\n"
            + "}";

        @Language("java") String after = "import com.azure.core.http.policy.RequestContext;\n"
            + "import io.clientcore.core.http.models.HttpHeaderName;\n"
            + "\n"
            + "public class TestSetHeader {\n"
            + "    public void test() {\n"
            + "        RequestContext.Builder builder = new RequestContext.Builder();\n"
            + "        builder.addHeader(HttpHeaderName.fromString(\"Content-Type\"), \"application/json\");\n"
            + "    }\n"
            + "}";

        rewriteRun(
            java(before, after)
        );
    }

    @Test
    public void testTransformChainedSetHeader() {
        @Language("java") String before = "import com.azure.core.http.policy.RequestContext;\n"
            + "\n"
            + "public class TestChainedSetHeader {\n"
            + "    public void test() {\n"
            + "        new RequestContext.Builder()\n"
            + "            .setHeader(\"Content-Type\", \"application/json\")\n"
            + "            .setHeader(\"Authorization\", \"Bearer token\");\n"
            + "    }\n"
            + "}";

        @Language("java") String after = "import com.azure.core.http.policy.RequestContext;\n"
            + "import io.clientcore.core.http.models.HttpHeaderName;\n"
            + "\n"
            + "public class TestChainedSetHeader {\n"
            + "    public void test() {\n"
            + "        new RequestContext.Builder()\n"
            + "            .addHeader(HttpHeaderName.fromString(\"Content-Type\"), \"application/json\")\n"
            + "            .addHeader(HttpHeaderName.fromString(\"Authorization\"), \"Bearer token\");\n"
            + "    }\n"
            + "}";

        rewriteRun(
            java(before, after)
        );
    }
}