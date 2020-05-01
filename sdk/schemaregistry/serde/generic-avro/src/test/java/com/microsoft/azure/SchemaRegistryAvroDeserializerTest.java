/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class SchemaRegistryAvroDeserializerTest extends TestCase{
    public SchemaRegistryAvroDeserializerTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(SchemaRegistryAvroDeserializerTest.class);
    }

    protected void setUp() {
    }

    protected void tearDown() {
    }

    public void testShouldAnswerWithTrue()
    {
        assertTrue( true );
    }
}