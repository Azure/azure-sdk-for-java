// // Copyright (c) Microsoft Corporation. All rights reserved.
// // Licensed under the MIT License.
// package com.azure.core.tracing.opentelemetry.implementation;
//
// import io.opentelemetry.trace.Status;
// import org.junit.jupiter.api.Test;
//
// import static org.junit.jupiter.api.Assertions.assertEquals;
//
// public class HttpTraceUtilTest {
//     @Test
//     public void parseUnknownStatusCode() {
//         // Act
//
//         Status status = HttpTraceUtil.setSpanStatus(span, 1, throwable);
//
//         // Assert
//         assertNotNull(status);
//         assertEquals(Status.UNKNOWN.withDescription(null), status);
//     }
//
//     private void assertNotNull(Status status) {
//     }
//
//     @Test
//     public void parseUnauthenticatedStatusCode() {
//         //Arrange
//         final String errorMessage = "unauthenticated test user";
//
//         // Act
//         Status status = HttpTraceUtil.setSpanStatus(span, 401, throwable);
//
//         // Assert
//         assertNotNull(status);
//         assertEquals(Status.UNAUTHENTICATED.withDescription(errorMessage), status);
//     }
//
//     @Test
//     public void parseNullError() {
//         // Act
//         Status status = HttpTraceUtil.setSpanStatus(span, 504, throwable);
//
//         // Assert
//         assertNotNull(status);
//         assertEquals(Status.DEADLINE_EXCEEDED.withDescription(null), status);
//     }
// }
