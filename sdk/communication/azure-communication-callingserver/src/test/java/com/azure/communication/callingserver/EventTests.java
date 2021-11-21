// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.models.AddParticipantResultEventInternal;
import com.azure.communication.callingserver.implementation.models.CallConnectionStateChangedEventInternal;
import com.azure.communication.callingserver.implementation.models.CallParticipantInternal;
import com.azure.communication.callingserver.implementation.models.CallRecordingStateChangeEventInternal;
import com.azure.communication.callingserver.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.callingserver.implementation.models.ParticipantsUpdatedEventInternal;
import com.azure.communication.callingserver.implementation.models.PhoneNumberIdentifierModel;
import com.azure.communication.callingserver.implementation.models.PlayAudioResultEventInternal;
import com.azure.communication.callingserver.implementation.models.ResultInfoInternal;
import com.azure.communication.callingserver.implementation.models.ToneInfoInternal;
import com.azure.communication.callingserver.implementation.models.ToneReceivedEventInternal;
import com.azure.communication.callingserver.models.CallConnectionState;
import com.azure.communication.callingserver.models.CallRecordingState;
import com.azure.communication.callingserver.models.OperationStatus;
import com.azure.communication.callingserver.models.ToneValue;
import com.azure.communication.callingserver.models.events.AddParticipantResultEvent;
import com.azure.communication.callingserver.models.events.CallConnectionStateChangedEvent;
import com.azure.communication.callingserver.models.events.CallRecordingStateChangeEvent;
import com.azure.communication.callingserver.models.events.ParticipantsUpdatedEvent;
import com.azure.communication.callingserver.models.events.PlayAudioResultEvent;
import com.azure.communication.callingserver.models.events.ToneReceivedEvent;
import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EventTests {
    @Test
    public void callConnectionStateChangedEventTest() throws JsonProcessingException {
        String serverCallId = UUID.randomUUID().toString();
        String callConnectionId = UUID.randomUUID().toString();
        CallConnectionStateChangedEventInternal internalEvent =
            new CallConnectionStateChangedEventInternal()
                .setCallConnectionId(callConnectionId)
                .setServerCallId(serverCallId)
                .setCallConnectionState(CallConnectionState.CONNECTED);

        BinaryData binaryData = getBinaryData(internalEvent);
        CallConnectionStateChangedEvent event =
            assertDoesNotThrow(() -> CallConnectionStateChangedEvent.deserialize(binaryData));

        assertNotNull(event);
        assertEquals(event.getCallConnectionId(), callConnectionId);
        assertEquals(event.getServerCallId(), serverCallId);
        assertEquals(event.getCallConnectionState(), CallConnectionState.CONNECTED);
    }

    @Test
    public void addParticipantResultEventTest() throws JsonProcessingException {
        String operationContext = UUID.randomUUID().toString();
        String message = "Participant added.";
        AddParticipantResultEventInternal internalEvent =
            new AddParticipantResultEventInternal()
                .setOperationContext(operationContext)
                .setStatus(OperationStatus.COMPLETED)
                .setResultInfo(new ResultInfoInternal().setCode(100).setSubcode(200).setMessage(message));

        BinaryData binaryData = getBinaryData(internalEvent);
        AddParticipantResultEvent event =
            assertDoesNotThrow(() -> AddParticipantResultEvent.deserialize(binaryData));

        assertNotNull(event);
        assertEquals(event.getOperationContext(), operationContext);
        assertEquals(event.getStatus(), OperationStatus.COMPLETED);
        assertNotNull(event.getResultInfo());
        assertEquals(event.getResultInfo().getCode(), 100);
        assertEquals(event.getResultInfo().getSubcode(), 200);
        assertEquals(event.getResultInfo().getMessage(), message);
    }

    @Test
    public void callRecordingStateChangeEventTest() throws JsonProcessingException {
        String serverCallId = UUID.randomUUID().toString();
        String recordingId = UUID.randomUUID().toString();
        CallRecordingStateChangeEventInternal internalEvent =
            new CallRecordingStateChangeEventInternal()
                .setServerCallId(serverCallId)
                .setRecordingId(recordingId)
                .setState(CallRecordingState.ACTIVE)
                .setStartDateTime(OffsetDateTime.MIN);

        BinaryData binaryData = getBinaryData(internalEvent);
        CallRecordingStateChangeEvent event =
            assertDoesNotThrow(() -> CallRecordingStateChangeEvent.deserialize(binaryData));

        assertNotNull(event);
        assertEquals(event.getRecordingId(), recordingId);
        assertEquals(event.getServerCallId(), serverCallId);
        assertEquals(event.getState(), CallRecordingState.ACTIVE);
        assertEquals(event.getStartDateTime(), OffsetDateTime.MIN);
    }

    @Test
    public void playAudioResultEventTest() throws JsonProcessingException {
        String operationContext = UUID.randomUUID().toString();
        String message = "Media operation executed.";
        PlayAudioResultEventInternal internalEvent =
            new PlayAudioResultEventInternal()
                .setOperationContext(operationContext)
                .setStatus(OperationStatus.COMPLETED)
                .setResultInfo(new ResultInfoInternal().setCode(100).setSubcode(200).setMessage(message));

        BinaryData binaryData = getBinaryData(internalEvent);
        PlayAudioResultEvent event =
            assertDoesNotThrow(() -> PlayAudioResultEvent.deserialize(binaryData));

        assertNotNull(event);
        assertEquals(event.getOperationContext(), operationContext);
        assertEquals(event.getStatus(), OperationStatus.COMPLETED);
        assertNotNull(event.getResultInfo());
        assertEquals(event.getResultInfo().getCode(), 100);
        assertEquals(event.getResultInfo().getSubcode(), 200);
        assertEquals(event.getResultInfo().getMessage(), message);
    }

    @Test
    public void toneReceivedEventTest() throws JsonProcessingException {
        String callConnectionId = UUID.randomUUID().toString();
        ToneReceivedEventInternal internalEvent =
            new ToneReceivedEventInternal()
                .setCallConnectionId(callConnectionId)
                .setToneInfo(new ToneInfoInternal()
                    .setSequenceId(1)
                    .setTone(ToneValue.TONE1));

        BinaryData binaryData = getBinaryData(internalEvent);
        ToneReceivedEvent event =
            assertDoesNotThrow(() -> ToneReceivedEvent.deserialize(binaryData));

        assertNotNull(event);
        assertEquals(event.getCallConnectionId(), callConnectionId);
        assertNotNull(event.getToneInfo());
        assertEquals(event.getToneInfo().getSequenceId(), 1);
        assertEquals(event.getToneInfo().getTone(), ToneValue.TONE1);
    }

    @Test
    public void participantsUpdatedEventTest() throws JsonProcessingException {
        String callConnectionId = UUID.randomUUID().toString();
        String participantId = UUID.randomUUID().toString();
        String phoneNumber = "+18881112222";
        ParticipantsUpdatedEventInternal internalEvent =
            new ParticipantsUpdatedEventInternal()
                .setCallConnectionId(callConnectionId)
                .setParticipants(
                    new LinkedList<>(Collections.singletonList(
                        new CallParticipantInternal()
                            .setParticipantId(participantId)
                            .setIsMuted(false)
                            .setIdentifier(new CommunicationIdentifierModel()
                                .setPhoneNumber(new PhoneNumberIdentifierModel().setValue(phoneNumber))))));

        BinaryData binaryData = getBinaryData(internalEvent);
        ParticipantsUpdatedEvent event =
            assertDoesNotThrow(() -> ParticipantsUpdatedEvent.deserialize(binaryData));

        assertNotNull(event);
        assertEquals(event.getCallConnectionId(), callConnectionId);
        assertNotNull(event.getParticipants());
        assertEquals(1, event.getParticipants().size());
        assertEquals(event.getParticipants().get(0).getParticipantId(), participantId);
        assertFalse(event.getParticipants().get(0).isMuted());
        assertEquals(((PhoneNumberIdentifier) event.getParticipants().get(0).getIdentifier()).getPhoneNumber(), phoneNumber);
    }

    private BinaryData getBinaryData(Object eventObject) throws JsonProcessingException {
        ObjectWriter objectWriter = getObjectMapper().writer().withDefaultPrettyPrinter();
        String jsonData = objectWriter.writeValueAsString(eventObject);
        return BinaryData.fromString(jsonData);
    }

    private ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(OffsetDateTime.class, new JsonSerializer<OffsetDateTime>() {
            @Override
            public void serialize(
                OffsetDateTime offsetDateTime,
                JsonGenerator jsonGenerator,
                SerializerProvider serializerProvider) throws IOException {
                jsonGenerator.writeString(offsetDateTime.toString());
            }
        });
        simpleModule.addDeserializer(OffsetDateTime.class, new JsonDeserializer<OffsetDateTime>() {
            @Override
            public OffsetDateTime deserialize(
                JsonParser jsonParser,
                DeserializationContext deserializationContext) throws IOException {
                return OffsetDateTime.parse(jsonParser.getText());
            }
        });
        objectMapper.registerModule(simpleModule);
        return objectMapper;
    }
}
