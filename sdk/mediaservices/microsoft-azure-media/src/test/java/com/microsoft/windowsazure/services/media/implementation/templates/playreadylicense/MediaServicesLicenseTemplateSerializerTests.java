package com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.UUID;
import java.util.Arrays;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.junit.Test;

import com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense.AgcAndColorStripeRestriction;
import com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense.ContentEncryptionKeyFromHeader;
import com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense.ContentEncryptionKeyFromKeyIdentifier;
import com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense.ErrorMessages;
import com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense.ExplicitAnalogTelevisionRestriction;
import com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense.MediaServicesLicenseTemplateSerializer;
import com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense.PlayReadyLicenseResponseTemplate;
import com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense.PlayReadyLicenseTemplate;
import com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense.PlayReadyLicenseType;
import com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense.PlayReadyPlayRight;
import com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense.ScmsRestriction;
import com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense.UnknownOutputPassingOption;

public class MediaServicesLicenseTemplateSerializerTests {

    private final String schemaFile = null; // MediaServicesLicenseTemplateSerializerTests.class
            ///  .getClassLoader().getResource("")
            /// .getPath() + "schemas/TokenRestrictionTemplate.xsd";
    
    @Test
    public void roundTripTest() throws Exception {
        PlayReadyLicenseResponseTemplate template = new PlayReadyLicenseResponseTemplate();
        template.setResponseCustomData("This is my response custom data");
        PlayReadyLicenseTemplate licenseTemplate = new PlayReadyLicenseTemplate();
        template.getLicenseTemplates().add(licenseTemplate);

        //@Act
        
        licenseTemplate.setLicenseType(PlayReadyLicenseType.Persistent);
        licenseTemplate.setBeginDate(new Date());
        licenseTemplate.setExpirationDate(new Date());
        licenseTemplate.setContentKey(new ContentEncryptionKeyFromKeyIdentifier(UUID.randomUUID()));
        
        PlayReadyPlayRight playRight = new PlayReadyPlayRight();
        licenseTemplate.setPlayRight(playRight);
        
        playRight.setAgcAndColorStripeRestriction(new AgcAndColorStripeRestriction((byte) 1));
        playRight.setAllowPassingVideoContentToUnknownOutput(UnknownOutputPassingOption.Allowed);
        playRight.setAnalogVideoOpl(100);
        playRight.setCompressedDigitalAudioOpl(300);
        playRight.setCompressedDigitalVideoOpl(400);
        playRight.setExplicitAnalogTelevisionOutputRestriction(new ExplicitAnalogTelevisionRestriction(true, (byte)0));
        playRight.setImageConstraintForAnalogComponentVideoRestriction(true);
        playRight.setImageConstraintForAnalogComputerMonitorRestriction(true);
        playRight.setScmsRestriction(new ScmsRestriction((byte)2));
        playRight.setUncompressedDigitalAudioOpl(250);
        playRight.setUncompressedDigitalVideoOpl(270);
        
        String result = MediaServicesLicenseTemplateSerializer.serialize(template);
        assertNotNull(result);
        
        PlayReadyLicenseResponseTemplate deserialized = MediaServicesLicenseTemplateSerializer.deserialize(result, schemaFile);
        assertNotNull(deserialized);
    }
    
