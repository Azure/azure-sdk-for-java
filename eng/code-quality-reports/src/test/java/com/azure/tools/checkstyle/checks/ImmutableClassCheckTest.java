// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.Checker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static com.azure.tools.checkstyle.checks.ImmutableClassCheck.PUBLIC_FIELD_ERROR_TEMPLATE;
import static com.azure.tools.checkstyle.checks.ImmutableClassCheck.SETTER_METHOD_ERROR_TEMPLATE;

/**
 * Tests {@link ImmutableClassCheck}.
 */
public class ImmutableClassCheckTest extends AbstractModuleTestSupport {
    private Checker checker;

    @Before
    public void prepare() throws Exception {
        checker = createChecker(createModuleConfig(ImmutableClassCheck.class));
    }

    @After
    public void cleanup() {
        checker.destroy();
    }

    @Override
    protected String getPackageLocation() {
        return "com/azure/tools/checkstyle/checks/ImmutableClassCheck";
    }

    @Test
    public void emptyClass() throws Exception {
        File file = TestUtils.createCheckFile("emptyClass",
            "package com.azure;",
            "@Immutable",
            "public class EmptyClass {",
            "}");

        verify(checker, new File[]{file}, file.getAbsolutePath());
    }

    @Test
    public void classWithAllFinalFields() throws Exception {
        File file = TestUtils.createCheckFile("emptyClass",
            "package com.azure;",
            "@Immutable",
            "public class AllFieldsFinal {",
            "    private final int intField;",
            "    private final String stringField;",
            "}");

        verify(checker, new File[]{file}, file.getAbsolutePath());
    }

    @Test
    public void classWithNonFinalNonPublicFields() throws Exception {
        File file = TestUtils.createCheckFile("emptyClass",
            "package com.azure;",
            "@Immutable",
            "public class NonFinalNonPublic {",
            "    private int intField;",
            "    String stringField;",
            "}");

        verify(checker, new File[]{file}, file.getAbsolutePath());
    }

    @Test
    public void classWithFinalPublicFields() throws Exception {
        File file = TestUtils.createCheckFile("emptyClass",
            "package com.azure;",
            "@Immutable",
            "public class NonFinalNonPublic {",
            "    public final int intField;",
            "    protected final String stringField;",
            "}");

        verify(checker, new File[]{file}, file.getAbsolutePath());
    }

    @Test
    public void classWithNonFinalPublicFields() throws Exception {
        File file = TestUtils.createCheckFile("emptyClass",
            "package com.azure;",
            "@Immutable",
            "public class NonFinalNonPublic {",
            "    public int intField;",
            "    protected String stringField;",
            "}");

        String[] expected = {
            expectedErrorMessage(4, 5, String.format(PUBLIC_FIELD_ERROR_TEMPLATE, "intField")),
            expectedErrorMessage(5, 5, String.format(PUBLIC_FIELD_ERROR_TEMPLATE, "stringField"))
        };
        verify(checker, new File[]{file}, file.getAbsolutePath(), expected);
    }

    @Test
    public void publicInnerClassWithNonFinalPublicFields() throws Exception {
        File file = TestUtils.createCheckFile("emptyClass",
            "package com.azure;",
            "@Immutable",
            "public class NonFinalNonPublic {",
            "    @Immutable",
            "    public static final class InnerClass {",
            "        public int intField;",
            "        protected String stringField;",
            "    }",
            "}");

        String[] expected = {
            expectedErrorMessage(6, 9, String.format(PUBLIC_FIELD_ERROR_TEMPLATE, "intField")),
            expectedErrorMessage(7, 9, String.format(PUBLIC_FIELD_ERROR_TEMPLATE, "stringField"))
        };
        verify(checker, new File[]{file}, file.getAbsolutePath(), expected);
    }

    @Test
    public void nonPublicInnerClassWithNonFinalPublicFields() throws Exception {
        File file = TestUtils.createCheckFile("emptyClass",
            "package com.azure;",
            "@Immutable",
            "public class NonFinalNonPublic {",
            "    @Immutable",
            "    static final class InnerClass {",
            "        public int intField;",
            "        protected String stringField;",
            "    }",
            "}");

        verify(checker, new File[]{file}, file.getAbsolutePath());
    }

    @Test
    public void classWithOnlyGetterMethods() throws Exception {
        File file = TestUtils.createCheckFile("emptyClass",
            "package com.azure;",
            "@Immutable",
            "public class NonFinalNonPublic {",
            "    public String getString() {",
            "    }",
            "    public int getInt() {",
            "    }",
            "}");

        verify(checker, new File[]{file}, file.getAbsolutePath());
    }

    @Test
    public void classWithPublicSetterMethods() throws Exception {
        File file = TestUtils.createCheckFile("emptyClass",
            "package com.azure;",
            "@Immutable",
            "public class NonFinalNonPublic {",
            "    public String setString(String string) {",
            "    }",
            "    public int setInt(int integer) {",
            "    }",
            "}");

        String[] expected = {
            expectedErrorMessage(4, 5, String.format(SETTER_METHOD_ERROR_TEMPLATE, "setString")),
            expectedErrorMessage(6, 5, String.format(SETTER_METHOD_ERROR_TEMPLATE, "setInt"))
        };
        verify(checker, new File[]{file}, file.getAbsolutePath(), expected);
    }

    @Test
    public void publicInnerClassWithPublicSetterMethods() throws Exception {
        File file = TestUtils.createCheckFile("emptyClass",
            "package com.azure;",
            "@Immutable",
            "public class NonFinalNonPublic {",
            "    @Immutable",
            "    public static final class InnerClass {",
            "        public String setString(String string) {",
            "        }",
            "        public int setInt(int integer) {",
            "        }",
            "    }",
            "}");

        String[] expected = {
            expectedErrorMessage(6, 9, String.format(SETTER_METHOD_ERROR_TEMPLATE, "setString")),
            expectedErrorMessage(8, 9, String.format(SETTER_METHOD_ERROR_TEMPLATE, "setInt"))
        };
        verify(checker, new File[]{file}, file.getAbsolutePath(), expected);
    }

    @Test
    public void nonPublicInnerClassWithPublicSetterMethods() throws Exception {
        File file = TestUtils.createCheckFile("emptyClass",
            "package com.azure;",
            "@Immutable",
            "public class NonFinalNonPublic {",
            "    @Immutable",
            "    static final class InnerClass {",
            "        public String setString(String string) {",
            "        }",
            "        public int setInt(int integer) {",
            "        }",
            "    }",
            "}");

        verify(checker, new File[]{file}, file.getAbsolutePath());
    }

    @Test
    public void classWithNonPublicSetterMethods() throws Exception {
        File file = TestUtils.createCheckFile("emptyClass",
            "package com.azure;",
            "@Immutable",
            "public class NonFinalNonPublic {",
            "    String setString(String string) {",
            "    }",
            "    private int setInt(int integer) {",
            "    }",
            "}");

        verify(checker, new File[]{file}, file.getAbsolutePath());
    }

    @Test
    public void classWithNonSetterMethodStartingWithSet() throws Exception {
        File file = TestUtils.createCheckFile("emptyClass",
            "package com.azure;",
            "@Immutable",
            "public class NonFinalNonPublic {",
            "    public void settleArgument(String argument) {",
            "    }",
            "}");

        verify(checker, new File[]{file}, file.getAbsolutePath());
    }

    private String expectedErrorMessage(int line, int column, String error) {
        return String.format("%d:%d: %s", line, column, error);
    }
}
