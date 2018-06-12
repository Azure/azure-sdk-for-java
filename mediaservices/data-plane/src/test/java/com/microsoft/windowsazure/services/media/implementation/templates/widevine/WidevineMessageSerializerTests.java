package com.microsoft.windowsazure.services.media.implementation.templates.widevine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.xml.bind.JAXBException;

import org.junit.Test;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WidevineMessageSerializerTests {

    @Test
    public void RoundTripTest() throws JAXBException, URISyntaxException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        WidevineMessage message = new WidevineMessage();
        message.setAllowedTrackTypes(AllowedTrackTypes.SD_HD);
        ContentKeySpecs ckspecs = new ContentKeySpecs();
        message.setContentKeySpecs(new ContentKeySpecs[] { ckspecs });
        ckspecs.setRequiredOutputProtection(new RequiredOutputProtection());
        ckspecs.getRequiredOutputProtection().setHdcp(Hdcp.HDCP_NONE);
        ckspecs.setSecurityLevel(1);
        ckspecs.setTrackType("SD");
        message.setPolicyOverrides(new Object() {
            public boolean can_play = true;
            public boolean can_persist = true;
            public boolean can_renew = false;
        });

        String json = mapper.writeValueAsString(message);

        WidevineMessage result = mapper.readValue(json, WidevineMessage.class);

        assertEqualsWidevineMessage(message, result);
    }

    @Test
    public void FromJsonTest() throws JAXBException, URISyntaxException, JsonProcessingException {
        String expected = "{\"allowed_track_types\":\"SD_HD\",\"content_key_specs\":[{\"track_type\":\"SD\",\"key_id\":null,\"security_level\":1,\"required_output_protection\":{\"hdcp\":\"HDCP_NONE\"}}],\"policy_overrides\":{\"can_play\":true,\"can_persist\":true,\"can_renew\":false}}";
        ObjectMapper mapper = new ObjectMapper();
        WidevineMessage message = new WidevineMessage();
        message.setAllowedTrackTypes(AllowedTrackTypes.SD_HD);
        ContentKeySpecs ckspecs = new ContentKeySpecs();
        message.setContentKeySpecs(new ContentKeySpecs[] { ckspecs });
        ckspecs.setRequiredOutputProtection(new RequiredOutputProtection());
        ckspecs.getRequiredOutputProtection().setHdcp(Hdcp.HDCP_NONE);
        ckspecs.setSecurityLevel(1);
        ckspecs.setTrackType("SD");
        message.setPolicyOverrides(new Object() {
            public boolean can_play = true;
            public boolean can_persist = true;
            public boolean can_renew = false;
        });

        String json = mapper.writeValueAsString(message);

        assertEquals(expected, json);
    }

    private static void assertEqualsWidevineMessage(WidevineMessage expected, WidevineMessage actual) {
        assertEquals(expected.getAllowedTrackTypes(), actual.getAllowedTrackTypes());
        if (expected.getContentKeySpecs() == null) {
            assertNull(actual.getContentKeySpecs());
        } else {
            assertNotNull(actual.getContentKeySpecs());
            assertEquals(expected.getContentKeySpecs().length, actual.getContentKeySpecs().length);
            for (int i = 0; i < expected.getContentKeySpecs().length; i++) {
                ContentKeySpecs expectedCks = expected.getContentKeySpecs()[i];
                ContentKeySpecs actualCks = actual.getContentKeySpecs()[i];
                assertEquals(expectedCks.getKeyId(), actualCks.getKeyId());
                assertEquals(expectedCks.getSecurityLevel(), actualCks.getSecurityLevel());
                assertEquals(expectedCks.getTrackType(), actualCks.getTrackType());
                if (expectedCks.getRequiredOutputProtection() != null) {
                    assertNotNull(actualCks.getRequiredOutputProtection());
                    assertEquals(expectedCks.getRequiredOutputProtection().getHdcp(),
                            actualCks.getRequiredOutputProtection().getHdcp());
                } else {
                    assertNull(actualCks.getRequiredOutputProtection());
                }
                assertEquals(expectedCks.getKeyId(), actualCks.getKeyId());
            }
        }
        if (expected.getPolicyOverrides() == null) {
            assertNull(actual.getPolicyOverrides());
        } else {
            assertNotNull(actual.getPolicyOverrides());
        }
    }
}
