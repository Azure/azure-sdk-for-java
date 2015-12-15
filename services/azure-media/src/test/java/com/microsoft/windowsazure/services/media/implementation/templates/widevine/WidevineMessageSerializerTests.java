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
    	message.allowed_track_types = AllowedTrackTypes.SD_HD;
    	ContentKeySpecs ckspecs = new ContentKeySpecs();
    	message.content_key_specs = new ContentKeySpecs[] { ckspecs };
    	ckspecs.required_output_protection = new RequiredOutputProtection();
    	ckspecs.required_output_protection.hdcp = Hdcp.HDCP_NONE;
    	ckspecs.security_level = 1;
    	ckspecs.track_type = "SD";
    	message.policy_overrides =  new Object() {
    		  public boolean can_play = true;
    		  public boolean can_persist = true;
    		  public boolean can_renew  = false;
    	};
    	
    	String json = mapper.writeValueAsString(message);
    	
    	WidevineMessage result = mapper.readValue(json, WidevineMessage.class);
    	
    	assertEqualsWidevineMessage(message, result);   	
    }
    
    @Test
    public void FromJsonTest() throws JAXBException, URISyntaxException, JsonProcessingException {
        String expected = "{\"allowed_track_types\":\"SD_HD\",\"content_key_specs\":[{\"track_type\":\"SD\",\"key_id\":null,\"security_level\":1,\"required_output_protection\":{\"hdcp\":\"HDCP_NONE\"}}],\"policy_overrides\":{\"can_play\":true,\"can_persist\":true,\"can_renew\":false}}";
    	ObjectMapper mapper = new ObjectMapper();
    	WidevineMessage message = new WidevineMessage();
    	message.allowed_track_types = AllowedTrackTypes.SD_HD;
    	ContentKeySpecs ckspecs = new ContentKeySpecs();
    	message.content_key_specs = new ContentKeySpecs[] { ckspecs };
    	ckspecs.required_output_protection = new RequiredOutputProtection();
    	ckspecs.required_output_protection.hdcp = Hdcp.HDCP_NONE;
    	ckspecs.security_level = 1;
    	ckspecs.track_type = "SD";
    	message.policy_overrides =  new Object() {
    		  public boolean can_play = true;
    		  public boolean can_persist = true;
    		  public boolean can_renew  = false;
    	};
    	
    	String json = mapper.writeValueAsString(message);
    	
    	assertEquals(expected, json);   	
    }
    
    private static void assertEqualsWidevineMessage(WidevineMessage expected, WidevineMessage actual) {
    	assertEquals(expected.allowed_track_types, actual.allowed_track_types);
    	if (expected.content_key_specs == null) {
    		assertNull(actual.content_key_specs);
    	} else {
    		assertNotNull(actual.content_key_specs);
    		assertEquals(expected.content_key_specs.length, actual.content_key_specs.length);
    		for(int i = 0; i < expected.content_key_specs.length; i++) {
    			ContentKeySpecs expectedCks = expected.content_key_specs[i];
    			ContentKeySpecs actualCks = actual.content_key_specs[i];
    			assertEquals(expectedCks.key_id, actualCks.key_id);
    			assertEquals(expectedCks.security_level, actualCks.security_level);
    			assertEquals(expectedCks.track_type, actualCks.track_type);
    			if (expectedCks.required_output_protection != null) {
    				assertNotNull(actualCks.required_output_protection);
    				assertEquals(expectedCks.required_output_protection.hdcp, actualCks.required_output_protection.hdcp);
    			} else {
    				assertNull(actualCks.required_output_protection);
    			}
    			assertEquals(expectedCks.key_id, actualCks.key_id);    			
    		}
    	}
    	if (expected.policy_overrides == null) {
    		assertNull(actual.policy_overrides);
    	} else {
    		assertNotNull(actual.policy_overrides);
    	}
    }
}
