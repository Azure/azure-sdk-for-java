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

@Disabled("Incorrect tests. Need to look into.")
public class BinaryDataTest extends RecipeTestBase {
    /**
     * BinaryDataTest tests the recipe that changes
     * com.azure.core.util.BinaryData to io.clientcore.core.util.binarydata.BinaryData.
     * This recipe also tests to ensure that the TypeReference is correctly changed
     * when used in conjunction with BinaryData.
     */


    /* Test to make sure BinaryData import is changed */
    @Test
    public void testBinaryDataImportChanged() {
        @Language("java") String before = "import com.azure.core.util.BinaryData;";
        before += "\npublic class Testing {";
        before += "\n  public Testing(){";
        before += "\n     BinaryData b = BinaryData.fromObject(null);";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "import io.clientcore.core.util.binarydata.BinaryData;";
        after += "\npublic class Testing {";
        after += "\n  public Testing(){";
        after += "\n     BinaryData b = BinaryData.fromObject(null);";
        after += "\n  }";
        after += "\n}";
        rewriteRun(
                java(before,after)
        );
    }

    /* Test to make sure BinaryData type is changed */
    @Test
    public void testBinaryDataTypeChanged() {
        @Language("java") String before = "";
        before += "public class Testing {\n  public Testing(){\n     com.azure.core.util.BinaryData b = com.azure.core.util.BinaryData.fromObject(null);\n  }\n}";

        @Language("java") String after = "";
        after += "public class Testing {\n  public Testing(){\n      io.clientcore.core.util.binarydata.BinaryData b =  io.clientcore.core.util.binarydata.BinaryData.fromObject(null);\n  }\n}";
        rewriteRun(
                java(before,after)
        );
    }

    /* Test to make sure BinaryData type and import is changed and TypeReference is changed */
    @Test
    @Disabled("This test is failing and is temporarily disabled")
    void testBinaryDataTypeReferenceChange() {
        @Language("java") String before = "";
        before += "\nimport java.lang.reflect.ParameterizedType;";
        before += "\nimport java.lang.reflect.Type;";
        before += "\nimport java.util.List;";
        before += "\nimport com.azure.core.util.serializer.TypeReference;";
        before += "\nimport com.azure.core.util.BinaryData;";
        before += "\npublic class Testing {";
        before += "\n  private static final TypeReference<List<String>> TESTING_TYPE = new TypeReference<List<String>>() {\n  };";
        before += "\n  private static final BinaryData b = BinaryData.fromObject(null);";
        before += "\n  public static void main(String[] args) {";
        before += "\n    System.out.println(b.toObject(TESTING_TYPE));";
        before += "\n  }";
        before += "\n}";


        @Language("java") String after =
                "import io.clientcore.core.util.binarydata.BinaryData;\n\n" +
                "import java.io.IOException;\n" +
                "import java.lang.reflect.ParameterizedType;\n" +
                "import java.lang.reflect.Type;\n" +
                "import java.util.List;\n\n"+
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
                "      }\n";
                after += "  };";
                after += "\n  private static final BinaryData b = BinaryData.fromObject(null);";
                after += "\n  public static void main(String[] args) {";
                after += "\n      try {";
                after += "\n          System.out.println(b.toObject(TESTING_TYPE));";
                after += "\n      } catch (IOException e) {";
                after += "\n          e.printStackTrace();";
                after += "\n      }";
                after += "\n  }\n";
                after += "}\n";

        rewriteRun(
                java(before,after)
        );
    }
}
