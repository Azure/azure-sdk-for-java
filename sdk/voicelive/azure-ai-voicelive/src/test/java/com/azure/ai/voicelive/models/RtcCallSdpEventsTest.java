// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.models;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the WebRTC SDP negotiation client/server events and the related
 * {@link RtcCallErrorDetails} payload.
 */
class RtcCallSdpEventsTest {

    @Test
    void testRtcCallEventTypesRegistered() {
        assertEquals("rtc.call.sdp.create", ClientEventType.RTC_CALL_SDP_CREATE.toString());
        assertEquals("rtc.call.sdp.created", ServerEventType.RTC_CALL_SDP_CREATED.toString());
        assertEquals("rtc.call.error", ServerEventType.RTC_CALL_ERROR.toString());
    }

    @Test
    void testClientEventRtcCallSdpCreateRoundTrip() {
        ClientEventRtcCallSdpCreate event = new ClientEventRtcCallSdpCreate("v=0\r\no=- 1 1 IN IP4 0.0.0.0\r\n")
            .setSession(new VoiceLiveSessionOptions().setModel("gpt-4o-realtime-preview"));
        event.setEventId("c1");

        ClientEventRtcCallSdpCreate deserialized
            = BinaryData.fromObject(event).toObject(ClientEventRtcCallSdpCreate.class);

        assertEquals(ClientEventType.RTC_CALL_SDP_CREATE, deserialized.getType());
        assertEquals("c1", deserialized.getEventId());
        assertEquals(event.getSdpOffer(), deserialized.getSdpOffer());
        assertNotNull(deserialized.getSession());
        assertEquals("gpt-4o-realtime-preview", deserialized.getSession().getModel());
    }

    @Test
    void testClientEventRtcCallSdpCreatePolymorphicViaSessionClientEvent() {
        String json = "{\"type\":\"rtc.call.sdp.create\",\"event_id\":\"c2\",\"sdp_offer\":\"v=0\"}";

        SessionClientEvent event = BinaryData.fromString(json).toObject(SessionClientEvent.class);

        assertTrue(event instanceof ClientEventRtcCallSdpCreate,
            "Expected ClientEventRtcCallSdpCreate, got " + event.getClass());
        assertEquals("v=0", ((ClientEventRtcCallSdpCreate) event).getSdpOffer());
    }

    @Test
    void testServerEventRtcCallSdpCreatedDeserialization() {
        String json = "{\"type\":\"rtc.call.sdp.created\",\"event_id\":\"s1\","
            + "\"rtc_call_id\":\"call-123\",\"sdp_answer\":\"v=0\\r\\no=- 2 2 IN IP4 0.0.0.0\\r\\n\"}";

        SessionServerEvent event = BinaryData.fromString(json).toObject(SessionServerEvent.class);

        assertTrue(event instanceof ServerEventRtcCallSdpCreated);
        ServerEventRtcCallSdpCreated sdpCreated = (ServerEventRtcCallSdpCreated) event;
        assertEquals(ServerEventType.RTC_CALL_SDP_CREATED, sdpCreated.getType());
        assertEquals("s1", sdpCreated.getEventId());
        assertEquals("call-123", sdpCreated.getRtcCallId());
        assertTrue(sdpCreated.getSdpAnswer().startsWith("v=0"));
    }

    @Test
    void testServerEventRtcCallErrorDeserialization() {
        String json = "{\"type\":\"rtc.call.error\",\"event_id\":\"s2\","
            + "\"operation\":\"rtc.call.sdp.create\",\"rtc_call_id\":\"call-9\","
            + "\"error\":{\"type\":\"invalid_request_error\",\"code\":\"bad_sdp\",\"message\":\"Malformed SDP\"}}";

        SessionServerEvent event = BinaryData.fromString(json).toObject(SessionServerEvent.class);

        assertTrue(event instanceof ServerEventRtcCallError);
        ServerEventRtcCallError err = (ServerEventRtcCallError) event;
        assertEquals(ServerEventType.RTC_CALL_ERROR, err.getType());
        assertEquals("rtc.call.sdp.create", err.getOperation());
        assertEquals("call-9", err.getRtcCallId());

        RtcCallErrorDetails details = err.getError();
        assertNotNull(details);
        assertEquals("invalid_request_error", details.getType());
        assertEquals("bad_sdp", details.getCode());
        assertEquals("Malformed SDP", details.getMessage());
    }
}
