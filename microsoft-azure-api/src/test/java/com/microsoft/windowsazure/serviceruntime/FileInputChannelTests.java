/**
 * 
 */
package com.microsoft.windowsazure.serviceruntime;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 */
public class FileInputChannelTests {
	@Test
	public void getInputStreamOpensFile() {
		InputChannel inputChannel = new FileInputChannel();
		
		try {
			File tempFile = File.createTempFile("getInputStreamOpensFile", null);			
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
			String expectedData = "test content";
			
			writer.write(expectedData);
			writer.close();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputChannel.getInputStream(tempFile.getAbsolutePath())));
			
			assertThat(reader.readLine(), equalTo(expectedData));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
