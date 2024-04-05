package com.azure.core.util;

import com.azure.core.http.ContentType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.azure.core.CoreTestUtils.readStream;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MultipartFormDataTests {
    @Test
    public void boundaryValueMatch() {
        // Arrange
        String boundaryValue = "boundaryValue";
        String contentType = "multipart/form-data; boundary=" + boundaryValue;
        // Act
        MultipartFormData formData = new MultipartFormDataBuilder(boundaryValue)
            .build();
        // Assert
        assertEquals(contentType, formData.getContentType());
    }

    @Test
    public void appendTextContentLengthMatch() throws IOException {
        // Arrange
        String fieldName = "fieldName";
        String value = "value";
        // Act
        MultipartFormData formData = new MultipartFormDataBuilder()
                .appendText(fieldName, value)
                .build();
        // Assert
        assertEquals(99, readStream(formData.getRequestBody()).length);
        assertEquals(99, formData.getContentLength());
    }

    @Test
    public void appendFileContentLengthMatch() throws IOException {
        // Arrange
        String fieldName = "fieldName";
        Path filePath = Paths.get("src/test/resources/upload.txt");
        byte[] fileBytes = BinaryData.fromFile(filePath).toBytes();
        // Act
        MultipartFormData formData = new MultipartFormDataBuilder()
                .appendFile(fieldName, BinaryData.fromBytes(fileBytes), "application/octet-stream", "upload.txt")
                .build();
        // Assert
        assertEquals(200, readStream(formData.getRequestBody()).length);
        assertEquals(200, formData.getContentLength());
    }

    @Test
    public void appendMultipleFiles() {
        // Arrange
        String fieldName = "fieldName";
        Path filePath1 = Paths.get("src/test/resources/upload.txt");
        byte[] fileBytes1 = BinaryData.fromFile(filePath1).toBytes();
        Path filePath2 = Paths.get("src/test/resources/CloudEvent/CloudEventBinaryData.json");
        byte[] fileBytes2 = BinaryData.fromFile(filePath2).toBytes();

        List<BinaryData> files = List.of(BinaryData.fromBytes(fileBytes1), BinaryData.fromBytes(fileBytes2));
        List<String> contentTypes = List.of(ContentType.APPLICATION_OCTET_STREAM, ContentType.APPLICATION_JSON);
//        List<>

        // Act
        MultipartFormData formData = new MultipartFormDataBuilder()
                .appendFiles(fieldName, files, contentTypes, List.of("upload.txt"))
                .build();
        // Assert
        assertEquals(400, formData.getContentLength());
    }


    @Test
    public void testBoundaryValue() {
        // Arrange
        String boundaryValue = "boundaryValue";
        MultipartFormData formData = new MultipartFormDataBuilder(boundaryValue)
            .appendText("contentType", "multipart/form-data; boundary=boundaryValue")
            .appendText("requestBody",
                "LS02OTgwYzEzYi1mMDE0LTQ0DQpDb250ZW50LURpc3Bvc2l0aW9uOiBmb3JtLWRhdGE7IG5hbWU9ImZpbGUiOyBmaWxlbmFtZT0iSlBfaXRfaXNfcmFpbnlfdG9kYXkud2F2Ig0KQ29udGVudC1UeXBlOiBhcHBsaWNhdGlvbi9vY3RldC1zdHJlYW0NCg0KUklGRgYdAQBXQVZFZm10IBAAAAABAAEAgD4AAAB9AAACABAAZGF0YeIcAQCGAKcAaACnAIcApAB4AFoAJQA9AFIAIABgAC0AGgAaAP3")
            .build();
        // Act

        MultipartFormData formData1 = BinaryData.fromObject(formData).toObject(MultipartFormData.class);
        // Assert

        System.out.println(formData1.getContentType());

    }

}
