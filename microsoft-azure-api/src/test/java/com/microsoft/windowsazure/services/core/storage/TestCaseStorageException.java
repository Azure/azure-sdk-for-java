package com.microsoft.windowsazure.services.core.storage;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.net.HttpURLConnection;

import org.junit.Test;
import org.mockito.Mockito;

public class TestCaseStorageException {

    /**
     * @author jurgenma
     * 
     *         Up to version 0.4.1 calling StorageException.translateException throwed an IllegalArgumentException
     *         when using Woodstox for XML Stream parsin
     */
    @Test
    public void testTranslateExceptionWithNullErrorStream() {

        /* 
         *      Add the following dependency to the Maven pom file for testing with Woodstox       
         * 
         *      <dependency> 
                    <groupId>woodstox</groupId> 
                    <artifactId>wstx-asl</artifactId> 
                    <version>3.2.2</version> 
                    <scope>test</scope>
                </dependency> 
        */

        HttpURLConnection request = Mockito.mock(HttpURLConnection.class);
        InputStream errStream = request.getErrorStream();
        assertNull(errStream);

        // Test calling with null value for request.getErrorStream. Must not throw an exception
        StorageException.translateException(request, null, null);
    }

}
