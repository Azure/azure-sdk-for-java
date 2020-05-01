/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.schemaregistry.avro;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AvroByteDecoderTest extends TestCase {
    public AvroByteDecoderTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(AvroByteDecoderTest.class);
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
