// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.tools.codegen.templating;

import io.clientcore.tools.codegen.models.TemplateInput;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    private JavaPoetTemplateProcessor processor;
    private ProcessingEnvironment processingEnv;
    private TemplateInput templateInput;

    @BeforeEach
    public void setUp() {
        processor = new JavaPoetTemplateProcessor();
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
        MethodSpec method = processor.getEndpointMethod();
        assertEquals("getEndpoint", method.name);
        assertEquals(Modifier.PUBLIC, method.modifiers.iterator().next());
        assertEquals(ClassName.get("java.lang", "String"), method.returnType);
    }

    @Test
    public void testGetPipelineMethod() {
        MethodSpec method = processor.getPipelineMethod();
        assertEquals("getPipeline", method.name);
        assertEquals(Modifier.PUBLIC, method.modifiers.iterator().next());
        assertEquals(processor.HTTP_PIPELINE, method.returnType);
    }

    @Test
    public void testGetServiceVersionMethod() {
        MethodSpec method = processor.getServiceVersionMethod();
        assertEquals("getServiceVersion", method.name);
        assertEquals(Modifier.PUBLIC, method.modifiers.iterator().next());
        when(templateInput.getServiceInterfaceShortName()).thenReturn("ExampleClientService");
        assertTrue(method.code.toString().contains("return serviceVersion"));
    }

    @Test
    public void testGetServiceVersionType() {
        assertEquals("com.example.ExampleServiceVersion", processor.getServiceVersionType(PACKAGE_NAME,
            SERVICE_INTERFACE_SHORT_NAME).toString());
    }

    @Test
    public void testServiceImplConstructorGeneration() {
        MethodSpec constructor = processor.getServiceImplConstructor(PACKAGE_NAME,
            SERVICE_INTERFACE_SHORT_NAME);
        assertEquals(Modifier.PUBLIC, constructor.modifiers.iterator().next());
        assertEquals(4, constructor.parameters.size());
        assertTrue(constructor.code.toString().contains("this.defaultPipeline = defaultPipeline"));
        assertTrue(constructor.code.toString().contains("this.serializer = serializer"));
        assertTrue(constructor.code.toString().contains("this.endpoint = endpoint"));
        assertTrue(constructor.code.toString().contains("this.apiVersion = serviceVersion.getVersion()"));
        assertTrue(constructor.code.toString().contains("this.serviceVersion = serviceVersion"));
    }

    @Test
    public void testLoggerFieldGeneration() {
        FieldSpec loggerField = processor.getLoggerField(PACKAGE_NAME, SERVICE_INTERFACE_SHORT_NAME);
        assertEquals(new HashSet<>(Arrays.asList(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)),
            loggerField.modifiers);
        assertEquals(processor.CLIENTLOGGER_NAME, loggerField.type);
        assertEquals("LOGGER", loggerField.name);
        assertTrue(loggerField.initializer.toString().contains("new io.clientcore.core.instrumentation.logging.ClientLogger(com.example.ExampleClientServiceImpl.class)"));
    }
}
