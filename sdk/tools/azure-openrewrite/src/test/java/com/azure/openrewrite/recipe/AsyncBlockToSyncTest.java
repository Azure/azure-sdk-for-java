package com.azure.openrewrite.recipe;

import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.TypeValidation;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import static org.openrewrite.java.Assertions.java;

public class AsyncBlockToSyncTest extends RecipeTestBase {

    @Override
    public void defaults(RecipeSpec spec) {
        List<String> asyncBlock = new ArrayList<String>();
        asyncBlock.add("com.azure.core.http.HttpClient send(..)");
        asyncBlock.add("reactor.core.publisher.* block()");
        Collections.reverse(asyncBlock);

        List<String> syncBlock = new ArrayList<String>();
        syncBlock.add("com.azure.core.HttpClient sendSync(..)");
        spec.recipe(new com.azure.openrewrite.util.AsyncBlockToSyncRecipe(asyncBlock, syncBlock, true))
            .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void testAsyncBlockToSync() {
        @Language("java") String before = """
                import com.azure.core.*;
                import com.azure.core.http.*;
                import reactor.core.publisher.*;

                public class Test {
                    public void test() {

                        HttpClient client = HttpClient.createDefault();
                        HttpRequest request = new HttpRequest(HttpMethod.GET, "http://localhost:8080");
                        HttpResponse response = client.send(request).block();
                    }
                }""";

        @Language("java") String after = """
                import com.azure.core.*;
                import com.azure.core.http.*;

                public class Test {
                    public void test() {

                        HttpClient client = HttpClient.createDefault();
                        HttpRequest request = new HttpRequest(HttpMethod.GET, "http://localhost:8080");
                        HttpResponse response = client.sendSync(request);
                    }
                }""";


        rewriteRun(
            spec -> spec.parser(JavaParser.fromJavaVersion().classpath("azure-core", "reactor-core", "reactive-streams", "core")),
            java(before));
    }

}
