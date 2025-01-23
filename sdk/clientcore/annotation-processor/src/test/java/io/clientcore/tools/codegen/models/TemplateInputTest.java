// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.tools.codegen.models;

import io.clientcore.core.http.annotation.UnexpectedResponseExceptionDetail;
import java.util.Collections;
import java.util.List;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        DeclaredType declaredType = mock(DeclaredType.class);
        when(declaredType.toString()).thenReturn("java.util.Map");
        when(declaredType.getKind()).thenReturn(TypeKind.DECLARED);
        String shortName = templateInput.addImport(declaredType);
        assertEquals("Map", shortName);
        assertTrue(templateInput.getImports().containsKey("java.util.Map"));
    }

    @Test
    void addImportTypeMirrorHandlesPrimitiveType() {
        TemplateInput templateInput = new TemplateInput();
        TypeMirror typeMirror = mock(TypeMirror.class);
        when(typeMirror.toString()).thenReturn("int");
        when(typeMirror.getKind()).thenReturn(TypeKind.INT);
        String shortName = templateInput.addImport(typeMirror);
        assertEquals("int", shortName);
        assertTrue(templateInput.getImports().containsKey("int"));
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
        List<UnexpectedResponseExceptionDetail> details =
            Collections.singletonList(mock(UnexpectedResponseExceptionDetail.class));
        templateInput.setUnexpectedResponseExceptionDetails(details);
        assertEquals(details, templateInput.getUnexpectedResponseExceptionDetails());
    }
}
