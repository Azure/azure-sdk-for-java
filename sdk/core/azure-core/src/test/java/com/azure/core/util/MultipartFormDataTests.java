package com.azure.core.util;

import org.junit.jupiter.api.Test;

public class MultipartFormDataTests {

    @Test
    public void testMultipartFormDataBuilder() {
        // Arrange
        MultipartFormData formData = new MultipartFormDataBuilder()
            .appendText("contentType", "multipart/form-data; boundary=6980c13b-f014-44")
            .appendText("requestBody",
                "LS02OTgwYzEzYi1mMDE0LTQ0DQpDb250ZW50LURpc3Bvc2l0aW9uOiBmb3JtLWRhdGE7IG5hbWU9ImZpbGUiOyBmaWxlbmFtZT0iSlBfaXRfaXNfcmFpbnlfdG9kYXkud2F2Ig0KQ29udGVudC1UeXBlOiBhcHBsaWNhdGlvbi9vY3RldC1zdHJlYW0NCg0KUklGRgYdAQBXQVZFZm10IBAAAAABAAEAgD4AAAB9AAACABAAZGF0YeIcAQCGAKcAaACnAIcApAB4AFoAJQA9AFIAIABgAC0AGgAaAP3")
            .build();
        // Act

        MultipartFormData formData1 = BinaryData.fromObject(formData).toObject(MultipartFormData.class);
        // Assert

        System.out.println(formData1.getContentType());

    }

}
