package com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense;

import static org.junit.Assert.*;

import org.junit.Test;

import com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense.AgcAndColorStripeRestriction;
import com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense.ExplicitAnalogTelevisionRestriction;
import com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense.PlayReadyPlayRight;
import com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense.UnknownOutputPassingOption;

public class PlayReadyPlayRightTest {
    
    @Test
    public void RoundTripTest() throws Exception {
        // Arrange
        PlayReadyPlayRight playRight = new PlayReadyPlayRight();
        // Act
        playRight.setAgcAndColorStripeRestriction(new AgcAndColorStripeRestriction((byte) 0));
        playRight.setAllowPassingVideoContentToUnknownOutput(UnknownOutputPassingOption.NotAllowed);
        playRight.setAnalogVideoOpl(100);
        playRight.setCompressedDigitalAudioOpl(200);
        playRight.setCompressedDigitalVideoOpl(400);
        playRight.setDigitalVideoOnlyContentRestriction(true);
        playRight.setExplicitAnalogTelevisionOutputRestriction(new ExplicitAnalogTelevisionRestriction(true, (byte)0));
        // Test
        assertEquals(playRight.getAgcAndColorStripeRestriction().getConfigurationData(), (byte)0);
        assertEquals(playRight.getAllowPassingVideoContentToUnknownOutput(), UnknownOutputPassingOption.NotAllowed);
    }

}
