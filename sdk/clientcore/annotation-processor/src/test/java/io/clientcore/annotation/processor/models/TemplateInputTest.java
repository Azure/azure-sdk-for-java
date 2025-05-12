// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.models;

import io.clientcore.annotation.processor.mocks.MockDeclaredType;
import io.clientcore.annotation.processor.mocks.MockPathParam;
import io.clientcore.annotation.processor.mocks.MockTypeMirror;
import io.clientcore.core.http.annotations.PathParam;
import io.clientcore.core.http.annotations.UnexpectedResponseExceptionDetail;
import org.junit.jupiter.api.Test;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the {@link TemplateInput} class.
 */
public class TemplateInputTest {

    @Test
    void getHostReturnsCorrectHost() {
        TemplateInput templateInput = new TemplateInput();
        templateInput.setHost("localhost");
        assertEquals("localhost", templateInput.getHost());
    }

    @Test
    void setHostUpdatesHost() {
        TemplateInput templateInput = new TemplateInput();
        templateInput.setHost("127.0.0.1");
        assertEquals("127.0.0.1", templateInput.getHost());
    }

    @Test
    void addImportAddsValidImport() {
        TemplateInput templateInput = new TemplateInput();
        String shortName = templateInput.addImport("java.util.List");
        assertEquals("List", shortName);
        assertTrue(templateInput.getImports().containsKey("java.util.List"));
    }

    @Test
    void addImportIgnoresNullImport() {
        TemplateInput templateInput = new TemplateInput();
        String shortName = templateInput.addImport((String) null);
        assertNull(shortName);
        assertTrue(templateInput.getImports().isEmpty());
    }

    @Test
    void addImportIgnoresEmptyImport() {
        TemplateInput templateInput = new TemplateInput();
        String shortName = templateInput.addImport("");
        assertNull(shortName);
        assertTrue(templateInput.getImports().isEmpty());
    }

    @Test
    void addImportTypeMirrorAddsValidImport() {
        TemplateInput templateInput = new TemplateInput();
        DeclaredType declaredType = new MockDeclaredType(TypeKind.DECLARED, "java.util.Map");
        String shortName = templateInput.addImport(declaredType);
        assertEquals("Map", shortName);
        assertTrue(templateInput.getImports().containsKey("java.util.Map"));
    }

    @Test
    void addImportTypeMirrorHandlesPrimitiveType() {
        TemplateInput templateInput = new TemplateInput();
        TypeMirror typeMirror = new MockTypeMirror(TypeKind.INT, "int");
        String shortName = templateInput.addImport(typeMirror);
        assertEquals("int", shortName);
        assertFalse(templateInput.getImports().containsKey("int"));
    }

    @Test
    void setAndGetServiceInterfaceFQN() {
        TemplateInput templateInput = new TemplateInput();
        templateInput.setServiceInterfaceFQN("com.example.Service");
        assertEquals("com.example.Service", templateInput.getServiceInterfaceFQN());
    }

    @Test
    void setAndGetUnexpectedResponseExceptionDetails() {
        TemplateInput templateInput = new TemplateInput();
        List<UnexpectedResponseExceptionDetail> details
            = Collections.singletonList(new MockUnexpectedResponseExceptionDetail());
        templateInput.setUnexpectedResponseExceptionDetails(details);
        assertEquals(details, templateInput.getUnexpectedResponseExceptionDetails());
    }

    @Test
    void shouldThrowExceptionWhenPathParamValueIsNull() {
        String paramName = "testParam";
        PathParam pathParam = new MockPathParam(null, false); // Mock with null value
        HttpRequestContext method = new HttpRequestContext();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            if (pathParam.value() == null) {
                throw new IllegalArgumentException("Path parameter '" + paramName + "' must not be null.");
            }
            method.addSubstitution(new Substitution(pathParam.value(), paramName, pathParam.encoded()));
        });

        assertEquals("Path parameter 'testParam' must not be null.", exception.getMessage());
    }

    private static final class MockUnexpectedResponseExceptionDetail implements UnexpectedResponseExceptionDetail {
        @Override
        public int[] statusCode() {
            return new int[0];
        }

        @Override
        public Class<?> exceptionBodyClass() {
            return String.class;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return UnexpectedResponseExceptionDetail.class;
        }
    }
}
