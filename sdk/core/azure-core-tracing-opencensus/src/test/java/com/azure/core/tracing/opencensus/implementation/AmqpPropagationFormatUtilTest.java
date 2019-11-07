// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.tracing.opencensus.implementation;

import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;
import com.azure.core.util.Context;
import io.opencensus.trace.SpanContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class AmqpPropagationFormatUtilTest {

    @Test
    public void extractContextReturnsSpanContext() {
        // Act
        Context context = AmqpPropagationFormatUtil.extractContext("", Context.NONE);

        // Assert
        Assertions.assertNotNull(context);
        Assertions.assertTrue(context.getData(SPAN_CONTEXT_KEY).isPresent());
    }

    @Test
    public void getInvalidSpanContext() {
        // Act
        Context context = AmqpPropagationFormatUtil.extractContext("", Context.NONE);

        // Assert
        Assertions.assertNotNull(context);
        Assertions.assertFalse(((SpanContext) context.getData(SPAN_CONTEXT_KEY).get()).getTraceId().isValid(), "Invalid diagnostic Id, returns invalid SpanContext ");
    }

    @Test
    public void getValidSpanContext() {
        // Act
        Context context = AmqpPropagationFormatUtil.extractContext("00-0af7651916cd43dd8448eb211c80319c-b9c7c989f97918e1-01", Context.NONE);

        // Assert
        Assertions.assertNotNull(context);
        Assertions.assertTrue(((SpanContext) context.getData(SPAN_CONTEXT_KEY).get()).getTraceId().isValid(), "Valid diagnostic Id, returns valid SpanContext ");
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
        Assertions.assertNotNull(diagnosticId);
        Assertions.assertEquals(testDiagnosticID, diagnosticId);
    }
}
