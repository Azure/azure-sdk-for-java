// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.contentvalidation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DownloadContentValidationOptionsTests {

    @Test
    public void testDefaultValues() {
        DownloadContentValidationOptions options = new DownloadContentValidationOptions();
        
        assertFalse(options.isStructuredMessageValidationEnabled());
        assertFalse(options.isMd5ValidationEnabled());
    }

    @Test
    public void testStructuredMessageValidationEnabled() {
        DownloadContentValidationOptions options = new DownloadContentValidationOptions()
            .setStructuredMessageValidationEnabled(true);
        
        assertTrue(options.isStructuredMessageValidationEnabled());
        assertFalse(options.isMd5ValidationEnabled());
    }

    @Test
    public void testMd5ValidationEnabled() {
        DownloadContentValidationOptions options = new DownloadContentValidationOptions()
            .setMd5ValidationEnabled(true);
        
        assertFalse(options.isStructuredMessageValidationEnabled());
        assertTrue(options.isMd5ValidationEnabled());
    }

    @Test
    public void testBothValidationsEnabled() {
        DownloadContentValidationOptions options = new DownloadContentValidationOptions()
            .setStructuredMessageValidationEnabled(true)
            .setMd5ValidationEnabled(true);
        
        assertTrue(options.isStructuredMessageValidationEnabled());
        assertTrue(options.isMd5ValidationEnabled());
    }

    @Test
    public void testFluentInterface() {
        DownloadContentValidationOptions options = new DownloadContentValidationOptions();
        
        DownloadContentValidationOptions result = options
            .setStructuredMessageValidationEnabled(true)
            .setMd5ValidationEnabled(false);
        
        assertSame(options, result); // Verify fluent interface returns same instance
        assertTrue(options.isStructuredMessageValidationEnabled());
        assertFalse(options.isMd5ValidationEnabled());
    }
}