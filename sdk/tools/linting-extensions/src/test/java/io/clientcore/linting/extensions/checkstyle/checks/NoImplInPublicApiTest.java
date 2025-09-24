// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.linting.extensions.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.Checker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static io.clientcore.linting.extensions.checkstyle.checks.NoImplInPublicAPI.EXTENDS_TYPE_ERROR;
import static io.clientcore.linting.extensions.checkstyle.checks.NoImplInPublicAPI.IMPLEMENTS_TYPE_ERROR;
import static io.clientcore.linting.extensions.checkstyle.checks.NoImplInPublicAPI.RETURN_TYPE_ERROR;
import static io.clientcore.linting.extensions.checkstyle.checks.NoImplInPublicAPI.TYPE_PARAM_TYPE_ERROR;

public class NoImplInPublicApiTest extends AbstractModuleTestSupport {
    private Checker checker;

    @BeforeEach
    public void prepare() throws Exception {
        checker = createChecker(createModuleConfig(NoImplInPublicAPI.class));
    }

    @AfterEach
    public void cleanup() {
        checker.destroy();
    }

    @Override
    protected String getPackageLocation() {
        return "io/clientcore/linting/extensions/checkstyle/checks/NoImplInPublicApiCheck";
    }

    @Test
    public void staticInitializerDoesNotCountAsApi() throws Exception {
        String[] expected = new String[0];
        verify(checker, getPath("StaticInitializer.java"), expected);
    }

    @Test
    public void gettersNeedToCheckForImplementation() throws Exception {
        String[] expected = {
            "42:12: " + String.format(RETURN_TYPE_ERROR, "AnImplementationClass"),
            "46:15: " + String.format(RETURN_TYPE_ERROR, "AnImplementationClass"),
            "59:36: " + String.format(RETURN_TYPE_ERROR, "com.azure.implementation.AnImplementationClass"),
            "63:39: " + String.format(RETURN_TYPE_ERROR, "com.azure.implementation.AnImplementationClass") };

        verify(checker, getPath("Getters.java"), expected);
    }

    @Test
    public void publicClassImplementsPublicApi() throws Exception {
        File file = TestUtils.createCheckFile("publicClassImplementsPublicApi", "package com.azure;",
            "import com.azure.PublicClass;", "public class MyClass implements PublicClass {", "}");

        verify(checker, new File[] { file }, file.getAbsolutePath());
    }

    @Test
    public void publicClassImplementsImplementationApi() throws Exception {
        File file = TestUtils.createCheckFile("publicClassImplementsImplementationApi", "package com.azure;",
            "import com.azure.implementation.ImplementationClass;",
            "public class MyClass implements ImplementationClass {", // line 3, column 33
            "}");

        String[] expected = { "3:33: " + String.format(IMPLEMENTS_TYPE_ERROR, "ImplementationClass") };
        verify(checker, new File[] { file }, file.getAbsolutePath(), expected);
    }

    @Test
    public void nonPublicClassImplementsImplementationApi() throws Exception {
        File file = TestUtils.createCheckFile("nonPublicClassImplementsImplementationApi", "package com.azure;",
            "import com.azure.implementation.ImplementationClass;", "class MyClass implements ImplementationClass {",
            "}");

        verify(checker, new File[] { file }, file.getAbsolutePath());
    }

    @Test
    public void publicClassExtendsPublicApi() throws Exception {
        File file = TestUtils.createCheckFile("publicClassExtendsPublicApi", "package com.azure;",
            "import com.azure.PublicClass;", "public class MyClass extends PublicClass {", "}");

        verify(checker, new File[] { file }, file.getAbsolutePath());
    }

    @Test
    public void publicClassExtendsImplementationApi() throws Exception {
        File file = TestUtils.createCheckFile("publicClassExtendsImplementationApi", "package com.azure;",
            "import com.azure.implementation.ImplementationClass;",
            "public class MyClass extends ImplementationClass {", // line 3, column 30
            "}");

        String[] expected = { "3:30: " + String.format(EXTENDS_TYPE_ERROR, "ImplementationClass") };
        verify(checker, new File[] { file }, file.getAbsolutePath(), expected);
    }

    @Test
    public void nonPublicClassExtendsImplementationApi() throws Exception {
        File file = TestUtils.createCheckFile("nonPublicClassExtendsImplementationApi", "package com.azure;",
            "import com.azure.implementation.ImplementationClass;", "class MyClass extends ImplementationClass {", "}");

        verify(checker, new File[] { file }, file.getAbsolutePath());
    }

