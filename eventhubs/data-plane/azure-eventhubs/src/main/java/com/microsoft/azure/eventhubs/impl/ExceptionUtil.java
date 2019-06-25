// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import com.microsoft.azure.eventhubs.AuthorizationFailedException;
import com.microsoft.azure.eventhubs.CommunicationException;
import com.microsoft.azure.eventhubs.ErrorContext;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.IllegalEntityException;
import com.microsoft.azure.eventhubs.PayloadSizeExceededException;
import com.microsoft.azure.eventhubs.QuotaExceededException;
import com.microsoft.azure.eventhubs.ReceiverDisconnectedException;
import com.microsoft.azure.eventhubs.ServerBusyException;
import com.microsoft.azure.eventhubs.TimeoutException;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            return ExceptionUtil.distinguishNotFound(errorCondition.getDescription());
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
        } else if (errorCondition.getCondition() == ClientConstants.PROTON_IO_ERROR) {
            String message = ClientConstants.COMMUNICATION_EXCEPTION_GENERIC_MESSAGE;
            if (errorCondition.getDescription() != null) {
                message = errorCondition.getDescription();
            }
            return new CommunicationException(message, null);
        }

        return new EventHubException(ClientConstants.DEFAULT_IS_TRANSIENT, errorCondition.getDescription());
    }

    static Exception amqpResponseCodeToException(final int statusCode, final String statusDescription) {
        final AmqpResponseCode amqpResponseCode = AmqpResponseCode.valueOf(statusCode);
        if (amqpResponseCode == null) {
            return new EventHubException(true, String.format(ClientConstants.AMQP_REQUEST_FAILED_ERROR, statusCode, statusDescription));
        }
        switch (amqpResponseCode) {
            case BAD_REQUEST:
                return new IllegalArgumentException(String.format(ClientConstants.AMQP_REQUEST_FAILED_ERROR, statusCode, statusDescription));
            case NOT_FOUND:
                return ExceptionUtil.distinguishNotFound(statusDescription);
            case FORBIDDEN:
                return new QuotaExceededException(String.format(ClientConstants.AMQP_REQUEST_FAILED_ERROR, statusCode, statusDescription));
            case UNAUTHORIZED:
                return new AuthorizationFailedException(String.format(ClientConstants.AMQP_REQUEST_FAILED_ERROR, statusCode, statusDescription));
            default:
                return new EventHubException(true, String.format(ClientConstants.AMQP_REQUEST_FAILED_ERROR, statusCode, statusDescription));
        }
    }

    static Exception distinguishNotFound(final String message) {
        Pattern p = Pattern.compile("The messaging entity .* could not be found");
        Matcher m = p.matcher(message);
        if (m.find()) {
            return new IllegalEntityException(message);
        } else {
            return new EventHubException(true, String.format(ClientConstants.AMQP_REQUEST_FAILED_ERROR, AmqpResponseCode.NOT_FOUND, message));
        }
    }

    static <T> void completeExceptionally(CompletableFuture<T> future, Exception exception, ErrorContextProvider contextProvider) {
        if (exception == null) {
            throw new NullPointerException();
        }

        if (exception instanceof EventHubException) {
            final ErrorContext errorContext = contextProvider.getContext();
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
        for (final StackTraceElement ste : stackTraceElements) {
            builder.append(System.lineSeparator());
            builder.append(ste.toString());
        }

        final Throwable innerException = exception.getCause();
        if (innerException != null) {
            builder.append("Cause: ").append(innerException.getMessage());
            final StackTraceElement[] innerStackTraceElements = innerException.getStackTrace();
            for (final StackTraceElement ste : innerStackTraceElements) {
                builder.append(System.lineSeparator());
                builder.append(ste.toString());
            }
        }

        return builder.toString();
    }

    public static Throwable getExceptionFromCompletedFuture(
            final CompletableFuture<?> exceptionallyCompletedFuture) {
        try {
            exceptionallyCompletedFuture.get();
        } catch (ExecutionException | InterruptedException exception) {
            final Throwable cause = exception.getCause();
            return (cause == null ? exception : cause);
        } catch (Exception exception) {
            return exception;
        }

        return null;
    }

    static Exception stripOuterException(final Exception exception) {
        Throwable throwable = exception.getCause();
        if (throwable instanceof EventHubException) {
            return (EventHubException) throwable;
        } else if (throwable instanceof RuntimeException) {
            return (RuntimeException) throwable;
        } else if (throwable != null) {
            return new RuntimeException(throwable);
        } else {
            return new RuntimeException(exception);
        }
    }

    private static void handle(final Exception exception) throws EventHubException {
        if (exception instanceof InterruptedException) {
            // Re-assert the thread's interrupted status
            Thread.currentThread().interrupt();
        }

        Throwable throwable = exception.getCause();
        if (throwable instanceof EventHubException) {
            throw (EventHubException) throwable;
        } else if (throwable instanceof RuntimeException) {
            throw (RuntimeException) throwable;
        } else if (throwable != null) {
            throw new RuntimeException(throwable);
        } else {
            throw new RuntimeException(exception);
        }
    }

    public static <T> T sync(final SyncFactory<T> factory) throws EventHubException {
        try {
            return factory.execute();
        } catch (InterruptedException | ExecutionException exception) {
            handle(exception);
            return null;
        }
    }

    public static <T> T syncWithIOException(final SyncFactoryWithIOException<T> factory) throws IOException, EventHubException {
        try {
            return factory.execute();
        } catch (InterruptedException | ExecutionException exception) {
            handle(exception);
            return null;
        }
    }

    public static void syncVoid(final SyncFactoryVoid factory) throws EventHubException {
        try {
            factory.execute();
        } catch (InterruptedException | ExecutionException exception) {
            handle(exception);
        }
    }

    public static <T> T syncWithIllegalArgException(final SyncFactoryWithIllegalArgException<T> factory) throws EventHubException {
        try {
            return factory.execute();
        } catch (InterruptedException | ExecutionException exception) {
            handle(exception);
            return null;
        }
    }

    @FunctionalInterface
    public interface SyncFactory<T> {
        T execute() throws EventHubException, ExecutionException, InterruptedException;
    }

    @FunctionalInterface
    public interface SyncFactoryWithIOException<T> {
        T execute() throws IOException, EventHubException, ExecutionException, InterruptedException;
    }

    @FunctionalInterface
    public interface SyncFactoryVoid {
        void execute() throws EventHubException, ExecutionException, InterruptedException;
    }

    @FunctionalInterface
    public interface SyncFactoryWithIllegalArgException<T> {
        T execute() throws IllegalArgumentException, EventHubException, ExecutionException, InterruptedException;
    }
}
