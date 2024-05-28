package com.azure.cosmos.cris.querystuckrepro;

import com.azure.cosmos.rx.TestSuiteBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyLogger {
    protected static Logger logger = LoggerFactory.getLogger(DummyLogger.class.getSimpleName());

    public void logMessage(EMessageLevel msgLevel, ELogLevel logLevel, String message) {
        logger.info("{}: {} - {}", msgLevel, logLevel, message);
    }
}
