package com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense;

import static org.junit.Assert.*;

import java.security.InvalidParameterException;
import java.util.UUID;

import org.junit.Test;

import com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense.ContentEncryptionKeyFromHeader;
import com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense.ContentEncryptionKeyFromKeyIdentifier;

public class PlayReadyContentKeyTests {
    
    @Test
    public void InvalidUUIDContentEncryptionKeyFromKeyIdentifierTest() {
        // Arrange
        UUID invalidUUID = new UUID(0L, 0L);
        
        // Act
        try {
            @SuppressWarnings("unused")
            ContentEncryptionKeyFromKeyIdentifier contentEncryptionKeyFromKeyIdentifier =
                    new ContentEncryptionKeyFromKeyIdentifier(invalidUUID);
            fail("Should Thrown");
        } catch (InvalidParameterException e) {
            assertEquals(e.getMessage(), "keyIdentifier");
        }
    }
    
    @Test
    public void ValidUUIDContentEncryptionKeyFromKeyIdentifierTest() {
        // Arrange
        UUID validUUID = UUID.randomUUID();
        
        // Act
        ContentEncryptionKeyFromKeyIdentifier contentEncryptionKeyFromKeyIdentifier =
                new ContentEncryptionKeyFromKeyIdentifier(validUUID);
        UUID resultUUID = contentEncryptionKeyFromKeyIdentifier.getKeyIdentifier();

        // Assert
        assertEquals(resultUUID, validUUID);
    }
    
    @Test
    public void GetterSetterUUIDContentEncryptionKeyFromKeyIdentifierTest() {
        // Arrange
        UUID expectedUUID = UUID.randomUUID();
        
        // Act
        ContentEncryptionKeyFromKeyIdentifier contentEncryptionKeyFromKeyIdentifier =
                new ContentEncryptionKeyFromKeyIdentifier(UUID.randomUUID());
        
        contentEncryptionKeyFromKeyIdentifier.setKeyIdentifier(expectedUUID);
        UUID resultUUID = contentEncryptionKeyFromKeyIdentifier.getKeyIdentifier();

        // Assert
        assertEquals(resultUUID, expectedUUID);
    }
    
    @Test
    public void NewContentEncryptionKeyFromHeaderTest() {
        // Arrange
        // Act
        ContentEncryptionKeyFromHeader contentEncryptionKeyFromHeader =
                new ContentEncryptionKeyFromHeader();
        // Assert
        assertNotNull(contentEncryptionKeyFromHeader);
    }

}
