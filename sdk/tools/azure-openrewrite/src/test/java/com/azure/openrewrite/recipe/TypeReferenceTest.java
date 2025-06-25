// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.openrewrite.recipe;


import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.openrewrite.java.Assertions.java;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

/**
 * TypeReferenceTest is used to test out the recipe that changes the usage of TypeReference (azure core v1)
 * to ParameterizedType (azure core v2)
 */
@Disabled("Incorrect tests. Need to look into.")
public class TypeReferenceTest extends RecipeTestBase {

    /**
     * This test method is used to make sure that TypeReference is correctly
     * changed to ParameterizedType when using List generic type
     */
    @Test
    void testTypeReferenceVariableDeclarationChangeList() {
        @Language("java") String before = "";
        before += "\nimport java.lang.reflect.ParameterizedType;";
        before += "\nimport java.lang.reflect.Type;";
        before += "\nimport java.util.List;";
        before += "\nimport com.azure.core.util.serializer.TypeReference;";
        before += "\npublic class Testing {";
        before += "\n  private static final TypeReference<List<String>> TESTING_TYPE = new TypeReference<List<String>>() {\n  };";
        before += "\n}";


        @Language("java") String after = "import java.lang.reflect.ParameterizedType;\n" +
                "import java.lang.reflect.Type;\n" +
                "import java.util.List;\n"+
                "public class Testing {\n" +
                "  private static final Type TESTING_TYPE = new ParameterizedType() {\n" +
                "      @Override\n" +
                "      public Type getRawType() {\n" +
                "          return List.class;\n" +
                "      }\n\n" +
                "      @Override\n" +
                "      public Type[] getActualTypeArguments() {\n" +
                "          return new Type[]{String.class};\n" +
                "      }\n\n" +
                "      @Override\n" +
                "      public Type getOwnerType() {\n" +
                "          return null;\n" +
                "      }\n" +
                "  };\n" +
                "}\n";

        rewriteRun(

                java(before,after)
        );
    }
    /**
     * This test method is used to make sure that TypeReference is correctly
     * changed to ParameterizedType when using Map generic type
     */
    @Test
    void testTypeReferenceVariableDeclarationChangeMap() {
        @Language("java") String before = "";
        before += "\nimport java.lang.reflect.ParameterizedType;";
        before += "\nimport java.lang.reflect.Type;";
        before += "\nimport java.util.Map;";
        before += "\nimport com.azure.core.util.serializer.TypeReference;";
        before += "\npublic class Testing {";
        before += "\n  private static final TypeReference<Map<String, Integer>> TESTING_TYPE = new TypeReference<Map<String, Integer>>() {\n  };";
        before += "\n}";


        @Language("java") String after = "import java.lang.reflect.ParameterizedType;\n" +
                "import java.lang.reflect.Type;\n" +
                "import java.util.Map;\n" +
                "public class Testing {\n" +
                "  private static final Type TESTING_TYPE = new ParameterizedType() {\n" +
                "      @Override\n" +
                "      public Type getRawType() {\n" +
                "          return Map.class;\n" +
                "      }\n\n" +
                "      @Override\n" +
                "      public Type[] getActualTypeArguments() {\n" +
                "          return new Type[]{String.class, Integer.class};\n" +
                "      }\n\n" +
                "      @Override\n" +
                "      public Type getOwnerType() {\n" +
                "          return null;\n" +
                "      }\n" +
                "  };\n" +
                "}\n";

        rewriteRun(
                java(before,after)
        );
    }
    /**
     * This test method is used to make sure that TypeReference is correctly
     * changed to ParameterizedType when using non-generic type
     */
    @Test
    void testTypeReferenceVariableDeclarationChangeNonGeneric() {
        @Language("java") String before = "";
        before += "\nimport java.lang.reflect.ParameterizedType;";
        before += "\nimport java.lang.reflect.Type;";
        before += "\nimport com.azure.core.util.serializer.TypeReference;";
        before += "\npublic class Testing {";
        before += "\n  private static final TypeReference<String> TESTING_TYPE = new TypeReference<String>() {\n  };";
        before += "\n}";


        @Language("java") String after = "import java.lang.reflect.ParameterizedType;\n" +
                "import java.lang.reflect.Type;\n" +
                "public class Testing {\n" +
                "  private static final Type TESTING_TYPE = new ParameterizedType() {\n" +
                "      @Override\n" +
                "      public Type getRawType() {\n" +
                "          return String.class;\n" +
                "      }\n\n" +
                "      @Override\n" +
                "      public Type[] getActualTypeArguments() {\n" +
                "          return new Type[]{};\n" +
                "      }\n\n" +
                "      @Override\n" +
                "      public Type getOwnerType() {\n" +
                "          return null;\n" +
                "      }\n" +
                "  };\n" +
                "}\n";

        rewriteRun(
                java(before,after)
        );
    }
}
