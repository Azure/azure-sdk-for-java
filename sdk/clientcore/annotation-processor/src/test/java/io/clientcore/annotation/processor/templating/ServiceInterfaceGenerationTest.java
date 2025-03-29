// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.templating;

import io.clientcore.annotation.processor.mocks.MockFiler;
import io.clientcore.annotation.processor.mocks.MockJavaFileObject;
import io.clientcore.annotation.processor.mocks.MockProcessingEnvironment;
import io.clientcore.annotation.processor.mocks.MockTemplateInput;
import io.clientcore.annotation.processor.models.TemplateInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServiceInterfaceGenerationTest {

    private JavaParserTemplateProcessor processor;
    private TemplateInput templateInput = new MockTemplateInput();
    private ProcessingEnvironment processingEnv;

    @BeforeEach
    public void setUp() {
        processor = new JavaParserTemplateProcessor();
        JavaFileObject filerSourceFile = new MockJavaFileObject();
        Filer filer = new MockFiler(filerSourceFile);
        processingEnv = new MockProcessingEnvironment(filer, null, null);
    }

    @Test
    public void testProcessPackageName() {
        processor.process(templateInput, processingEnv);
        assertEquals("com.azure.v2.dummy.implementation",
            processor.getCompilationUnit().getPackageDeclaration().get().getNameAsString());
    }

    @Test
    public void testProcessServiceInterfaceFQN() {
        processor.process(templateInput, processingEnv);
        assertEquals(templateInput.getServiceInterfaceFQN(),
            processor.getCompilationUnit().getPackageDeclaration().get().getNameAsString() + ".DummyClientImpl."
                + templateInput.getServiceInterfaceShortName());
    }

    @Test
    public void testAddImports() {
        processor.addImports(templateInput);
        assertEquals(0, processor.getCompilationUnit().getImports().size());
    }

    @Test
    public void testAddOrphanComments() {
        processor.addCopyrightComments();
        assertEquals(2, processor.getCompilationUnit().getOrphanComments().size());
    }

    @Test
    public void testSetPackageDeclaration() {
        processor.setPackageDeclaration("com.example.service");
        assertEquals("com.example.service",
            processor.getCompilationUnit().getPackageDeclaration().get().getNameAsString());
    }

    @Test
    public void testCreateClass() {
        processor.createClass("ServiceImpl", "Service", templateInput, processingEnv);
        assertEquals("ServiceImpl", processor.getCompilationUnit().getTypes().get(0).getNameAsString());
    }
}
