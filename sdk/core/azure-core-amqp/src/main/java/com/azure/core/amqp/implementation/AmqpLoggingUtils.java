package com.azure.core.amqp.implementation;

import com.azure.core.util.logging.LoggingEventBuilder;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.Sinks;

import static com.azure.core.amqp.implementation.ClientConstants.EMIT_RESULT_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.ERROR_CONDITION_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.ERROR_DESCRIPTION_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.NOT_APPLICABLE;
import static com.azure.core.amqp.implementation.ClientConstants.SIGNAL_TYPE_KEY;

public class AmqpLoggingUtils {
    public static LoggingEventBuilder addSignalTypeAndResult(LoggingEventBuilder logBuilder, SignalType signalType, Sinks.EmitResult result) {
        return logBuilder
            .addKeyValue(SIGNAL_TYPE_KEY, signalType)
            .addKeyValue(EMIT_RESULT_KEY, result);
    }

    public static LoggingEventBuilder addErrorCondition(LoggingEventBuilder logBuilder, ErrorCondition errorCondition) {
        if (errorCondition == null) {
            return logBuilder
                .addKeyValue(ERROR_CONDITION_KEY, NOT_APPLICABLE)
                .addKeyValue(ERROR_DESCRIPTION_KEY, NOT_APPLICABLE);
        }

        return logBuilder
            .addKeyValue(ERROR_CONDITION_KEY, errorCondition.getCondition())
            .addKeyValue(ERROR_DESCRIPTION_KEY,  errorCondition.getDescription());
    }
}

