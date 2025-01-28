// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.implementation.eventprocessor.EventAwaiter;
import com.azure.communication.callautomation.implementation.eventprocessor.EventAwaiterOngoing;
import com.azure.communication.callautomation.implementation.eventprocessor.EventAwaiterSingleTime;
import com.azure.communication.callautomation.implementation.eventprocessor.EventBacklog;
import com.azure.communication.callautomation.implementation.eventprocessor.EventWithBacklogId;
import com.azure.communication.callautomation.implementation.eventprocessor.OngoingEventAwaiterKey;
import com.azure.communication.callautomation.models.events.CallAutomationEventBase;
import com.azure.communication.callautomation.models.events.CallDisconnected;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Call Automation's EventProcessor for incoming events for ease of use.
 */
public final class CallAutomationEventProcessor {
    private final EventBacklog eventBacklog;
    private final HashSet<EventAwaiter> eventAwaiters;
    private static final ClientLogger LOGGER = new ClientLogger(CallAutomationEventProcessor.class);
    private static final int DEFAULT_EVENT_AWAITER_EXPIRATION_SECONDS = 240;

    // Key: Map entry of ConnectionId and Event class, Value: ongoingEventAwaiter
    // Used to keep track of all ongoing event awaiters.
    private final ConcurrentHashMap<OngoingEventAwaiterKey<?>, EventAwaiterOngoing<?>> ongoingEventAwaiters;

    /**
     * Constructor of the event processor
     */
    CallAutomationEventProcessor() {
        eventBacklog = new EventBacklog();
        eventAwaiters = new HashSet<>();
        ongoingEventAwaiters = new ConcurrentHashMap<>();
    }

    /**
     * Process incoming events. Pass incoming events to get it processed to have other method like WaitForEventProcessor to function.
     * @param requestBody Incoming event request's body in string.
     */
    public void processEvents(String requestBody) {
        List<CallAutomationEventBase> receivedEvents = CallAutomationEventParser.parseEvents(requestBody);
        if (receivedEvents.isEmpty()) {
            return;
        }
        processEvents(receivedEvents);
    }

    /**
     * Process incoming events. Pass incoming events to get it processed to have other method like WaitForEventProcessor to function.
     * @param events Incoming event in a list that after CallAutomationEventParser processing.
     */
    public void processEvents(List<CallAutomationEventBase> events) {
        // Note: There will always be only 1 event coming from the service
        CallAutomationEventBase receivedEvent = events.get(0);
        String backlogEventId = UUID.randomUUID().toString();
        EventWithBacklogId receivedEventWithBacklogId = eventBacklog.addEvent(backlogEventId, receivedEvent);

        // Each event awaiter should be notified and check if this is the event it is looking for.
        eventAwaiters.forEach(eventAwaiter -> {
            eventAwaiter.onEventsReceived(receivedEventWithBacklogId);
        });

        // If this call is about to disconnect, remove all related items in memory
        if (receivedEvent instanceof CallDisconnected) {
            // remove the event from eventBacklog
            eventBacklog.removeEvent(backlogEventId);
            // remove from ongoingevent list
            removeFromOngoingEvent(receivedEvent.getCallConnectionId());
        }
    }

    /**
     * Wait for matching incoming event. This is blocking Call. Returns the event once it arrives in ProcessEvent method.
     * @param connectionId Call connection id of the call.
     * @param operationContext OperationContext of the method.
     * @param eventType The event type that is being waited.
     * @return Returns the event once matching event arrives.
     * @param <TEvent> Any CallAutomation events.
     */
    public <TEvent extends CallAutomationEventBase> TEvent waitForEventProcessor(String connectionId,
        String operationContext, Class<TEvent> eventType) {
        return waitForEventProcessorAsync(connectionId, operationContext, eventType).block();
    }

    /**
     * Wait for matching incoming event. This is blocking Call. Returns the event once it arrives in ProcessEvent method.
     * @param connectionId Call connection id of the call.
     * @param operationContext OperationContext of the method.
     * @param eventType The event type that is being waited.
     * @param timeout The timeout duration for the event to arrive.
     * @return Returns the event once matching event arrives.
     * @param <TEvent> Any CallAutomation events.
     */
    public <TEvent extends CallAutomationEventBase> TEvent waitForEventProcessor(String connectionId,
        String operationContext, Class<TEvent> eventType, Duration timeout) {
        return waitForEventProcessorAsync(connectionId, operationContext, eventType, timeout).block();
    }

