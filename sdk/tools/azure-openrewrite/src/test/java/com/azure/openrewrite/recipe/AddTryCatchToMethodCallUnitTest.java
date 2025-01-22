package com.azure.openrewrite.recipe;


import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;


import static org.openrewrite.java.Assertions.java;

/**
 * Add try-catch recipe wraps all method calls that match the supplied method pattern
 * in a try-catch block with the provided catch code snippet.
 * This test class tests the recipe alone.
 * @author Annabelle Mittendorf Smith
 */
public class AddTryCatchToMethodCallUnitTest implements RewriteTest {
    /**
     * This method defines recipes used for testing.
     * @param spec stores settings for testing environment.
     */
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipes(new com.azure.recipes.core.v2.AddTryCatchToMethodCallRecipe("UserClass myMethod(..)",
                "catch (IOException e) { e.printStackTrace(); }",
                "java.io.IOException",false),
                new com.azure.recipes.core.v2.AddTryCatchToMethodCallRecipe("CatchAndThrow myMethod(..)",
                        "catch (IOException e) { throw new RuntimeException(e); }",
                        "java.io.IOException",false)
        );
    }

    @Test
    void testAddTryCatchVoidMethod() {
        @Language("java") String before =
                "public class UserClass {\n" +
                "    \n" +
                "    private void myMethod() {\n" +
                "        int a = 1 + 1;\n" +
                "    }\n" +
                "    \n" +
                "    private void anotherMethod(){\n" +
                "        int b = 2 + 2;\n" +
                "        myMethod();\n" +
                "        int c = 3;\n" +
                "    }\n" +
                "}\n";

        @Language("java") String after = "import java.io.IOException;\n" +
                "\n" +
                "public class UserClass {\n" +
                "    \n" +
                "    private void myMethod() {\n" +
                "        int a = 1 + 1;\n" +
                "    }\n" +
                "    \n" +
                "    private void anotherMethod(){\n" +
                "        int b = 2 + 2;\n" +
                "        try {\n" +
                "            myMethod();\n" +
                "        } catch (IOException e) {\n" +
                "            e.printStackTrace();\n" +
                "        }\n" +
                "        int c = 3;\n" +
                "    }\n" +
                "}\n";

        rewriteRun(
                java(before,after)
        );
    }

    @Test
    void testAddTryCatchMethodInVarDeclaration() {
        @Language("java") String before =
                "public class UserClass {\n" +
                "    \n" +
                "    private int myMethod() {\n" +
                "       return 2;\n" +
                "    }\n" +
                "    \n" +
                "    private void anotherMethod(){\n" +
                "        int b = myMethod();\n" +
                "    }\n" +
                "}\n";

        @Language("java") String after = "import java.io.IOException;\n" +
                "\n" +
                "public class UserClass {\n" +
                "    \n" +
                "    private int myMethod() {\n" +
                "       return 2;\n" +
                "    }\n" +
                "    \n" +
                "    private void anotherMethod(){\n" +
                "        int b = null;\n" +
                "        try {\n" +
                "            b = myMethod();\n" +
                "        } catch (IOException e) {\n" +
                "            e.printStackTrace();\n" +
                "        }\n" +
                "    }\n" +
                "}\n";

        rewriteRun(
                java(before,after)
        );
    }

    @Test
    void testAddTryCatchMethodInAssignment() {
        @Language("java") String before =
                "public class UserClass {\n" +
                "    \n" +
                "    private int myMethod() {\n" +
                "       return 2;\n" +
                "    }\n" +
                "    \n" +
                "    private void anotherMethod(){\n" +
                "        int b;\n" +
                "        b = myMethod();\n" +
                "        int a = b;\n" +
                "    }\n" +
                "}\n";

        @Language("java") String after = "import java.io.IOException;\n" +
                "\n" +
                "public class UserClass {\n" +
                "    \n" +
                "    private int myMethod() {\n" +
                "       return 2;\n" +
                "    }\n" +
                "    \n" +
                "    private void anotherMethod(){\n" +
                "        int b;\n" +
                "        try {\n" +
                "            b = myMethod();\n" +
                "        } catch (IOException e) {\n" +
                "            e.printStackTrace();\n" +
                "        }\n" +
                "        int a = b;\n" +
                "    }\n"+
                "}\n";

        rewriteRun(
                java(before,after)
        );
    }

    @Test
    void testAddTryCatchMethodIsFromInstance() {
        @Language("java") String before =
                "public class UserClass {\n" +
                "    public UserClass(){}\n" +
                "    String s = \"Hello\";\n" +
                "    \n" +
                "    public String myMethod(int a, String b) {\n" +
                "       return s;\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "class UserClass2 {" +
                "    \n" +
                "    public void myMethod2() {\n" +
                "        UserClass c = new UserClass();\n" +
                "        String s2 = c.myMethod(3, \"hello\");\n" +
                "    }\n" +
                "}\n";

        @Language("java") String after = "import java.io.IOException;\n" +
                "\n" +
                "public class UserClass {\n" +
                "    public UserClass(){}\n" +
                "    String s = \"Hello\";\n" +
                "    \n" +
                "    public String myMethod(int a, String b) {\n" +
                "       return s;\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "class UserClass2 {" +
                "    \n" +
                "    public void myMethod2() {\n" +
                "        UserClass c = new UserClass();\n" +
                "        String s2 = null;\n" +
                "        try {\n" +
                "            s2 = c.myMethod(3, \"hello\");\n" +
                "        } catch (IOException e) {\n" +
                "            e.printStackTrace();\n" +
                "        }\n" +
                "    }\n" +
                "}\n";

        rewriteRun(
                java(before,after)
        );
    }

    @Test
    void testAddTryCatchMethodIsInNestedCall() {
        @Language("java") String before =
                "public class CatchAndThrow {\n" +
                "    public CatchAndThrow(){}\n" +
                "    String s = \"Hello\";\n" +
                "    \n" +
                "    public String myMethod() {\n" +
                "       return s;\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "class UserClass2 {\n" +
                "    CatchAndThrow c = new CatchAndThrow();\n" +
                "}\n" +
                "class UserClass3 {\n" +
                "    UserClass2 c2 = new UserClass2();\n" +
                "    public void myMethod3() {\n" +
                "        String s = c2.c.myMethod();\n" +
                "    }\n" +
                "}";

        @Language("java") String after = "import java.io.IOException;\n" +
                "\n" +
                "public class CatchAndThrow {\n" +
                "    public CatchAndThrow(){}\n" +
                "    String s = \"Hello\";\n" +
                "    \n" +
                "    public String myMethod() {\n" +
                "       return s;\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "class UserClass2 {\n" +
                "    CatchAndThrow c = new CatchAndThrow();\n" +
                "}\n" +
                "class UserClass3 {\n" +
                "    UserClass2 c2 = new UserClass2();\n" +
                "    public void myMethod3() {\n" +
                "        String s = null;\n" +
                "        try {\n" +
                "            s = c2.c.myMethod();\n" +
                "        } catch (IOException e) {\n" +
                "            throw new RuntimeException(e);\n" +
                //"            e.printStackTrace();\n" +
                "        }\n" +
                "    }\n" +
                "}";

        rewriteRun(
                java(before,after)
        );
    }
    @Test
    void testAddTryCatchThrows() {
        @Language("java") String before =
                "public class CatchAndThrow {\n" +
                        "    \n" +
                        "    private void myMethod() {\n" +
                        "        int a = 1 + 1;\n" +
                        "    }\n" +
                        "    \n" +
                        "    private void anotherMethod(){\n" +
                        "        int b = 2 + 2;\n" +
                        "        myMethod();\n" +
                        "        int c = 3;\n" +
                        "    }\n" +
                        "}\n";

        @Language("java") String after = "import java.io.IOException;\n" +
                "\n" +
                "public class CatchAndThrow {\n" +
                "    \n" +
                "    private void myMethod() {\n" +
                "        int a = 1 + 1;\n" +
                "    }\n" +
                "    \n" +
                "    private void anotherMethod(){\n" +
                "        int b = 2 + 2;\n" +
                "        try {\n" +
                "            myMethod();\n" +
                "        } catch (IOException e) {\n" +
                "            throw new RuntimeException(e);\n" +
                "        }\n" +
                "        int c = 3;\n" +
                "    }\n" +
                "}\n";

        rewriteRun(
                java(before,after)
        );
    }
}
