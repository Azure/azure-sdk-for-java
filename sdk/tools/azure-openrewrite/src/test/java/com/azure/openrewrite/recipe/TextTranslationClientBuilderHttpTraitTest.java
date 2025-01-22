// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.openrewrite.recipe;


import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import static org.openrewrite.java.Assertions.java;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

/**
 * HttpTraitTest tests interface migration from com.azure.core.client.traits.HttpTrait
 * to io.clientcore.core.models.traits.HttpTrait.
 * Tests simple method renaming with declarative recipe.
 */

public class TextTranslationClientBuilderHttpTraitTest implements RewriteTest {
    /**
     * This method defines recipes used for testing.
     * @param spec stores settings for testing environment.
     */
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResource("/META-INF/rewrite/rewrite.yml",
                "com.azure.openrewrite.migrateToVNext");
    }

    /**
     * Test simple declarative rename of:
     * retryOptions to httpRetryOptions
     * pipeline to httpPipeline
     * addPolicy to addHttpPipelinePolicy
     * and complex rename of clientOptions to httpRedirectOptions
     */
    @Test
    void testMethodsRenamedSuccessful() {
        @Language("java") String before = "import com.azure.ai.translation.text.TextTranslationClient;\n" +
                "import com.azure.ai.translation.text.TextTranslationClientBuilder;\n" +
                "\n" +
                "public class UserClass {\n" +
                "\n" +
                "    TextTranslationClient textTranslationClient = new TextTranslationClientBuilder()\n" +
                "            .pipeline(null)\n" +
                "            .addPolicy(null)\n" +
                "            .retryOptions(null)\n" +
                "            .buildClient();\n" +
                "\n" +
                "}\n";


        @Language("java") String after = "import com.azure.ai.translation.text.TextTranslationClient;\n" +
                "import com.azure.ai.translation.text.TextTranslationClientBuilder;\n" +
                "\n" +
                "public class UserClass {\n" +
                "\n" +
                "    TextTranslationClient textTranslationClient = new TextTranslationClientBuilder()\n" +
                "            .httpPipeline(null)\n" +
                "            .addHttpPipelinePolicy(null)\n" +
                "            .httpRetryOptions(null)\n" +
                "            .buildClient();\n" +
                "\n" +
                "}\n";

        rewriteRun(
                java(before,after)
        );
    }


    @Test
    void testUninitializedParamsAndImportsChanged() {
        @Language("java") String before = "import com.azure.ai.translation.text.TextTranslationClient;\n" +
                "import com.azure.ai.translation.text.TextTranslationClientBuilder;\n" +
                "import com.azure.core.http.HttpClient;\n" +
                "import com.azure.core.http.HttpPipeline;\n" +
                "import com.azure.core.http.policy.HttpLogOptions;\n" +
                "import com.azure.core.http.policy.HttpPipelinePolicy;\n" +
                "import com.azure.core.http.policy.RetryOptions;\n" +
                //"import com.azure.core.util.ClientOptions;" +
                "\n" +
                "public class UserClass{\n" +
                "    \n" +
                "    HttpClient client;\n" +
                "    HttpPipeline pipeline;\n" +
                "    HttpPipelinePolicy httpPipelinePolicy;\n" +
                "    RetryOptions retryOptions;\n" +
                "    HttpLogOptions logOptions;\n" +
                //"    ClientOptions clientOptions;\n" +
                "    \n" +
                "    TextTranslationClient textTranslationClient = new TextTranslationClientBuilder()\n" +
                "            .httpClient(client)\n" +
                "            .pipeline(pipeline)\n" +
                "            .addPolicy(httpPipelinePolicy)\n" +
                "            .retryOptions(retryOptions)\n" +
                "            .httpLogOptions(logOptions)\n" +
                //"            .clientOptions(clientOptions)\n" +
                "            .buildClient();\n" +
                "}";


        @Language("java") String after = "import com.azure.ai.translation.text.TextTranslationClient;\n" +
                "import com.azure.ai.translation.text.TextTranslationClientBuilder;\n" +
                "import io.clientcore.core.http.client.HttpClient;\n" +
                "import io.clientcore.core.http.models.HttpLogOptions;\n" +
                "import io.clientcore.core.http.models.HttpRetryOptions;\n" +
                "import io.clientcore.core.http.pipeline.HttpPipeline;\n" +
                "import io.clientcore.core.http.pipeline.HttpPipelinePolicy;\n" +
                "\n" +
                "public class UserClass {\n" +
                "    \n" +
                "    HttpClient client;\n" +
                "    HttpPipeline pipeline;\n" +
                "    HttpPipelinePolicy httpPipelinePolicy;\n" +
                "    HttpRetryOptions retryOptions;\n" +
                "    HttpLogOptions logOptions;\n" +
                "    \n" +
                "    TextTranslationClient textTranslationClient = new TextTranslationClientBuilder()\n" +
                "            .httpClient(client)\n" +
                "            .httpPipeline(pipeline)\n" +
                "            .addHttpPipelinePolicy(httpPipelinePolicy)\n" +
                "            .httpRetryOptions(retryOptions)\n" +
                "            .httpLogOptions(logOptions)\n" +
                "            .buildClient();\n" +
                "}";

        rewriteRun(
                java(before,after)
        );
    }


    @Test
    void testLikeSampleImplementationChanged() {
        @Language("java") String before = "import com.azure.ai.translation.text.TextTranslationClient;\n" +
                "import com.azure.ai.translation.text.TextTranslationClientBuilder;\n" +
                "import com.azure.core.credential.AzureKeyCredential;\n" +
                "import com.azure.core.http.policy.FixedDelayOptions;\n" +
                "import com.azure.core.http.policy.HttpLogDetailLevel;\n" +
                "import com.azure.core.http.policy.HttpLogOptions;\n" +
                "import com.azure.core.http.policy.RetryOptions;\n" +
                "import java.time.Duration;\n" +
                "\n" +
                "public class UserClass {\n" +
                "\n" +
                "    TextTranslationClient textTranslationClient = new TextTranslationClientBuilder()\n" +
                "            .credential(new AzureKeyCredential(\"<api-key>\"))\n" +
                "            .endpoint(\"<endpoint>\")\n" +
                "            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))\n" +
                "            .retryOptions(new RetryOptions(new FixedDelayOptions(3, Duration.ofMillis(50))))\n" +
                "            .buildClient();" +
                "\n" +
                "}\n";


        @Language("java") String after = "import com.azure.ai.translation.text.TextTranslationClient;\n" +
                "import com.azure.ai.translation.text.TextTranslationClientBuilder;\n" +
                "import io.clientcore.core.credential.AzureKeyCredential;\n" +
                "import io.clientcore.core.http.models.HttpLogOptions;\n" +
                "import io.clientcore.core.http.models.HttpRetryOptions;\n\n" +
                "import java.time.Duration;\n" +
                "\n" +
                "public class UserClass {\n" +
                "\n" +

                "    TextTranslationClient textTranslationClient = new TextTranslationClientBuilder()\n" +
                "            .credential(new AzureKeyCredential(\"<api-key>\"))\n" +
                "            .endpoint(\"<endpoint>\")\n" +
                // Copied from azure-ai-translation-text-v2 TextTranslationSample
                // Not affected at all
                "            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogOptions.HttpLogDetailLevel.BODY_AND_HEADERS))\n" +
                "            .httpRetryOptions(new HttpRetryOptions(3, Duration.ofMillis(50)))\n" +
                "            .buildClient();" +
                "\n" +
                "}\n";

        rewriteRun(
                java(before,after)
        );
    }
}
