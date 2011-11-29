package com.microsoft.windowsazure.services.serviceBus;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.microsoft.windowsazure.services.core.Builder;
import com.microsoft.windowsazure.services.core.Builder.Alteration;
import com.microsoft.windowsazure.services.core.Builder.Registry;
import com.microsoft.windowsazure.services.core.Configuration;
import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.core.ServiceFilter;
import com.microsoft.windowsazure.services.core.ServiceFilter.Request;
import com.microsoft.windowsazure.services.core.ServiceFilter.Response;
import com.microsoft.windowsazure.services.serviceBus.implementation.CorrelationFilter;
import com.microsoft.windowsazure.services.serviceBus.implementation.EmptyRuleAction;
import com.microsoft.windowsazure.services.serviceBus.implementation.FalseFilter;
import com.microsoft.windowsazure.services.serviceBus.implementation.SqlRuleAction;
import com.microsoft.windowsazure.services.serviceBus.implementation.TrueFilter;
import com.microsoft.windowsazure.services.serviceBus.models.BrokeredMessage;
import com.microsoft.windowsazure.services.serviceBus.models.ListQueuesResult;
import com.microsoft.windowsazure.services.serviceBus.models.ListRulesResult;
import com.microsoft.windowsazure.services.serviceBus.models.ListSubscriptionsResult;
import com.microsoft.windowsazure.services.serviceBus.models.ListTopicsResult;
import com.microsoft.windowsazure.services.serviceBus.models.QueueInfo;
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveMessageOptions;
import com.microsoft.windowsazure.services.serviceBus.models.RuleInfo;
import com.microsoft.windowsazure.services.serviceBus.models.SubscriptionInfo;
import com.microsoft.windowsazure.services.serviceBus.models.TopicInfo;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.LoggingFilter;

public class ServiceBusIntegrationTest extends IntegrationTestBase {

    private ServiceBusContract service;

    static ReceiveMessageOptions RECEIVE_AND_DELETE_5_SECONDS = new ReceiveMessageOptions().setReceiveAndDelete()
            .setTimeout(5);
    static ReceiveMessageOptions PEEK_LOCK_5_SECONDS = new ReceiveMessageOptions().setPeekLock().setTimeout(5);

    @Before
    public void createService() throws Exception {
        // reinitialize configuration from known state
        Configuration config = Configuration.load();
        overrideWithEnv(config, ServiceBusConfiguration.URI);
        overrideWithEnv(config, ServiceBusConfiguration.WRAP_URI);
        overrideWithEnv(config, ServiceBusConfiguration.WRAP_NAME);
        overrideWithEnv(config, ServiceBusConfiguration.WRAP_PASSWORD);
        overrideWithEnv(config, ServiceBusConfiguration.WRAP_SCOPE);

        // add LoggingFilter to any pipeline that is created
        Registry builder = (Registry) config.getBuilder();
        builder.alter(Client.class, new Alteration<Client>() {
            @Override
            public Client alter(Client instance, Builder builder, Map<String, Object> properties) {
                instance.addFilter(new LoggingFilter());
                return instance;
            }
        });

        // applied as default configuration 
        Configuration.setInstance(config);
        service = ServiceBusService.create();
    }

    private static void overrideWithEnv(Configuration config, String key) {
        String value = System.getenv(key);
        if (value == null)
            return;

        config.setProperty(key, value);
    }

    @Test
    public void fetchQueueAndListQueuesWorks() throws Exception {
        // Arrange

        // Act
        QueueInfo entry = service.getQueue("TestAlpha").getValue();
        ListQueuesResult feed = service.listQueues();

        // Assert
        assertNotNull(entry);
        assertNotNull(feed);
    }

    @Test
    public void createQueueWorks() throws Exception {
        // Arrange

        // Act
        QueueInfo queue = new QueueInfo("TestCreateQueueWorks").setMaxSizeInMegabytes(1024L);
        QueueInfo saved = service.createQueue(queue).getValue();

        // Assert
        assertNotNull(saved);
        assertNotSame(queue, saved);
        assertEquals("TestCreateQueueWorks", saved.getPath());
    }

    @Test
    public void deleteQueueWorks() throws Exception {
        // Arrange
        service.createQueue(new QueueInfo("TestDeleteQueueWorks"));

        // Act
        service.deleteQueue("TestDeleteQueueWorks");

        // Assert
    }

