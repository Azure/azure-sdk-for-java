/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package sample.cloudfoundry.servicebus;

import com.microsoft.azure.servicebus.*;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

@RestController
public class ServiceBusRestController {

    private static final Logger LOG = LoggerFactory
            .getLogger(ServiceBusRestController.class);

    private static final String CR = "</BR>";
    private static StringBuffer currentResult = null;
    @Autowired
    private QueueClient queueClientForSending;
    @Autowired
    private QueueClient queueClientForReceiving;

    @PostConstruct
    private void postConstruct() {
        LOG.debug("postConstruct start...");
        try {
            LOG.debug("registering queue handler...");
            queueClientForReceiving.registerMessageHandler(new MessageHandler(),
                    new MessageHandlerOptions());
            LOG.debug("done registering handlers...");
        } catch (InterruptedException e) {
            LOG.error("Error registering message handler", e);
        } catch (ServiceBusException e) {
            LOG.error("Error registering message handler", e);
        }
        LOG.debug("postConstruct end.");
    }

    @RequestMapping(value = "/sb", method = RequestMethod.GET)
    @ResponseBody
    @SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    public String processMessages(HttpServletResponse response) {
        final StringBuffer result = new StringBuffer();
        currentResult = result;
        result.append("starting..." + CR);
        try {
            result.append("sending queue message" + CR);
            sendQueueMessage();
            result.append("receiving queue message" + CR);
            Thread.sleep(2000);

            result.append("done!" + CR);

        } catch (ServiceBusException e) {
            LOG.error("Error processing messages", e);
        } catch (InterruptedException e) {
            LOG.error("Error processing messages", e);
        } finally {
            currentResult = null;
        }

        return result.toString();
    }

    // NOTE: Please be noted that below are the minimum code for demonstrating
    // the usage of autowired clients.
    // For complete documentation of Service Bus, reference
    // https://azure.microsoft.com/en-us/services/service-bus/
    private void sendQueueMessage() throws ServiceBusException,
            InterruptedException {
        final String messageBody = "queue message";
        LOG.debug("Sending message: " + messageBody);
        final Message message = new Message(
                messageBody.getBytes(StandardCharsets.UTF_8));
        queueClientForSending.send(message);
    }

    static class MessageHandler implements IMessageHandler {
        public CompletableFuture<Void> onMessageAsync(IMessage message) {
            final String messageString = new String(message.getBody(),
                    StandardCharsets.UTF_8);
            LOG.debug("Received message: " + messageString);
            if (currentResult != null) {
                currentResult.append("Received message: " + messageString + CR);
            }
            return CompletableFuture.completedFuture(null);
        }

        public void notifyException(Throwable exception, ExceptionPhase phase) {
            LOG.error(phase + " encountered exception:", exception);
        }
    }
}
