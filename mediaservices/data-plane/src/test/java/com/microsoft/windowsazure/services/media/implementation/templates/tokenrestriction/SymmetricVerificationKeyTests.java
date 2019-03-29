package com.microsoft.windowsazure.services.media.implementation.templates.tokenrestriction;

import static org.junit.Assert.*;

import org.junit.Test;

import com.microsoft.windowsazure.services.media.implementation.templates.tokenrestriction.SymmetricVerificationKey;

public class SymmetricVerificationKeyTests {
    
    @Test
    public void NewSymmetricVerificationKeyShouldCreateNewKey() {
        // Arrange
        SymmetricVerificationKey key = new SymmetricVerificationKey();
        
        // Act
        byte[] resultKey = key.getKeyValue();
        
        // Assert
        assertNotNull(resultKey);
    }
    
    @Test
    public void GetterSetterSymmetricVerificationKey() {
        // Arrange
        SymmetricVerificationKey key = new SymmetricVerificationKey();
        byte[] keyValue = key.getKeyValue();
        
        // Act
        SymmetricVerificationKey key2 = new SymmetricVerificationKey();
        key2.setKeyValue(keyValue);
        byte[] resultsValue = key2.getKeyValue();
        
        // Assert
        assertArrayEquals(keyValue, resultsValue);
    }
    
    @Test
    public void KeyInConstructorSymmetricVerificationKeyShouldMatch() {
        // Arrange
        SymmetricVerificationKey key = new SymmetricVerificationKey();
        byte[] keyValue = key.getKeyValue();
        
        // Act
        SymmetricVerificationKey key2 = new SymmetricVerificationKey(keyValue);
        byte[] resultsValue = key2.getKeyValue();
        
        // Assert
        assertArrayEquals(keyValue, resultsValue);
    }
    
}
