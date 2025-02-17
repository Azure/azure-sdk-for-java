// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.templating;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import io.clientcore.annotation.processor.mocks.MockFiler;
import io.clientcore.annotation.processor.mocks.MockJavaFileObject;
import io.clientcore.annotation.processor.mocks.MockProcessingEnvironment;
import io.clientcore.annotation.processor.models.TemplateInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.annotation.processing.Filer;
import javax.tools.JavaFileObject;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * Tests for builder/helper methods generated in ServiceClientImpl class.
 */
public class HttpPipelineBuilderMethodTest {

    private static final String PACKAGE_NAME = "com.example";
    private static final String SERVICE_INTERFACE_SHORT_NAME = getExampleClientServiceImpl();
    private final JavaParserTemplateProcessor processor = new JavaParserTemplateProcessor();
    private TemplateInput templateInput;

    @BeforeEach
    public void setUp() {
        templateInput = new TemplateInput();
        templateInput.setPackageName(PACKAGE_NAME);
        templateInput.setServiceInterfaceImplShortName(SERVICE_INTERFACE_SHORT_NAME);
        templateInput.setServiceInterfaceShortName("ExampleClientService");
        templateInput.setHttpRequestContexts(Collections.emptyList());
    }

    private static String getExampleClientServiceImpl() {
        return "ExampleClientServiceImpl";
    }

    @Test
    public void testProcess() {
        JavaFileObject filerSourceFile = new MockJavaFileObject();
        Filer filer = new MockFiler(filerSourceFile);
        MockProcessingEnvironment processingEnv = new MockProcessingEnvironment(filer);

        processor.process(templateInput, processingEnv);

        // Verify that the JavaFile.writeTo was called
        assertEquals(1, processingEnv.getGetFilerCount(),
            "Expected getFiler to be called once, but was called " + processingEnv.getGetFilerCount() + " times");
    }

    @Test
    public void testGetEndpointMethod() {
        MethodDeclaration method = new MethodDeclaration();
        processor.configureEndpointMethod(method);
        assertEquals("getEndpoint", method.getNameAsString());
        assertEquals(Modifier.publicModifier(), method.getModifiers().iterator().next());
        assertEquals("String", method.getTypeAsString());
    }

    @Test
    public void testGetPipelineMethod() {
        MethodDeclaration method = new MethodDeclaration();
        processor.configurePipelineMethod(method);
        assertEquals("getPipeline", method.getNameAsString());
        assertEquals(Modifier.publicModifier(), method.getModifiers().iterator().next());
        assertEquals("HttpPipeline", method.getTypeAsString());
    }

    @Test
    public void testGetServiceVersionMethod() {
        MethodDeclaration method = new MethodDeclaration();
        processor.configureServiceVersionMethod(method, "ExampleClientService");
        assertEquals("getServiceVersion", method.getNameAsString());
        assertEquals(Modifier.publicModifier(), method.getModifiers().iterator().next());
        templateInput.setServiceInterfaceShortName("ExampleClientService");
        assertTrue(method.getBody().map(BlockStmt::toString).get().contains("return serviceVersion"));
    }

    @Test
    public void testGetServiceVersionType() {
        assertEquals("com.example.ExampleServiceVersion",
            JavaParserTemplateProcessor.getServiceVersionType(PACKAGE_NAME, SERVICE_INTERFACE_SHORT_NAME));
    }

    @Test
    public void testLoggerFieldGeneration() {
        FieldDeclaration loggerField = new FieldDeclaration();
        processor.configureLoggerField(loggerField, SERVICE_INTERFACE_SHORT_NAME);
        assertEquals(
            new HashSet<>(
                Arrays.asList(Modifier.privateModifier(), Modifier.staticModifier(), Modifier.finalModifier())),
            new HashSet<>(loggerField.getModifiers()));
        assertEquals("ClientLogger", loggerField.getVariable(0).getTypeAsString());
        assertEquals("LOGGER", loggerField.getVariable(0).getNameAsString());

        String expected = "new ClientLogger(ExampleClientServiceImpl.class)";
        String actual = loggerField.getVariable(0).getInitializer().get().toString();
        assertTrue(actual.contains(expected), "Expected to contain: " + expected + " Actual: " + actual);
    }
}
