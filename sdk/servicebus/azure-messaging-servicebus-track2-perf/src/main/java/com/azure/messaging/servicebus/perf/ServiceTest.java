// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.perf;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * Base class for performance test.
 * @param <TOptions> for performance configuration.
 */
abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(ServiceTest.class);
    protected static final int TOTAL_MESSAGE_MULTIPLIER = 300;

    protected static final String AZURE_SERVICE_BUS_CONNECTION_STRING = "AZURE_SERVICE_BUS_CONNECTION_STRING";
    protected static final String AZURE_SERVICEBUS_QUEUE_NAME = "AZURE_SERVICEBUS_QUEUE_NAME";
    protected static final String AZURE_SERVICEBUS_TOPIC_NAME = "AZURE_SERVICEBUS_TOPIC_NAME";
    protected static final String AZURE_SERVICEBUS_SUBSCRIPTION_NAME = "AZURE_SERVICEBUS_SUBSCRIPTION_NAME";
    protected final String connectionString;
    protected final String queueName;
    protected ServiceBusReceiverAsyncClient receiverAsync;

    private static final String FILE_FORMAT = "%s%ssb-performance-test-result-t2-%s.csv";

    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("MM-dd-yyyy-HHMMSSS");

    final ServiceBusReceiverClient receiver;
    final ServiceBusSenderClient sender;
    final ServiceBusSenderAsyncClient senderAsync;
    final ServiceBusClientBuilder baseBuilder;

    private final String resultFilePath;

    /**
     *
     * @param options to configure.
     * @param receiveMode to receive messages.
     * @throws IllegalArgumentException if environment variable not being available.
     */
    ServiceTest(TOptions options, ServiceBusReceiveMode receiveMode) {
        super(options);

        resultFilePath = String.format(FILE_FORMAT, System.getProperty("user.dir"), File.separatorChar,
            dateFormatter.format(new Date()));
        updateResult("Date, Use case, Number of messages, Total time(Seconds), messages/seconds, SDK Version");
        connectionString = System.getenv(AZURE_SERVICE_BUS_CONNECTION_STRING);
        if (CoreUtils.isNullOrEmpty(connectionString)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Environment variable "
                + AZURE_SERVICE_BUS_CONNECTION_STRING + " must be set."));
        }

        queueName = System.getenv(AZURE_SERVICEBUS_QUEUE_NAME);
        if (CoreUtils.isNullOrEmpty(queueName)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Environment variable "
                + AZURE_SERVICEBUS_QUEUE_NAME + " must be set."));
        }

        // Setup the service client
        baseBuilder = new ServiceBusClientBuilder()
            .proxyOptions(ProxyOptions.SYSTEM_DEFAULTS)
            .retryOptions(new AmqpRetryOptions().setTryTimeout(Duration.ofSeconds(60)))
            .transportType(AmqpTransportType.AMQP)
            .connectionString(connectionString);

        ServiceBusClientBuilder.ServiceBusReceiverClientBuilder receiverBuilder = baseBuilder
            .receiver()
            .receiveMode(receiveMode)
            .queueName(queueName);

        ServiceBusClientBuilder.ServiceBusSenderClientBuilder senderBuilder = baseBuilder
            .sender()
            .queueName(queueName);

        receiver = receiverBuilder.buildClient();

        sender = senderBuilder.buildClient();
        senderAsync = senderBuilder.buildAsyncClient();
    }

    protected void updateResult(String data) {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(resultFilePath, true);
            fileWriter.write(data);
            fileWriter.write("\n");
            fileWriter.close();
        } catch (IOException ex) {
            LOGGER.verbose("Could not write to the result file %s." , resultFilePath);
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {}
            }
        }

        LOGGER.verbose("Written to result file %s." , resultFilePath);
    }

    protected String getSDKVersion() {
        String version = null;
        String propFileName = "azure-messaging-servicebus.properties";
        try {
            Properties prop = new Properties();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
            if (inputStream != null) {
                prop.load(inputStream);
                version = (String) prop.get("version");
            } else {
                throw new FileNotFoundException("Property file '" + propFileName + "' not found in the classpath.");
            }
        } catch (IOException ee) {
            LOGGER.error("Error in loading property file %s.", propFileName);
        }
        return version;
    }

    protected void updateResults(String testName, int messagesReceived, Duration testDuration) {
        StringBuffer result = new StringBuffer();
        result.append(new Date().toString());
        result.append(",");
        result.append(testName);
        result.append(",");
        result.append(messagesReceived);
        result.append(",");
        result.append(testDuration.getSeconds());
        result.append(",");
        double mps = messagesReceived / (testDuration.getSeconds() * 1.000000);
        result.append(mps);
        result.append(",");
        result.append(getSDKVersion());
        updateResult(result.toString());
    }
}