    @Test
    public void publicInterfaceImplementsPublicApi() throws Exception {
        File file = TestUtils.createCheckFile("publicInterfaceImplementsPublicApi", "package com.azure;",
            "import com.azure.PublicClass;", "public interface MyInterface extends PublicClass {", "}");

        verify(checker, new File[] { file }, file.getAbsolutePath());
    }

    @Test
    public void publicInterfaceImplementsImplementationApi() throws Exception {
        File file = TestUtils.createCheckFile("publicInterfaceImplementsImplementationApi", "package com.azure;",
            "import com.azure.implementation.ImplementationClass;",
            "public interface MyInterface extends ImplementationClass {", // line 3, column 38
            "}");

        String[] expected = { "3:38: " + String.format(EXTENDS_TYPE_ERROR, "ImplementationClass") };
        verify(checker, new File[] { file }, file.getAbsolutePath(), expected);
    }

    @Test
    public void nonPublicInterfaceImplementsImplementationApi() throws Exception {
        File file = TestUtils.createCheckFile("nonPublicInterfaceImplementsImplementationApi", "package com.azure;",
            "import com.azure.implementation.ImplementationClass;",
            "interface MyInterface extends ImplementationClass {", "}");

        verify(checker, new File[] { file }, file.getAbsolutePath());
    }

    @Test
    public void publicEnumImplementsPublicApi() throws Exception {
        File file = TestUtils.createCheckFile("publicEnumImplementsPublicApi", "package com.azure;",
            "import com.azure.PublicClass;", "public enum MyEnum implements PublicClass {", "}");

        verify(checker, new File[] { file }, file.getAbsolutePath());
    }

    @Test
    public void publicEnumImplementsImplementationApi() throws Exception {
        File file = TestUtils.createCheckFile("publicEnumImplementsImplementationApi", "package com.azure;",
            "import com.azure.implementation.ImplementationClass;",
            "public enum MyEnum implements ImplementationClass {", // line 3, column 31
            "}");

        String[] expected = { "3:31: " + String.format(IMPLEMENTS_TYPE_ERROR, "ImplementationClass") };
        verify(checker, new File[] { file }, file.getAbsolutePath(), expected);
    }

    @Test
    public void nonPublicEnumImplementsImplementationApi() throws Exception {
        File file = TestUtils.createCheckFile("nonPublicEnumImplementsImplementationApi", "package com.azure;",
            "import com.azure.implementation.ImplementationClass;", "enum MyEnum implements ImplementationClass {",
            "}");

        verify(checker, new File[] { file }, file.getAbsolutePath());
    }

    @Test
    public void publicClassUsesPublicApiTypeParam() throws Exception {
        File file = TestUtils.createCheckFile("publicClassUsesPublicApiTypeParam", "package com.azure;",
            "import com.azure.PublicClass;", "public class MyClass<PublicClass> {", "}");

        verify(checker, new File[] { file }, file.getAbsolutePath());
    }

    @Test
    public void publicClassUsesImplementationApiTypeParam() throws Exception {
        File file = TestUtils.createCheckFile("publicClassUsesImplementationApiTypeParam", "package com.azure;",
            "import com.azure.implementation.ImplementationClass;", "public class MyClass<ImplementationClass> {", // line 3, column 22
            "}");

        String[] expected = { "3:22: " + String.format(TYPE_PARAM_TYPE_ERROR, "ImplementationClass") };
        verify(checker, new File[] { file }, file.getAbsolutePath(), expected);
    }

    @Test
    public void nonPublicClassUsesImplementationApiTypeParam() throws Exception {
        File file = TestUtils.createCheckFile("nonPublicClassUsesImplementationApiTypeParam", "package com.azure;",
            "import com.azure.implementation.ImplementationClass;", "class MyClass<ImplementationClass> {", "}");

        verify(checker, new File[] { file }, file.getAbsolutePath());
    }

    @Test
    public void publicClassUsesPublicApiUpperBoundTypeParam() throws Exception {
        File file = TestUtils.createCheckFile("publicClassUsesPublicApiUpperBoundTypeParam", "package com.azure;",
            "import com.azure.PublicClass;", "public class MyClass<A extends PublicClass> {", "}");

        verify(checker, new File[] { file }, file.getAbsolutePath());
    }

