package com.azure.openrewrite.recipe;

import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import static org.openrewrite.java.Assertions.java;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

/**
 * Add try-catch recipe wraps all method calls that match the supplied method pattern
 * in a try-catch block with the provided catch code snippet.
 * This test class tests the recipe as part of .
 * @author Annabelle Mittendorf Smith
 */
public class AddTryCatchToMethodCallTest implements RewriteTest {

    /**
     * This method defines recipes used for testing.
     * @param spec stores settings for testing environment.
     */
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResource("/META-INF/rewrite/rewrite.yml",
                "com.azure.openrewrite.migrateToVNext");
    }

    @Test
    void testTextTranslationClientTranslate() {
        @Language("java") String before = "import com.azure.ai.translation.text.TextTranslationClient;\n" +
                "import com.azure.ai.translation.text.TextTranslationClientBuilder;\n" +
                "import com.azure.ai.translation.text.models.InputTextItem;\n" +
                "import com.azure.ai.translation.text.models.TranslatedTextItem;\n" +
                "\n" +
                "import java.util.Arrays;\n" +
                "import java.util.List;\n" +
                "\n" +
                "public class UserClass {\n" +
                "    public void myMethod() {\n" +
                "        TextTranslationClient textTranslationClient = new TextTranslationClientBuilder().buildClient();\n" +
                "        List<InputTextItem> inputTextItems = Arrays.asList(new InputTextItem(\"hello world\"));\n" +
                "        List<TranslatedTextItem> result = textTranslationClient.translate(Arrays.asList(\"es\"), inputTextItems);\n" +
                "    }\n" +
                "}";

        @Language("java") String after = "import com.azure.ai.translation.text.TextTranslationClient;\n" +
                "import com.azure.ai.translation.text.TextTranslationClientBuilder;\n" +
                "import com.azure.ai.translation.text.models.InputTextItem;\n" +
                "import com.azure.ai.translation.text.models.TranslatedTextItem;\n" +
                "\n" +
                "import java.io.IOException;\n" +
                "import java.util.Arrays;\n" +
                "import java.util.List;\n" +
                "\n" +
                "public class UserClass {\n" +
                "    public void myMethod() {\n" +
                "        TextTranslationClient textTranslationClient = new TextTranslationClientBuilder().buildClient();\n" +
                "        List<InputTextItem> inputTextItems = Arrays.asList(new InputTextItem(\"hello world\"));\n" +
                "        List<TranslatedTextItem> result = null;\n" +
                "        try {\n" +
                "            result = textTranslationClient.translate(Arrays.asList(\"es\"), inputTextItems);\n" +
                "        } catch (IOException e) {\n" +
                "            throw new RuntimeException(e);\n" +
                "        }\n" +
                "    }\n" +
                "}";

        rewriteRun(
                java(before,after)
        );
    }

    @Test
    void testTextTranslationClientTranslateWithResponse() {
        @Language("java") String before = "import com.azure.ai.translation.text.TextTranslationClient;\n" +
                "import com.azure.ai.translation.text.TextTranslationClientBuilder;\n" +
                "import com.azure.ai.translation.text.models.InputTextItem;\n" +
                "import com.azure.ai.translation.text.models.TranslatedTextItem;\n" +
                "import com.azure.core.http.rest.RequestOptions;\n" +
                "import com.azure.core.http.rest.Response;\n" +
                "import com.azure.core.util.BinaryData;\n" +
                "import com.azure.core.util.Context;\n" +
                "import com.azure.core.util.serializer.TypeReference;\n" +
                "\n" +
                "import java.util.Arrays;\n" +
                "import java.util.List;\n" +
                "\n" +
                "public class UserClass {\n" +
                "    void myMethod() {\n" +
                "        TextTranslationClient textTranslationClient = new TextTranslationClientBuilder().buildClient();\n" +
                "\n" +
                "        List<InputTextItem> inputTextItems = Arrays.asList(new InputTextItem(\"hello world\"));\n" +
                "        List<String> targetLanguages = Arrays.asList(\"es\");\n" +
                "        BinaryData requestBody = BinaryData.fromObject(inputTextItems);\n" +
                "        RequestOptions requestOptions = new RequestOptions().setContext(Context.NONE);\n" +
                "\n" +
                "        Response<BinaryData> binaryDataResponse = textTranslationClient.translateWithResponse(targetLanguages, requestBody, requestOptions);\n" +
                "        List<TranslatedTextItem> result = binaryDataResponse.getValue().toObject(new TypeReference<List<TranslatedTextItem>>() { });\n" +
               // "        \n" +
                "    }\n" +
                "}";

        @Language("java") String after = "import com.azure.ai.translation.text.TextTranslationClient;\n" +
                "import com.azure.ai.translation.text.TextTranslationClientBuilder;\n" +
                "import com.azure.ai.translation.text.models.InputTextItem;\n" +
                "import com.azure.ai.translation.text.models.TranslatedTextItem;\n" +
                "import io.clientcore.core.http.models.RequestOptions;\n" +
                "import io.clientcore.core.http.models.Response;\n" +
                "import io.clientcore.core.util.Context;\n" +
                "import io.clientcore.core.util.binarydata.BinaryData;\n" +
                "\n" +
                "import java.io.IOException;\n" +
                "import java.lang.reflect.ParameterizedType;\n" +
                "import java.lang.reflect.Type;\n" +
                "import java.util.Arrays;\n" +
                "import java.util.List;\n" +
                "\n" +
                "public class UserClass {\n" +
                "    void myMethod() {\n" +
                "        TextTranslationClient textTranslationClient = new TextTranslationClientBuilder().buildClient();\n" +
                "\n" +
                "        List<InputTextItem> inputTextItems = Arrays.asList(new InputTextItem(\"hello world\"));\n" +
                "        List<String> targetLanguages = Arrays.asList(\"es\");\n" +
                "        BinaryData requestBody = BinaryData.fromObject(inputTextItems);\n" +
                "        RequestOptions requestOptions = new RequestOptions().setContext(Context.none());\n" +
                "\n" +
                "        Response<BinaryData> binaryDataResponse = textTranslationClient.translateWithResponse(targetLanguages, requestBody, requestOptions);\n" +
                "        List<TranslatedTextItem> result = null;\n" +
                "        try {\n" +
                "            result = binaryDataResponse.getValue().toObject(new ParameterizedType() {\n" +
                "                @Override\n" +
                "                public Type getRawType() {\n" +
                "                    return List.class;\n" +
                "                }\n" +
                "\n" +
                "                @Override\n" +
                "                public Type[] getActualTypeArguments() {\n" +
                "                    return new Type[]{TranslatedTextItem.class};\n" +
                "                }\n" +
                "\n" +
                "                @Override\n" +
                "                public Type getOwnerType() {\n" +
                "                    return null;\n" +
                "                } });\n" +
                "        } catch (IOException e) {\n" +
                "            e.printStackTrace();\n" +
                "        }\n" +
                "    }\n" +
                "}";

        rewriteRun(
                spec -> spec.typeValidationOptions(TypeValidation.none()),
                java(before,after)
        );
    }
}
