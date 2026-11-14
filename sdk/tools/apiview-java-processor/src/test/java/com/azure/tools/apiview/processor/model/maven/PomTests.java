// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apiview.processor.model.maven;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PomTests {
    @Test
    public void malformedPomPreservesParserFailure() {
        IOException exception = assertThrows(IOException.class,
            () -> new Pom(new ByteArrayInputStream("<project>".getBytes(StandardCharsets.UTF_8))));
        assertInstanceOf(SAXException.class, exception.getCause());
    }
}