    /**
     * Wait for matching incoming event. This is blocking Call. Returns the event once it arrives in ProcessEvent method.
     * @param predicate Predicate for waiting on event.
     * @return Returns the event once matching event arrives.
     */
    public CallAutomationEventBase waitForEventProcessor(Predicate<CallAutomationEventBase> predicate) {
        return waitForEventProcessorAsync(predicate).block();
    }

    /**
     * Wait for matching incoming event. This is blocking Call. Returns the event once it arrives in ProcessEvent method.
     * @param predicate Predicate for waiting on event.
     * @param timeout The timeout duration for the event to arrive.
     * @return Returns the event once matching event arrives.
     */
    public CallAutomationEventBase waitForEventProcessor(Predicate<CallAutomationEventBase> predicate,
        Duration timeout) {
        return waitForEventProcessorAsync(predicate, timeout).block();
    }

    /**
     * Wait for matching incoming event. Returns the event once it arrives in ProcessEvent method.
     * @param connectionId Call connection id of the call.
     * @param operationContext OperationContext of the method.
     * @param eventType The event type that is being waited.
     * @return Returns the event once matching event arrives.
     * @param <TEvent> Any CallAutomation events.
     */
    @SuppressWarnings("unchecked")
    public <TEvent extends CallAutomationEventBase> Mono<TEvent> waitForEventProcessorAsync(String connectionId,
        String operationContext, Class<TEvent> eventType) {
        return waitForEventProcessorAsync(connectionId, operationContext, eventType,
            Duration.ofSeconds(DEFAULT_EVENT_AWAITER_EXPIRATION_SECONDS));
    }

    /**
     * Wait for matching incoming event. Returns the event once it arrives in ProcessEvent method.
     * @param connectionId Call connection id of the call.
     * @param operationContext OperationContext of the method.
     * @param eventType The event type that is being waited.
     * @param timeout The timeout duration for the event to arrive.
     * @return Returns the event once matching event arrives.
     * @param <TEvent> Any CallAutomation events.
     */
    @SuppressWarnings("unchecked")
    public <TEvent extends CallAutomationEventBase> Mono<TEvent> waitForEventProcessorAsync(String connectionId,
        String operationContext, Class<TEvent> eventType, Duration timeout) {
        Mono<CallAutomationEventBase> ret = waitForEventProcessorAsync(
            event -> (Objects.equals(event.getCallConnectionId(), connectionId) || Objects.isNull(connectionId))
                && (Objects.equals(event.getOperationContext(), operationContext) || Objects.isNull(operationContext))
                && event.getClass() == eventType,
            timeout);

        return ret.map(event -> event == null ? null : (TEvent) event);
    }

    /**
     * Wait for matching incoming event. Returns the event once it arrives in ProcessEvent method.
     * @param predicate Predicate for waiting on event.
     * @return Returns the event once matching event arrives.
     * @throws RuntimeException all checked exceptions if the logic fails.
     */
    public Mono<CallAutomationEventBase> waitForEventProcessorAsync(Predicate<CallAutomationEventBase> predicate) {
        return waitForEventProcessorAsync(predicate, Duration.ofSeconds(DEFAULT_EVENT_AWAITER_EXPIRATION_SECONDS));
    }