    @Test
    public void sendMessageWorks() throws Exception {
        // Arrange
        BrokeredMessage message = new BrokeredMessage("sendMessageWorks");

        // Act
        service.sendQueueMessage("TestAlpha", message);

        // Assert
    }

    @Test
    public void receiveMessageWorks() throws Exception {
        // Arrange
        String queueName = "TestReceiveMessageWorks";
        service.createQueue(new QueueInfo(queueName));
        service.sendQueueMessage(queueName, new BrokeredMessage("Hello World"));

        // Act
        BrokeredMessage message = service.receiveQueueMessage(queueName, RECEIVE_AND_DELETE_5_SECONDS).getValue();
        byte[] data = new byte[100];
        int size = message.getBody().read(data);

        // Assert
        assertEquals(11, size);
        assertArrayEquals("Hello World".getBytes(), Arrays.copyOf(data, size));
    }

    @Test
    public void peekLockMessageWorks() throws Exception {
        // Arrange
        String queueName = "TestPeekLockMessageWorks";
        service.createQueue(new QueueInfo(queueName));
        service.sendQueueMessage(queueName, new BrokeredMessage("Hello Again"));

        // Act
        BrokeredMessage message = service.receiveQueueMessage(queueName, PEEK_LOCK_5_SECONDS).getValue();

        // Assert
        byte[] data = new byte[100];
        int size = message.getBody().read(data);
        assertEquals(11, size);
        assertEquals("Hello Again", new String(data, 0, size));
    }

    @Test
    public void peekLockedMessageCanBeCompleted() throws Exception {
        // Arrange
        String queueName = "TestPeekLockedMessageCanBeCompleted";
        service.createQueue(new QueueInfo(queueName));
        service.sendQueueMessage(queueName, new BrokeredMessage("Hello Again"));
        BrokeredMessage message = service.receiveQueueMessage(queueName, PEEK_LOCK_5_SECONDS).getValue();

        // Act
        String lockToken = message.getLockToken();
        Date lockedUntil = message.getLockedUntilUtc();
        String lockLocation = message.getLockLocation();

        service.deleteMessage(message);

        // Assert
        assertNotNull(lockToken);
        assertNotNull(lockedUntil);
        assertNotNull(lockLocation);
    }

    @Test
    public void peekLockedMessageCanBeUnlocked() throws Exception {
        // Arrange
        String queueName = "TestPeekLockedMessageCanBeUnlocked";
        service.createQueue(new QueueInfo(queueName));
        service.sendQueueMessage(queueName, new BrokeredMessage("Hello Again"));
        BrokeredMessage peekedMessage = service.receiveQueueMessage(queueName, PEEK_LOCK_5_SECONDS).getValue();

        // Act
        String lockToken = peekedMessage.getLockToken();
        Date lockedUntil = peekedMessage.getLockedUntilUtc();

        service.unlockMessage(peekedMessage);
        BrokeredMessage receivedMessage = service.receiveQueueMessage(queueName, RECEIVE_AND_DELETE_5_SECONDS)
                .getValue();

        // Assert
        assertNotNull(lockToken);
        assertNotNull(lockedUntil);
        assertNull(receivedMessage.getLockToken());
        assertNull(receivedMessage.getLockedUntilUtc());
    }

    @Test
    public void peekLockedMessageCanBeDeleted() throws Exception {
        // Arrange
        String queueName = "TestPeekLockedMessageCanBeDeleted";
        service.createQueue(new QueueInfo(queueName));
        service.sendQueueMessage(queueName, new BrokeredMessage("Hello Again"));
        BrokeredMessage peekedMessage = service.receiveQueueMessage(queueName, PEEK_LOCK_5_SECONDS).getValue();

        // Act
        String lockToken = peekedMessage.getLockToken();
        Date lockedUntil = peekedMessage.getLockedUntilUtc();

        service.deleteMessage(peekedMessage);
        BrokeredMessage receivedMessage = service.receiveQueueMessage(queueName, RECEIVE_AND_DELETE_5_SECONDS)
                .getValue();

        // Assert
        assertNotNull(lockToken);
        assertNotNull(lockedUntil);
        assertNull(receivedMessage.getLockToken());
        assertNull(receivedMessage.getLockedUntilUtc());
    }

