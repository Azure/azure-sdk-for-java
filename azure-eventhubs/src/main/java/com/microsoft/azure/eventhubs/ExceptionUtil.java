/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs;

import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;

import com.microsoft.azure.eventhubs.amqp.AmqpErrorCode;
import com.microsoft.azure.eventhubs.amqp.AmqpException;
import com.microsoft.azure.eventhubs.amqp.AmqpResponseCode;

public final class ExceptionUtil {
    static Exception toException(ErrorCondition errorCondition) {
        if (errorCondition == null) {
            throw new IllegalArgumentException("'null' errorCondition cannot be translated to EventHubException");
        }

        if (errorCondition.getCondition() == ClientConstants.TIMEOUT_ERROR) {
            return new EventHubException(ClientConstants.DEFAULT_IS_TRANSIENT, new TimeoutException(errorCondition.getDescription()));
        } else if (errorCondition.getCondition() == ClientConstants.SERVER_BUSY_ERROR) {
            return new ServerBusyException(errorCondition.getDescription());
        } else if (errorCondition.getCondition() == AmqpErrorCode.NotFound) {
            return new IllegalEntityException(errorCondition.getDescription());
        } else if (errorCondition.getCondition() == ClientConstants.ENTITY_DISABLED_ERROR) {
            return new IllegalEntityException(errorCondition.getDescription());
        } else if (errorCondition.getCondition() == AmqpErrorCode.Stolen) {
            return new ReceiverDisconnectedException(errorCondition.getDescription());
        } else if (errorCondition.getCondition() == AmqpErrorCode.UnauthorizedAccess) {
            return new AuthorizationFailedException(errorCondition.getDescription());
        } else if (errorCondition.getCondition() == AmqpErrorCode.PayloadSizeExceeded) {
            return new PayloadSizeExceededException(errorCondition.getDescription());
        } else if (errorCondition.getCondition() == AmqpErrorCode.InternalError) {
            return new EventHubException(true, new AmqpException(errorCondition));
        } else if (errorCondition.getCondition() == ClientConstants.ARGUMENT_ERROR) {
            return new EventHubException(false, errorCondition.getDescription(), new AmqpException(errorCondition));
        } else if (errorCondition.getCondition() == ClientConstants.ARGUMENT_OUT_OF_RANGE_ERROR) {
            return new EventHubException(false, errorCondition.getDescription(), new AmqpException(errorCondition));
        } else if (errorCondition.getCondition() == AmqpErrorCode.NotImplemented) {
            return new UnsupportedOperationException(errorCondition.getDescription());
        } else if (errorCondition.getCondition() == AmqpErrorCode.NotAllowed) {
            return new UnsupportedOperationException(errorCondition.getDescription());
        } else if (errorCondition.getCondition() == ClientConstants.PARTITION_NOT_OWNED_ERROR) {
            return new EventHubException(false, errorCondition.getDescription());
        } else if (errorCondition.getCondition() == ClientConstants.STORE_LOCK_LOST_ERROR) {
            return new EventHubException(false, errorCondition.getDescription());
        } else if (errorCondition.getCondition() == AmqpErrorCode.AmqpLinkDetachForced) {
            return new EventHubException(true, new AmqpException(errorCondition));
        } else if (errorCondition.getCondition() == AmqpErrorCode.ResourceLimitExceeded) {
            return new QuotaExceededException(new AmqpException(errorCondition));
        }

        return new EventHubException(ClientConstants.DEFAULT_IS_TRANSIENT, errorCondition.getDescription());
    }

    static Exception amqpResponseCodeToException(final int statusCode, final String statusDescription) {
        final AmqpResponseCode amqpResponseCode = AmqpResponseCode.valueOf(statusCode);
        if (amqpResponseCode == null)
            return new EventHubException(true, String.format(ClientConstants.AMQP_PUT_TOKEN_FAILED_ERROR, statusCode, statusDescription));

        switch (amqpResponseCode) {
            case BAD_REQUEST:
                return new IllegalArgumentException(String.format(ClientConstants.AMQP_PUT_TOKEN_FAILED_ERROR, statusCode, statusDescription));
            case NOT_FOUND:
                return new AmqpException(new ErrorCondition(AmqpErrorCode.NotFound, statusDescription));
            case FORBIDDEN:
                return new QuotaExceededException(String.format(ClientConstants.AMQP_PUT_TOKEN_FAILED_ERROR, statusCode, statusDescription));
            case UNAUTHORIZED:
                return new AuthorizationFailedException(String.format(ClientConstants.AMQP_PUT_TOKEN_FAILED_ERROR, statusCode, statusDescription));
            default:
                return new EventHubException(true, String.format(ClientConstants.AMQP_PUT_TOKEN_FAILED_ERROR, statusCode, statusDescription));
        }
    }

    static <T> void completeExceptionally(CompletableFuture<T> future, Exception exception, IErrorContextProvider contextProvider) {
        if (exception != null && exception instanceof EventHubException) {
            ErrorContext errorContext = contextProvider.getContext();
            ((EventHubException) exception).setContext(errorContext);
        }

        future.completeExceptionally(exception);
    }

    // not a specific message related error
    static boolean isGeneralSendError(Symbol amqpError) {
        return (amqpError == ClientConstants.SERVER_BUSY_ERROR
                || amqpError == ClientConstants.TIMEOUT_ERROR
                || amqpError == AmqpErrorCode.ResourceLimitExceeded);
    }

    static String getTrackingIDAndTimeToLog() {
        return String.format(Locale.US, "TrackingId: %s, at: %s", UUID.randomUUID().toString(), ZonedDateTime.now());
    }

    public static String toStackTraceString(final Throwable exception, final String customErrorMessage) {
        final StringBuilder builder = new StringBuilder();

        if (!StringUtil.isNullOrEmpty(customErrorMessage)) {
            builder.append(customErrorMessage);
            builder.append(System.lineSeparator());
        }

        builder.append(exception.getMessage());
        final StackTraceElement[] stackTraceElements = exception.getStackTrace();
        if (stackTraceElements != null) {
            for (final StackTraceElement ste : stackTraceElements) {
                builder.append(System.lineSeparator());
                builder.append(ste.toString());
            }
        }

        final Throwable innerException = exception.getCause();
        if (innerException != null) {
            builder.append("Cause: " + innerException.getMessage());
            final StackTraceElement[] innerStackTraceElements = innerException.getStackTrace();
            if (innerStackTraceElements != null) {
                for (final StackTraceElement ste : innerStackTraceElements) {
                    builder.append(System.lineSeparator());
                    builder.append(ste.toString());
                }
            }
        }

        return builder.toString();
    }

    public static Throwable getExceptionFromCompletedFuture(
            final CompletableFuture<?> exceptionallyCompletedFuture) {
        try {
            exceptionallyCompletedFuture.get();
        } catch (ExecutionException|InterruptedException exception) {
            final Throwable cause = exception.getCause();
            return (cause == null ? exception : cause);
        } catch (Exception exception) {
            return exception;
        }

        return null;
    }
}