    /**
     * Wait for matching incoming event. Returns the event once it arrives in ProcessEvent method.
     * @param predicate Predicate for waiting on event.
     * @param timeout The timeout duration for the event to arrive.
     * @return Returns the event once matching event arrives.
     * @throws RuntimeException all checked exceptions if the logic fails.
     */
    public Mono<CallAutomationEventBase> waitForEventProcessorAsync(Predicate<CallAutomationEventBase> predicate,
        Duration timeout) {
        // Initialize awaiter
        EventAwaiterSingleTime eventAwaiterSingleTime = new EventAwaiterSingleTime(predicate);

        try {
            // Queue it into the subscriber list.
            eventAwaiters.add(eventAwaiterSingleTime);

            EventWithBacklogId eventWithBacklogId = eventBacklog.tryGetAndRemoveMatchedEvent(predicate);

            if (eventWithBacklogId == null) {
                // If event comes to backlog later before the awaiter expires, ret will get that event, otherwise a timeout exception will throw.
                Mono<EventWithBacklogId> futureEventWithBacklogId
                    = eventAwaiterSingleTime.getEventWithBacklogId().timeout(timeout);
                futureEventWithBacklogId.subscribe(event -> {
                    eventBacklog.removeEvent(event.getBackLogEventId());
                    // Remove the awaiter since the event is found or the awaiter is expired.
                    eventAwaiters.remove(eventAwaiterSingleTime);
                    eventAwaiterSingleTime.close();
                });
                return futureEventWithBacklogId.map(EventWithBacklogId::getEvent);
            } else {
                // Remove the awaiter since the event is found or the awaiter is expired.
                eventAwaiters.remove(eventAwaiterSingleTime);
                eventAwaiterSingleTime.close();
                return Mono.just(eventWithBacklogId.getEvent());
            }
        } catch (Exception e) {
            if (!eventAwaiterSingleTime.isDisposed()) {
                eventAwaiters.remove(eventAwaiterSingleTime);
                eventAwaiterSingleTime.close();
            }
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    /**
     * Attach Ongoing EventProcessor for specific event.
     * @param callConnectionId The call connection id of the call.
     * @param eventProcessor EventProcessor to be fired when the specified event arrives.
     * @param eventType The event type that is being waited.
     * @param <TEvent> Any CallAutomation events.
     */
    public <TEvent extends CallAutomationEventBase> void attachOngoingEventProcessor(String callConnectionId,
        Consumer<TEvent> eventProcessor, Class<TEvent> eventType) {
        EventAwaiterOngoing<TEvent> eventAwaiterOngoing
            = new EventAwaiterOngoing<>(eventType, callConnectionId, eventProcessor);

        try {
            // Queue it into the subscriber list.
            eventAwaiters.add(eventAwaiterOngoing);
            ongoingEventAwaiters.put(new OngoingEventAwaiterKey<>(callConnectionId, eventType), eventAwaiterOngoing);
        } catch (Exception e) {
            if (!eventAwaiterOngoing.isDisposed()) {
                OngoingEventAwaiterKey<?> removalKey = ongoingEventAwaiters.searchKeys(1, key -> {
                    if (Objects.equals(key.getCallConnectionId(), callConnectionId) && key.getClazz() == eventType) {
                        return key;
                    }
                    return null;
                });

                if (removalKey != null) {
                    ongoingEventAwaiters.remove(removalKey);
                    eventAwaiters.remove(eventAwaiterOngoing);
                }
            }
        }
    }

    /**
     * Detach Ongoing EventProcessor for specific event.
     * @param callConnectionId The call connection id of the call.
     * @param eventType The event type that is being waited.
     * @param <TEvent> Any CallAutomation events.
     */
    public <TEvent extends CallAutomationEventBase> void detachOngoingEventProcessor(String callConnectionId,
        Class<TEvent> eventType) {
        removeFromOngoingEvent(callConnectionId, eventType);
    }

    private <TEvent extends CallAutomationEventBase> void removeFromOngoingEvent(String connectionId) {
        removeFromOngoingEvent(connectionId, null);
    }

    private <TEvent extends CallAutomationEventBase> void removeFromOngoingEvent(String connectionId,
        Class<TEvent> clazz) {
        if (clazz == null) {
            // Remove all matching connectionId
            List<OngoingEventAwaiterKey<?>> keysToRemove = new ArrayList<>();
            ongoingEventAwaiters.forEachKey(1, key -> {
                if (Objects.equals(key.getCallConnectionId(), connectionId)) {
                    keysToRemove.add(key);
                }
            });
            keysToRemove.forEach(key -> {
                EventAwaiterOngoing<?> awaiterOngoingToBeRemoved = ongoingEventAwaiters.remove(key);
                eventAwaiters.remove(awaiterOngoingToBeRemoved);
            });
        } else {
            OngoingEventAwaiterKey<?> removalKey = ongoingEventAwaiters.searchKeys(1, key -> {
                if (Objects.equals(key.getCallConnectionId(), connectionId) && key.getClazz() == clazz) {
                    return key;
                }
                return null;
            });

            if (removalKey != null) {
                EventAwaiterOngoing<?> awaiterOngoingToBeRemoved = ongoingEventAwaiters.remove(removalKey);
                eventAwaiters.remove(awaiterOngoingToBeRemoved);
            }
        }
    }
}