    @Test
    public void contentTypePassesThrough() throws Exception {
        // Arrange
        String queueName = "TestContentTypePassesThrough";
        service.createQueue(new QueueInfo(queueName));

        // Act
        service.sendQueueMessage(queueName, new BrokeredMessage("<data>Hello Again</data>").setContentType("text/xml"));

        BrokeredMessage message = service.receiveQueueMessage(queueName, RECEIVE_AND_DELETE_5_SECONDS).getValue();

        // Assert
        assertNotNull(message);
        assertEquals("text/xml", message.getContentType());
    }

    @Test
    public void topicCanBeCreatedListedFetchedAndDeleted() throws ServiceException {
        // Arrange
        String topicName = "TestTopicCanBeCreatedListedFetchedAndDeleted";

        // Act
        TopicInfo created = service.createTopic(new TopicInfo().setPath(topicName)).getValue();
        ListTopicsResult listed = service.listTopics();
        TopicInfo fetched = service.getTopic(topicName).getValue();
        service.deleteTopic(topicName);
        ListTopicsResult listed2 = service.listTopics();

        // Assert
        assertNotNull(created);
        assertNotNull(listed);
        assertNotNull(fetched);
        assertNotNull(listed2);

        assertEquals(listed.getItems().size() - 1, listed2.getItems().size());
    }

    @Test
    public void filterCanSeeAndChangeRequestOrResponse() throws ServiceException {
        // Arrange
        final List<Request> requests = new ArrayList<Request>();
        final List<Response> responses = new ArrayList<Response>();

        ServiceBusContract filtered = service.withFilter(new ServiceFilter() {
            @Override
            public Response handle(Request request, Next next) throws Exception {
                requests.add(request);
                Response response = next.handle(request);
                responses.add(response);
                return response;
            }
        });

        // Act 
        QueueInfo created = filtered.createQueue(new QueueInfo("TestFilterCanSeeAndChangeRequestOrResponse"))
                .getValue();

        // Assert
        assertNotNull(created);
        assertEquals(1, requests.size());
        assertEquals(1, responses.size());
    }

    @Test
    public void subscriptionsCanBeCreatedOnTopics() throws Exception {
        // Arrange
        String topicName = "TestSubscriptionsCanBeCreatedOnTopics";
        service.createTopic(new TopicInfo(topicName));

        // Act
        SubscriptionInfo created = service.createSubscription(topicName, new SubscriptionInfo("MySubscription"))
                .getValue();

        // Assert
        assertNotNull(created);
        assertEquals("MySubscription", created.getName());
    }

    @Test
    public void subscriptionsCanBeListed() throws Exception {
        // Arrange
        String topicName = "TestSubscriptionsCanBeListed";
        service.createTopic(new TopicInfo(topicName));
        service.createSubscription(topicName, new SubscriptionInfo("MySubscription2"));

        // Act
        ListSubscriptionsResult result = service.listSubscriptions(topicName);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals("MySubscription2", result.getItems().get(0).getName());
    }

    @Test
    public void subscriptionsDetailsMayBeFetched() throws Exception {
        // Arrange
        String topicName = "TestSubscriptionsDetailsMayBeFetched";
        service.createTopic(new TopicInfo(topicName));
        service.createSubscription(topicName, new SubscriptionInfo("MySubscription3"));

        // Act
        SubscriptionInfo result = service.getSubscription(topicName, "MySubscription3").getValue();

        // Assert
        assertNotNull(result);
        assertEquals("MySubscription3", result.getName());
    }

    @Test
    public void subscriptionsMayBeDeleted() throws Exception {
        // Arrange
        String topicName = "TestSubscriptionsMayBeDeleted";
        service.createTopic(new TopicInfo(topicName));
        service.createSubscription(topicName, new SubscriptionInfo("MySubscription4"));
        service.createSubscription(topicName, new SubscriptionInfo("MySubscription5"));

        // Act
        service.deleteSubscription(topicName, "MySubscription4");

        // Assert
        ListSubscriptionsResult result = service.listSubscriptions(topicName);
        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals("MySubscription5", result.getItems().get(0).getName());
    }

    @Test
    public void subscriptionWillReceiveMessage() throws Exception {
        // Arrange
        String topicName = "TestSubscriptionWillReceiveMessage";
        service.createTopic(new TopicInfo(topicName));
        service.createSubscription(topicName, new SubscriptionInfo("sub"));
        service.sendTopicMessage(topicName,
                new BrokeredMessage("<p>Testing subscription</p>").setContentType("text/html"));

        // Act
        BrokeredMessage message = service.receiveSubscriptionMessage(topicName, "sub", RECEIVE_AND_DELETE_5_SECONDS)
                .getValue();

        // Assert
        assertNotNull(message);

        byte[] data = new byte[100];
        int size = message.getBody().read(data);
        assertEquals("<p>Testing subscription</p>", new String(data, 0, size));
        assertEquals("text/html", message.getContentType());
    }