    @Test
    public void roundTripTestWithRelativeBeginDateRelativeEndDate() throws Exception {
        PlayReadyLicenseResponseTemplate template = new PlayReadyLicenseResponseTemplate();
        template.setResponseCustomData("This is my response custom data");
        PlayReadyLicenseTemplate licenseTemplate = new PlayReadyLicenseTemplate();
        template.getLicenseTemplates().add(licenseTemplate);
        
        licenseTemplate.setLicenseType(PlayReadyLicenseType.Persistent);
        licenseTemplate.setRelativeBeginDate(DatatypeFactory.newInstance().newDuration("PT1H"));
        licenseTemplate.setRelativeExpirationDate(DatatypeFactory.newInstance().newDuration("PT1H"));
        licenseTemplate.setContentKey(new ContentEncryptionKeyFromKeyIdentifier(UUID.randomUUID()));
        
        PlayReadyPlayRight playRight = new PlayReadyPlayRight();
        licenseTemplate.setPlayRight(playRight);
        
        playRight.setAgcAndColorStripeRestriction(new AgcAndColorStripeRestriction((byte) 1));
        playRight.setAllowPassingVideoContentToUnknownOutput(UnknownOutputPassingOption.Allowed);
        playRight.setAnalogVideoOpl(100);
        playRight.setCompressedDigitalAudioOpl(300);
        playRight.setCompressedDigitalVideoOpl(400);
        playRight.setExplicitAnalogTelevisionOutputRestriction(new ExplicitAnalogTelevisionRestriction(true, (byte)0));
        playRight.setImageConstraintForAnalogComponentVideoRestriction(true);
        playRight.setImageConstraintForAnalogComputerMonitorRestriction(true);
        playRight.setScmsRestriction(new ScmsRestriction((byte)2));
        playRight.setUncompressedDigitalAudioOpl(250);
        playRight.setUncompressedDigitalVideoOpl(270);
        
        String result = MediaServicesLicenseTemplateSerializer.serialize(template);
        assertNotNull(result);
        
        PlayReadyLicenseResponseTemplate deserialized = MediaServicesLicenseTemplateSerializer.deserialize(result, schemaFile);
        assertNotNull(deserialized);
    }
    
