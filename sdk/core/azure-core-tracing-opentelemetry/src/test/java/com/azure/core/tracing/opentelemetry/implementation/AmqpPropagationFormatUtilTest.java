// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.tracing.opentelemetry.implementation;

import com.azure.core.util.Context;
import io.opentelemetry.trace.SpanContext;
import org.junit.Assert;
import org.junit.Test;

import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;


public class AmqpPropagationFormatUtilTest {

    // TODO: Issue to fix this - https://github.com/open-telemetry/opentelemetry-java/issues/659
    @Test
    public void extractContextReturnsSpanContext() {
        // Act
        Context context = AmqpPropagationFormatUtil.extractContext("", Context.NONE);

        // Assert
        Assert.assertNotNull(context);
        Assert.assertTrue(context.getData(SPAN_CONTEXT_KEY).isPresent());
    }

    @Test
    public void getInvalidSpanContext() {
        // Act
        Context context = AmqpPropagationFormatUtil.extractContext("", Context.NONE);

        // Assert
        Assert.assertNotNull(context);
        Assert.assertFalse("Invalid diagnostic Id, returns invalid SpanContext ",
            ((SpanContext) context.getData(SPAN_CONTEXT_KEY).get()).getTraceId().isValid());
    }

    @Test
    public void getValidSpanContext() {
        // Act
        Context context = AmqpPropagationFormatUtil.extractContext("00-0af7651916cd43dd8448eb211c80319c-b9c7c989f97918e1-01", Context.NONE);

        // Assert
        Assert.assertNotNull(context);
        Assert.assertTrue("Valid diagnostic Id, returns valid SpanContext ",
            ((SpanContext) context.getData(SPAN_CONTEXT_KEY).get()).getTraceId().isValid());
    }

    @Test
    public void getValidDiagnosticId() {
        //Arrange
        final String testDiagnosticID = "00-0af7651916cd43dd8448eb211c80319c-b9c7c989f97918e1-01";
        final Context context = AmqpPropagationFormatUtil.extractContext(testDiagnosticID, Context.NONE);
        final SpanContext spanContext = (SpanContext) context.getData(SPAN_CONTEXT_KEY).get();

        // Act
        String diagnosticId = AmqpPropagationFormatUtil.getDiagnosticId(spanContext);

        // Assert
        Assert.assertNotNull(diagnosticId);
        Assert.assertEquals(testDiagnosticID, diagnosticId);
    }
}