    @Test
    public void rulesCanBeCreatedOnSubscriptions() throws Exception {
        // Arrange
        String topicName = "TestrulesCanBeCreatedOnSubscriptions";
        service.createTopic(new TopicInfo(topicName));
        service.createSubscription(topicName, new SubscriptionInfo("sub"));

        // Act
        RuleInfo created = service.createRule(topicName, "sub", new RuleInfo("MyRule1")).getValue();

        // Assert
        assertNotNull(created);
        assertEquals("MyRule1", created.getName());
    }

    @Test
    public void rulesCanBeListedAndDefaultRuleIsPrecreated() throws Exception {
        // Arrange
        String topicName = "TestrulesCanBeListedAndDefaultRuleIsPrecreated";
        service.createTopic(new TopicInfo(topicName));
        service.createSubscription(topicName, new SubscriptionInfo("sub"));
        service.createRule(topicName, "sub", new RuleInfo("MyRule2"));

        // Act
        ListRulesResult result = service.listRules(topicName, "sub");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getItems().size());
        RuleInfo rule0 = result.getItems().get(0);
        RuleInfo rule1 = result.getItems().get(1);
        if (rule0.getName() == "MyRule2") {
            RuleInfo swap = rule1;
            rule1 = rule0;
            rule0 = swap;
        }

        assertEquals("$Default", rule0.getName());
        assertEquals("MyRule2", rule1.getName());
        assertNotNull(result.getItems().get(0).getModel());
    }

    @Test
    public void ruleDetailsMayBeFetched() throws Exception {
        // Arrange
        String topicName = "TestruleDetailsMayBeFetched";
        service.createTopic(new TopicInfo(topicName));
        service.createSubscription(topicName, new SubscriptionInfo("sub"));

        // Act
        RuleInfo result = service.getRule(topicName, "sub", "$Default").getValue();

        // Assert
        assertNotNull(result);
        assertEquals("$Default", result.getName());
    }

    @Test
    public void rulesMayBeDeleted() throws Exception {
        // Arrange
        String topicName = "TestRulesMayBeDeleted";
        service.createTopic(new TopicInfo(topicName));
        service.createSubscription(topicName, new SubscriptionInfo("sub"));
        service.createRule(topicName, "sub", new RuleInfo("MyRule4"));
        service.createRule(topicName, "sub", new RuleInfo("MyRule5"));

        // Act
        service.deleteRule(topicName, "sub", "MyRule5");
        service.deleteRule(topicName, "sub", "$Default");

        // Assert
        ListRulesResult result = service.listRules(topicName, "sub");
        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals("MyRule4", result.getItems().get(0).getName());
    }

    @Test
    public void rulesMayHaveActionAndFilter() throws ServiceException {
        // Arrange
        String topicName = "TestRulesMayHaveAnActionAndFilter";
        service.createTopic(new TopicInfo(topicName));
        service.createSubscription(topicName, new SubscriptionInfo("sub"));

        // Act
        RuleInfo ruleOne = service.createRule(topicName, "sub", new RuleInfo("One").withCorrelationIdFilter("my-id"))
                .getValue();
        RuleInfo ruleTwo = service.createRule(topicName, "sub",
                new RuleInfo("Two").withTrueSqlExpressionFilter("my-true-expression")).getValue();
        RuleInfo ruleThree = service.createRule(topicName, "sub",
                new RuleInfo("Three").withFalseSqlExpressionFilter("my-false-expression")).getValue();
        RuleInfo ruleFour = service.createRule(topicName, "sub", new RuleInfo("Four").withEmptyRuleAction()).getValue();
        RuleInfo ruleFive = service.createRule(topicName, "sub", new RuleInfo("Five").withSqlRuleAction("SET x = 5"))
                .getValue();

        // Assert
        assertEquals(CorrelationFilter.class, ruleOne.getFilter().getClass());
        assertEquals(TrueFilter.class, ruleTwo.getFilter().getClass());
        assertEquals(FalseFilter.class, ruleThree.getFilter().getClass());
        assertEquals(EmptyRuleAction.class, ruleFour.getAction().getClass());
        assertEquals(SqlRuleAction.class, ruleFive.getAction().getClass());

    }
}