    @Test
    public void publicClassUsesImplementationApiUpperBoundTypeParam() throws Exception {
        File file = TestUtils.createCheckFile("publicClassUsesImplementationApiUpperBoundTypeParam",
            "package com.azure;", "import com.azure.implementation.ImplementationClass;",
            "public class MyClass<A extends ImplementationClass> {", // line 3, column 22
            "}");

        String[] expected = { "3:22: " + String.format(TYPE_PARAM_TYPE_ERROR, "ImplementationClass") };
        verify(checker, new File[] { file }, file.getAbsolutePath(), expected);
    }

    @Test
    public void nonPublicClassUsesImplementationApiUpperBoundTypeParam() throws Exception {
        File file = TestUtils.createCheckFile("nonPublicClassUsesImplementationApiUpperBoundTypeParam",
            "package com.azure;", "import com.azure.implementation.ImplementationClass;",
            "class MyClass<A extends ImplementationClass> {", "}");

        verify(checker, new File[] { file }, file.getAbsolutePath());
    }

    @Test
    public void publicInterfaceUsesPublicApiTypeParam() throws Exception {
        File file = TestUtils.createCheckFile("publicInterfaceUsesPublicApiTypeParam", "package com.azure;",
            "import com.azure.PublicClass;", "public interface MyInterface<PublicClass> {", "}");

        verify(checker, new File[] { file }, file.getAbsolutePath());
    }

    @Test
    public void publicInterfaceUsesImplementationApiTypeParam() throws Exception {
        File file = TestUtils.createCheckFile("publicInterfaceUsesImplementationApiTypeParam", "package com.azure;",
            "import com.azure.implementation.ImplementationClass;",
            "public interface MyInterface<ImplementationClass> {", // line 3, column 30
            "}");

        String[] expected = { "3:30: " + String.format(TYPE_PARAM_TYPE_ERROR, "ImplementationClass") };
        verify(checker, new File[] { file }, file.getAbsolutePath(), expected);
    }

    @Test
    public void nonPublicInterfaceUsesImplementationApiTypeParam() throws Exception {
        File file = TestUtils.createCheckFile("nonPublicInterfaceUsesImplementationApiTypeParam", "package com.azure;",
            "import com.azure.implementation.ImplementationClass;", "interface MyInterface<ImplementationClass> {",
            "}");

        verify(checker, new File[] { file }, file.getAbsolutePath());
    }

    @Test
    public void publicInterfaceUsesPublicApiUpperBoundTypeParam() throws Exception {
        File file = TestUtils.createCheckFile("publicInterfaceUsesPublicApiUpperBoundTypeParam", "package com.azure;",
            "import com.azure.PublicClass;", "public interface MyInterface<A extends PublicClass> {", "}");

        verify(checker, new File[] { file }, file.getAbsolutePath());
    }

    @Test
    public void publicInterfaceUsesImplementationApiUpperBoundTypeParam() throws Exception {
        File file = TestUtils.createCheckFile("publicInterfaceUsesImplementationApiUpperBoundTypeParam",
            "package com.azure;", "import com.azure.implementation.ImplementationClass;",
            "public interface MyInterface<A extends ImplementationClass> {", // line 3, column 30
            "}");

        String[] expected = { "3:30: " + String.format(TYPE_PARAM_TYPE_ERROR, "ImplementationClass") };
        verify(checker, new File[] { file }, file.getAbsolutePath(), expected);
    }

    @Test
    public void nonPublicInterfaceUsesImplementationApiUpperBoundTypeParam() throws Exception {
        File file = TestUtils.createCheckFile("nonPublicInterfaceUsesImplementationApiUpperBoundTypeParam",
            "package com.azure;", "import com.azure.implementation.ImplementationClass;",
            "interface MyInterface<A extends ImplementationClass> {", "}");

        verify(checker, new File[] { file }, file.getAbsolutePath());
    }

    @Test
    public void implementationPackageIsANoOp() throws Exception {
        File file = TestUtils.createCheckFile("implementationPackageIsANoOp", "package com.azure.implementation;",
            "import com.azure.implementation.ImplementationClass;",
            "public class MyClass extends ImplementationClass {", // normally would be line 3, column 30
            "}");

        verify(checker, new File[] { file }, file.getAbsolutePath());
    }
}
