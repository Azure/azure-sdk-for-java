package com.microsoft.windowsazure.services.serviceBus;

import javax.ws.rs.core.MediaType;
import javax.xml.datatype.Duration;

import com.microsoft.windowsazure.services.serviceBus.implementation.Content;
import com.microsoft.windowsazure.services.serviceBus.implementation.Entry;
import com.microsoft.windowsazure.services.serviceBus.implementation.EntryModel;
import com.microsoft.windowsazure.services.serviceBus.implementation.RuleDescription;
import com.microsoft.windowsazure.services.serviceBus.implementation.SubscriptionDescription;

public class Subscription extends EntryModel<SubscriptionDescription> {

    public Subscription() {
        super(new Entry(), new SubscriptionDescription());
        getEntry().setContent(new Content());
        getEntry().getContent().setType(MediaType.APPLICATION_XML);
        getEntry().getContent().setSubscriptionDescription(getModel());
    }

    public Subscription(Entry entry) {
        super(entry, entry.getContent().getSubscriptionDescription());
    }

    public Subscription(String name) {
        this();
        setName(name);
    }

    public String getName() {
        return getEntry().getTitle();
    }

    public Subscription setName(String value) {
        getEntry().setTitle(value);
        return this;
    }

    public Duration getLockDuration() {
        return getModel().getLockDuration();
    }

    public Subscription setLockDuration(Duration value) {
        getModel().setLockDuration(value);
        return this;
    }

    public Boolean isRequiresSession() {
        return getModel().isRequiresSession();
    }

    public Subscription setRequiresSession(Boolean value) {
        getModel().setRequiresSession(value);
        return this;
    }

    public Duration getDefaultMessageTimeToLive() {
        return getModel().getDefaultMessageTimeToLive();
    }

    public Subscription setDefaultMessageTimeToLive(Duration value) {
        getModel().setDefaultMessageTimeToLive(value);
        return this;
    }

    public Boolean isDeadLetteringOnMessageExpiration() {
        return getModel().isDeadLetteringOnMessageExpiration();
    }

    public Subscription setDeadLetteringOnMessageExpiration(Boolean value) {
        getModel().setDeadLetteringOnMessageExpiration(value);
        return this;
    }

    public Boolean isDeadLetteringOnFilterEvaluationExceptions() {
        return getModel().isDeadLetteringOnFilterEvaluationExceptions();
    }

    public Subscription setDeadLetteringOnFilterEvaluationExceptions(Boolean value) {
        getModel().setDeadLetteringOnFilterEvaluationExceptions(value);
        return this;
    }

    public RuleDescription getDefaultRuleDescription() {
        return getModel().getDefaultRuleDescription();
    }

    public Subscription setDefaultRuleDescription(RuleDescription value) {
        getModel().setDefaultRuleDescription(value);
        return this;
    }

    public Long getMessageCount() {
        return getModel().getMessageCount();
    }

    public Subscription setMessageCount(Long value) {
        getModel().setMessageCount(value);
        return this;
    }

    public Integer getMaxDeliveryCount() {
        return getModel().getMaxDeliveryCount();
    }

    public Subscription setMaxDeliveryCount(Integer value) {
        getModel().setMaxDeliveryCount(value);
        return this;
    }

    public Boolean isEnableBatchedOperations() {
        return getModel().isEnableBatchedOperations();
    }

    public Subscription setEnableBatchedOperations(Boolean value) {
        getModel().setEnableBatchedOperations(value);
        return this;
    }
}