    @Test
    public void roundTripTestErrorWithRelativeBeginDateBeginDate() throws Exception {
        try {
            PlayReadyLicenseResponseTemplate template = new PlayReadyLicenseResponseTemplate();
            template.setResponseCustomData("This is my response custom data");
            PlayReadyLicenseTemplate licenseTemplate = new PlayReadyLicenseTemplate();
            template.getLicenseTemplates().add(licenseTemplate);
            
            licenseTemplate.setLicenseType(PlayReadyLicenseType.Persistent);
            licenseTemplate.setBeginDate(new Date());
            licenseTemplate.setRelativeBeginDate(DatatypeFactory.newInstance().newDuration("PT1H"));
            
            fail("Should Thrown");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("both"));
        }
    }
    
    @Test
    public void roundTripTestErrorWithBeginDateRelativeBeginDate() throws Exception {
        try {
            PlayReadyLicenseResponseTemplate template = new PlayReadyLicenseResponseTemplate();
            template.setResponseCustomData("This is my response custom data");
            PlayReadyLicenseTemplate licenseTemplate = new PlayReadyLicenseTemplate();
            template.getLicenseTemplates().add(licenseTemplate);
            
            licenseTemplate.setLicenseType(PlayReadyLicenseType.Persistent);
            licenseTemplate.setRelativeBeginDate(DatatypeFactory.newInstance().newDuration("PT1H"));
            licenseTemplate.setBeginDate(new Date());
            
            fail("Should Thrown");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("both"));
        }
    }
    
    @Test
    public void roundTripTestErrorWithRelativeExpirationDateExpirationDate() throws Exception {
        try {
            PlayReadyLicenseResponseTemplate template = new PlayReadyLicenseResponseTemplate();
            template.setResponseCustomData("This is my response custom data");
            PlayReadyLicenseTemplate licenseTemplate = new PlayReadyLicenseTemplate();
            template.getLicenseTemplates().add(licenseTemplate);
            
            licenseTemplate.setLicenseType(PlayReadyLicenseType.Persistent);
            licenseTemplate.setExpirationDate(new Date());
            licenseTemplate.setRelativeExpirationDate(DatatypeFactory.newInstance().newDuration("PT1H"));
            
            fail("Should Thrown");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("both"));
        }
    }
    
    @Test
    public void roundTripTestErrorWithExpirationDateRelativeExpirationDate() throws Exception {
        try {
            PlayReadyLicenseResponseTemplate template = new PlayReadyLicenseResponseTemplate();
            template.setResponseCustomData("This is my response custom data");
            PlayReadyLicenseTemplate licenseTemplate = new PlayReadyLicenseTemplate();
            template.getLicenseTemplates().add(licenseTemplate);
            
            licenseTemplate.setLicenseType(PlayReadyLicenseType.Persistent);
            licenseTemplate.setRelativeExpirationDate(DatatypeFactory.newInstance().newDuration("PT1H"));
            licenseTemplate.setExpirationDate(new Date());
            
            fail("Should Thrown");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("both"));
        }
    }
   
    @Test
    public void validateNonPersistentLicenseConstraints() throws Exception {
        // Arrange
        @SuppressWarnings("unused")
        String serializedTemplate = null;
        Duration durationSanmple = DatatypeFactory.newInstance().newDuration("PT1H");
        PlayReadyLicenseResponseTemplate template = new PlayReadyLicenseResponseTemplate();
        PlayReadyLicenseTemplate licenseTemplate = new PlayReadyLicenseTemplate();
        template.getLicenseTemplates().add(licenseTemplate);
        PlayReadyPlayRight playRight = new PlayReadyPlayRight();
        licenseTemplate.setPlayRight(playRight);
        // Set as Nonpersistent
        licenseTemplate.setLicenseType(PlayReadyLicenseType.Nonpersistent);
        
        // ACT 1: Make sure we cannot set GracePeriod on a NonPersistent license
        try
        {
            serializedTemplate = MediaServicesLicenseTemplateSerializer.serialize(template);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(ErrorMessages.PLAY_READY_CONTENT_KEY_REQUIRED, e.getMessage());
            licenseTemplate.setContentKey(new ContentEncryptionKeyFromHeader());
        }
        
        // ACT 2: Make sure we cannot set GracePeriod on a NonPersistent license
        licenseTemplate.setGracePeriod(durationSanmple);
        try
        {
            serializedTemplate = MediaServicesLicenseTemplateSerializer.serialize(template);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(ErrorMessages.GRACE_PERIOD_CANNOT_BE_SET_ON_NON_PERSISTENT_LICENSE, e.getMessage());
        }
        
        // ACT 3: Make sure we cannot set a FirstPlayExpiration on a NonPersistent license.
        licenseTemplate.setGracePeriod(null);
        playRight.setFirstPlayExpiration(durationSanmple);
        
        try
        {
            serializedTemplate = MediaServicesLicenseTemplateSerializer.serialize(template);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(ErrorMessages.FIRST_PLAY_EXPIRATION_CANNOT_BE_SET_ON_NON_PERSISTENT_LICENSE, e.getMessage());
        }
        
        // ACT 4: Make sure we cannot set a BeginDate on a NonPersistent license.
        playRight.setFirstPlayExpiration(null);
        licenseTemplate.setBeginDate(new Date());
        
        try
        {
            serializedTemplate = MediaServicesLicenseTemplateSerializer.serialize(template);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(ErrorMessages.BEGIN_DATE_CANNOT_BE_SET_ON_NON_PERSISTENT_LICENSE, e.getMessage());
        }
        
        // ACT 5: Make sure we cannot set an ExpirationDate on a NonPersistent license.
        licenseTemplate.setBeginDate(null);
        licenseTemplate.setExpirationDate(new Date());
        
        try
        {
            serializedTemplate = MediaServicesLicenseTemplateSerializer.serialize(template);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(ErrorMessages.EXPIRATION_CANNOT_BE_SET_ON_NON_PERSISTENT_LICENSE, e.getMessage());
        }
    }
    
    @Test
    public void digitalVideoOnlyContentRestrictionAndAllowPassingVideoContentToUnknownOutputMutuallyExclusive() throws Exception {
        String serializedTemplate = null;
        PlayReadyLicenseResponseTemplate template = new PlayReadyLicenseResponseTemplate();
        PlayReadyLicenseTemplate licenseTemplate = new PlayReadyLicenseTemplate();
        template.getLicenseTemplates().add(licenseTemplate);
        // Set as Nonpersistent
        licenseTemplate.setLicenseType(PlayReadyLicenseType.Nonpersistent);
        licenseTemplate.setContentKey(new ContentEncryptionKeyFromHeader());
        PlayReadyPlayRight playRight = new PlayReadyPlayRight();
        licenseTemplate.setPlayRight(playRight);
        
        // ACT 1: Make sure we cannot set DigitalVideoOnlyContentRestriction to true if 
        //         UnknownOutputPassingOption.Allowed is set
        playRight.setAllowPassingVideoContentToUnknownOutput(UnknownOutputPassingOption.Allowed);
        playRight.setDigitalVideoOnlyContentRestriction(true);
        try
        {
            serializedTemplate = MediaServicesLicenseTemplateSerializer.serialize(template);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            // ASSERT 1
            assertEquals(ErrorMessages.DIGITAL_VIDEO_ONLY_MUTUALLY_EXCLUSIVE_WITH_PASSING_TO_UNKNOWN_OUTPUT_ERROR, e.getMessage());
        }
        
        // ACT 2: Make sure we cannot set UnknownOutputPassingOption.AllowedWithVideoConstriction
        //         if DigitalVideoOnlyContentRestriction is true
        playRight.setAllowPassingVideoContentToUnknownOutput(UnknownOutputPassingOption.AllowedWithVideoConstriction);
        try
        {
            serializedTemplate = MediaServicesLicenseTemplateSerializer.serialize(template);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            // ASSERT 2
            assertEquals(ErrorMessages.DIGITAL_VIDEO_ONLY_MUTUALLY_EXCLUSIVE_WITH_PASSING_TO_UNKNOWN_OUTPUT_ERROR, e.getMessage());
        }
        
        // ACT 3: Make sure we can set DigitalVideoOnlyContentRestriction to true if 
        //         UnknownOutputPassingOption.NotAllowed is set
        playRight.setAllowPassingVideoContentToUnknownOutput(UnknownOutputPassingOption.NotAllowed);
        serializedTemplate = MediaServicesLicenseTemplateSerializer.serialize(template);
  
        // ASSERT 3
        assertNotNull(serializedTemplate);
        assertNotNull(MediaServicesLicenseTemplateSerializer.deserialize(serializedTemplate));
    }
    
    @Test
    public void knownGoodInputTest() throws Exception
    {
        String serializedTemplate = "<PlayReadyLicenseResponseTemplate xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://schemas.microsoft.com/Azure/MediaServices/KeyDelivery/PlayReadyTemplate/v1\"><LicenseTemplates><PlayReadyLicenseTemplate><AllowTestDevices>false</AllowTestDevices><BeginDate i:nil=\"true\" /><ContentKey i:type=\"ContentEncryptionKeyFromHeader\" /><ContentType>Unspecified</ContentType><ExpirationDate i:nil=\"true\" /><LicenseType>Nonpersistent</LicenseType><PlayRight><AgcAndColorStripeRestriction><ConfigurationData>1</ConfigurationData></AgcAndColorStripeRestriction><AllowPassingVideoContentToUnknownOutput>Allowed</AllowPassingVideoContentToUnknownOutput><AnalogVideoOpl>100</AnalogVideoOpl><CompressedDigitalAudioOpl>300</CompressedDigitalAudioOpl><CompressedDigitalVideoOpl>400</CompressedDigitalVideoOpl><DigitalVideoOnlyContentRestriction>false</DigitalVideoOnlyContentRestriction><ExplicitAnalogTelevisionOutputRestriction><BestEffort>true</BestEffort><ConfigurationData>0</ConfigurationData></ExplicitAnalogTelevisionOutputRestriction><ImageConstraintForAnalogComponentVideoRestriction>true</ImageConstraintForAnalogComponentVideoRestriction><ImageConstraintForAnalogComputerMonitorRestriction>true</ImageConstraintForAnalogComputerMonitorRestriction><ScmsRestriction><ConfigurationData>2</ConfigurationData></ScmsRestriction><UncompressedDigitalAudioOpl>250</UncompressedDigitalAudioOpl><UncompressedDigitalVideoOpl>270</UncompressedDigitalVideoOpl></PlayRight></PlayReadyLicenseTemplate></LicenseTemplates><ResponseCustomData>This is my response custom data</ResponseCustomData></PlayReadyLicenseResponseTemplate>";

        PlayReadyLicenseResponseTemplate responseTemplate2 = MediaServicesLicenseTemplateSerializer.deserialize(serializedTemplate, schemaFile);
        assertNotNull(responseTemplate2);
    }
    
    @Test
    public void knownGoodInputMinimalLicenseTest() throws Exception
    {
        String serializedTemplate = "<PlayReadyLicenseResponseTemplate xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://schemas.microsoft.com/Azure/MediaServices/KeyDelivery/PlayReadyTemplate/v1\"><LicenseTemplates><PlayReadyLicenseTemplate><ContentKey i:type=\"ContentEncryptionKeyFromHeader\" /><PlayRight /></PlayReadyLicenseTemplate></LicenseTemplates></PlayReadyLicenseResponseTemplate>";

        PlayReadyLicenseResponseTemplate responseTemplate2 = MediaServicesLicenseTemplateSerializer.deserialize(serializedTemplate, schemaFile);
        assertNotNull(responseTemplate2);
    }
    
    @Test
    public void inputMissingContentKeyShouldThrowArgumentException() throws Exception
    {
        String serializedTemplate = "<PlayReadyLicenseResponseTemplate xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://schemas.microsoft.com/Azure/MediaServices/KeyDelivery/PlayReadyTemplate/v1\"><LicenseTemplates><PlayReadyLicenseTemplate><PlayRight /></PlayReadyLicenseTemplate></LicenseTemplates></PlayReadyLicenseResponseTemplate>";

        try
        {
            MediaServicesLicenseTemplateSerializer.deserialize(serializedTemplate, schemaFile);
            fail("Should throw an IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(ErrorMessages.PLAY_READY_CONTENT_KEY_REQUIRED, e.getMessage());
        }
    }
    
    @Test
    public void inputMissingPlayRightShouldThrowArgumentException() throws Exception
    {
        String serializedTemplate = "<PlayReadyLicenseResponseTemplate xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://schemas.microsoft.com/Azure/MediaServices/KeyDelivery/PlayReadyTemplate/v1\"><LicenseTemplates><PlayReadyLicenseTemplate><ContentKey i:type=\"ContentEncryptionKeyFromHeader\" /></PlayReadyLicenseTemplate></LicenseTemplates></PlayReadyLicenseResponseTemplate>";

        try
        {
            MediaServicesLicenseTemplateSerializer.deserialize(serializedTemplate, schemaFile);
            fail("Should throw an IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(ErrorMessages.PLAY_READY_PLAY_RIGHT_REQUIRED, e.getMessage());
        }
    }
    
    @Test
    public void inputMissingLicenseTemplatesShouldThrowArgumentException() throws Exception
    {
        String serializedTemplate = "<PlayReadyLicenseResponseTemplate xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://schemas.microsoft.com/Azure/MediaServices/KeyDelivery/PlayReadyTemplate/v1\"><LicenseTemplates></LicenseTemplates></PlayReadyLicenseResponseTemplate>";

        try
        {
            MediaServicesLicenseTemplateSerializer.deserialize(serializedTemplate, schemaFile);
            fail("Should throw an IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(ErrorMessages.AT_LEAST_ONE_LICENSE_TEMPLATE_REQUIRED, e.getMessage());
        }
    }
    
    @Test
    public void ScmsRestrictionConfigurationDataValidationTest()
    {
        byte[] validConfigurationValues = new byte[] { 0, 1, 2, 3 };
        byte[] invalidConfigurationValues = new byte[] {(byte) 255, (byte) 128, 4, 5, 15};

        for (byte configurationData : validConfigurationValues)
        {
            new ScmsRestriction(configurationData);
        }

        for  (byte configurationData : invalidConfigurationValues)
        {
            try
            {
                new ScmsRestriction(configurationData);
                fail("Invalid configuration data accepted");
            }
            catch (IllegalArgumentException e)
            {
                assertEquals(ErrorMessages.INVALID_TWO_BIT_CONFIGURATION_DATA, e.getMessage());
            }
        }
    }
    
    @Test
    public void validateOutputProtectionLevelValueChecks()
    {
        //  From the PlayReady Compliance Rules for issuing PlayReady Licenses.
        //
        //                         Table 6.6: Allowed Output Protection Level Values
        //
        //                      Field                                       Allowed Values
        //
        //  Minimum Compressed Digital Audio Output Protection Level    100, 150, 200, 250, 300
        //  Minimum Uncompressed Digital Audio Output Protection Level  100, 150, 200, 250, 300
        //  Minimum Compressed Digital Video Output Protection Level    400, 500
        //  Minimum Uncompressed Digital Video Output Protection Level  100, 250, 270, 300
        //  Minimum Analog Television Output Protection Level           100, 150, 200
        //

        boolean[] expectedResult = null;

        // First check null, which all of the Opls values support.  Setting Opl values is optional
        // and null is the way the user signals that they do not want to set a value.
        boolean[] currentResult = setOutputProtectionLevelValues(null);
        expectedResult = new boolean[] { true, true, true, true, true };
        assertTrue("null result didn't match expectations", Arrays.equals(currentResult, expectedResult));

        for (int i = 0; i <= 550; i += 10)
        {
            currentResult = setOutputProtectionLevelValues(i);

            switch (i)
            { 
                case 100:
                    expectedResult = new boolean[] {true, true, false, true, true };
                    assertTrue("100 result didn't match expectations", Arrays.equals(currentResult, expectedResult));
                    break;
                case 150:
                    expectedResult = new boolean[] {true, true, false, false, true };
                    assertTrue("150 result didn't match expectations", Arrays.equals(currentResult, expectedResult));
                    break;
                case 200:
                    expectedResult = new boolean[] {true, true, false, false, true };
                    assertTrue("200 result didn't match expectations", Arrays.equals(currentResult, expectedResult));
                    break;
                case 250:
                    expectedResult = new boolean[] {true, true, false, true, false };
                    assertTrue("250 result didn't match expectations", Arrays.equals(currentResult, expectedResult));
                    break;
                case 270:
                    expectedResult = new boolean[] { false, false, false, true, false };
                    assertTrue("270 result didn't match expectations", Arrays.equals(currentResult, expectedResult));
                    break;
                case 300:
                    expectedResult = new boolean[] { true, true, false, true, false };
                    assertTrue("300 result didn't match expectations", Arrays.equals(currentResult, expectedResult));
                    break;
                case 400:
                    expectedResult = new boolean[] { false, false, true, false, false };
                    assertTrue("400 result didn't match expectations", Arrays.equals(currentResult, expectedResult));
                    break;
                case 500:
                    expectedResult = new boolean[] { false, false, true, false, false };
                    assertTrue("500 result didn't match expectations", Arrays.equals(currentResult, expectedResult));
                    break;
                default:
                    // These values should always return false for all types
                    expectedResult = new boolean[] { false, false, false, false, false };
                    assertTrue("" + i + " result didn't match expectations", Arrays.equals(currentResult, expectedResult));
                    break;
            }

        }
    }

    private boolean[] setOutputProtectionLevelValues(Integer valueToSet)
    {
        PlayReadyPlayRight playRight = new PlayReadyPlayRight();
        boolean[] returnValue = new boolean[5];

        try
        {
            playRight.setCompressedDigitalAudioOpl(valueToSet);
            returnValue[0] = true;
        }
        catch (IllegalArgumentException ae)
        {
            if (!ae.getMessage().equals(ErrorMessages.COMPRESSED_DIGITAL_AUDIO_OPL_VALUE_ERROR)) {
                throw ae;
            }
        }

        try
        {
            playRight.setUncompressedDigitalAudioOpl(valueToSet);
            returnValue[1] = true;
        }
        catch (IllegalArgumentException ae)
        {
            if (!ae.getMessage().equals(ErrorMessages.UNCOMPRESSED_DIGITAL_AUDIO_OPL_VALUE_ERROR)) {
                throw ae;
            }
        }

        try
        {
            playRight.setCompressedDigitalVideoOpl(valueToSet);
            returnValue[2] = true;
        }
        catch (IllegalArgumentException ae)
        {
            if (!ae.getMessage().equals(ErrorMessages.COMPRESSED_DIGITAL_VIDEO_OPL_VALUE_ERROR)) {
                throw ae;
            }
        }

        try
        {
            playRight.setUncompressedDigitalVideoOpl(valueToSet);
            returnValue[3] = true;
        }
        catch (IllegalArgumentException ae)
        {
            if (!ae.getMessage().equals(ErrorMessages.UNCOMPRESSED_DIGITAL_VIDEO_OPL_VALUE_ERROR)) {
                throw ae;
            }
        }

        try
        {
            playRight.setAnalogVideoOpl(valueToSet);
            returnValue[4] = true;
        }
        catch (IllegalArgumentException ae)
        {
            if (!ae.getMessage().equals(ErrorMessages.ANALOG_VIDEO_OPL_VALUE_ERROR)) {
                throw ae;
            }
        }

        return returnValue;
    }

}

