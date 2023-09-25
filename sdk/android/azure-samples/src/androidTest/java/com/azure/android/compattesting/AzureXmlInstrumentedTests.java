package com.azure.android.compattesting;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.azure.xml.XmlReader;
import com.azure.xml.XmlWriter;
import com.azure.xml.implementation.DefaultXmlReader;
import com.azure.xml.implementation.DefaultXmlWriter;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;

import javax.xml.stream.XMLStreamException;

/**
 * Instrumented tests, which will execute on an Android device.
 * These test cases are for cases where the Android user has already installed the necessary
 * dependencies to run the Azure SDK, so it validates that the dependencies contain all of the
 * necessary libraries and methods.
 */
@RunWith(AndroidJUnit4.class)
public class AzureXmlInstrumentedTests {
    // TODO: Update comments and remove assertThrows() once AzureXML is updated past 1.0.0-beta.2
    /**
     * Tests whether DefaultXMLReader can make an XmlReader. Will throw NoSuchMethodError because
     * the StAX library is missing XmlInputFactory.newFactory() which is in the JDK, it only has
     * the XmlInputFactory.newInstance() method which should function identically to newFactory()
     */
    @Test
    public void defaultXmlReaderTest() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertThrows(NoSuchMethodError.class, () -> {
            try {
                XmlReader reader = DefaultXmlReader.fromString("");
                assertNotNull(reader);
            } catch (XMLStreamException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Tests whether DefaultXMLWriter can make an XmlWriter. Will throw NoSuchMethodError because
     * the StAX library is missing XmlOutputFactory.newFactory() which is in the JDK, it only has
     * the XmlOutputFactory.newInstance() method which should function identically to newFactory()
     */
    @Test
    public void defaultXmlWriterTest() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertThrows(NoSuchMethodError.class, () -> {
            try {
                XmlWriter writer = DefaultXmlWriter.toStream(new ByteArrayOutputStream());
                assertNotNull(writer);
            } catch (XMLStreamException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
