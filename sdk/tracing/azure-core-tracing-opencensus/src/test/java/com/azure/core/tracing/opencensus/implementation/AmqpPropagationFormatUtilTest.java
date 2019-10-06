// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.tracing.opencensus.implementation;

import com.azure.core.util.Context;
import io.opencensus.trace.*;
import org.junit.Assert;
import org.junit.Test;

import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT;

public class AmqpPropagationFormatUtilTest {

    @Test
    public void extractContextReturnsSpanContext() {
        // Act
        Context context = AmqpPropagationFormatUtil.extractContext("", Context.NONE);

        // Assert
        Assert.assertNotNull(context);
        Assert.assertTrue(context.getData(SPAN_CONTEXT).isPresent());
    }

    @Test
    public void getInvalidSpanContext() {
        // Act
        Context context = AmqpPropagationFormatUtil.extractContext("", Context.NONE);

        // Assert
        Assert.assertNotNull(context);
        Assert.assertFalse("Invalid diagnostic Id, returns invalid SpanContext ",
            ((SpanContext) context.getData(SPAN_CONTEXT).get()).getTraceId().isValid());
    }

    @Test
    public void getValidSpanContext() {
        // Act
        Context context = AmqpPropagationFormatUtil.extractContext("00-0af7651916cd43dd8448eb211c80319c-b9c7c989f97918e1-01", Context.NONE);

        // Assert
        Assert.assertNotNull(context);
        Assert.assertTrue("Valid diagnostic Id, returns valid SpanContext ",
            ((SpanContext) context.getData(SPAN_CONTEXT).get()).getTraceId().isValid());
    }

    @Test
    public void getValidDiagnosticId() {
        //Arrange
        final String testDiagnosticID = "00-0af7651916cd43dd8448eb211c80319c-b9c7c989f97918e1-01";
        final Context context = AmqpPropagationFormatUtil.extractContext(testDiagnosticID, Context.NONE);
        final SpanContext spanContext = (SpanContext) context.getData(SPAN_CONTEXT).get();

        // Act
        String diagnosticId = AmqpPropagationFormatUtil.getDiagnosticId(spanContext);

        // Assert
        Assert.assertNotNull(diagnosticId);
        Assert.assertEquals(testDiagnosticID, diagnosticId);
    }
}
