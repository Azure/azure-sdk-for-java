// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.templating;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import io.clientcore.annotation.processor.models.TemplateInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
 * Tests for builder/helper methods generated in ServiceClientImpl class.
 */
public class HttpPipelineBuilderMethodTest {

    private static final String PACKAGE_NAME = "com.example";
    private static final String SERVICE_INTERFACE_SHORT_NAME = getExampleClientServiceImpl();
    private JavaParserTemplateProcessor processor;
    private ProcessingEnvironment processingEnv;
    private TemplateInput templateInput;

    @BeforeEach
    public void setUp() {
        processor = new JavaParserTemplateProcessor();
        processingEnv = mock(ProcessingEnvironment.class);
        templateInput = mock(TemplateInput.class);

        when(templateInput.getPackageName()).thenReturn(PACKAGE_NAME);
        when(templateInput.getServiceInterfaceImplShortName()).thenReturn(SERVICE_INTERFACE_SHORT_NAME);
        when(templateInput.getServiceInterfaceShortName()).thenReturn("ExampleClientService");
        when(templateInput.getHttpRequestContexts()).thenReturn(Collections.emptyList());
    }

    private static String getExampleClientServiceImpl() {
        return "ExampleClientServiceImpl";
    }

    @Test
    public void testProcess() throws IOException {
        Filer filer = mock(Filer.class);
        JavaFileObject filerSourceFile = mock(JavaFileObject.class);
        when(processingEnv.getFiler()).thenReturn(filer);
        when(filer.createSourceFile(anyString())).thenReturn(filerSourceFile);
        when(filerSourceFile.openWriter()).thenReturn(mock(Writer.class));

        processor.process(templateInput, processingEnv);

        // Verify that the JavaFile.writeTo was called
        verify(processingEnv, times(1)).getFiler();
    }

    @Test
    public void testGetEndpointMethod() {
        MethodDeclaration method = processor.getEndpointMethod();
        assertEquals("getEndpoint", method.getNameAsString());
        assertEquals(Modifier.publicModifier(), method.getModifiers().iterator().next());
        assertEquals("String", method.getTypeAsString());
    }

    @Test
    public void testGetPipelineMethod() {
        MethodDeclaration method = processor.getPipelineMethod();
        assertEquals("getPipeline", method.getNameAsString());
        assertEquals(Modifier.publicModifier(), method.getModifiers().iterator().next());
        assertEquals("HttpPipeline", method.getTypeAsString());
    }

    @Test
    public void testGetServiceVersionMethod() {
        MethodDeclaration method = processor.getServiceVersionMethod("ExampleClientService");
        assertEquals("getServiceVersion", method.getNameAsString());
        assertEquals(Modifier.publicModifier(), method.getModifiers().iterator().next());
        when(templateInput.getServiceInterfaceShortName()).thenReturn("ExampleClientService");
        assertTrue(method.getBody().get().toString().contains("return serviceVersion"));
    }

    @Test
    public void testGetServiceVersionType() {
        assertEquals("com.example.ExampleServiceVersion",
            JavaParserTemplateProcessor.getServiceVersionType(PACKAGE_NAME, SERVICE_INTERFACE_SHORT_NAME));
    }

    @Test
    public void testLoggerFieldGeneration() {
        FieldDeclaration loggerField = processor.getLoggerField(SERVICE_INTERFACE_SHORT_NAME);
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
