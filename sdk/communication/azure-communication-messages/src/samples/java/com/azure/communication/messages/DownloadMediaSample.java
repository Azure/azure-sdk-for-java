// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.messages;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DownloadMediaSample {
    private static final String CONNECTION_STRING = System.getenv("ACS_CONNECTION_STRING");

    public static void main(String[] args) throws IOException {

        NotificationMessagesClient messagesClient = new NotificationMessagesClientBuilder()
            .connectionString(CONNECTION_STRING)
            .buildClient();

        Response<BinaryData> response = messagesClient.downloadMediaWithResponse("<MEDIA_ID>", null);

        if (response.getStatusCode() == 200) {
            InputStream inputStream = response.getValue().toStream();

            // https://developers.facebook.com/docs/whatsapp/cloud-api/reference/media
            String fileType = response.getHeaders().get(HttpHeaderName.CONTENT_TYPE).getValue();
            if (fileType.contains("jpeg")) {
                FileOutputStream fileOutputStream = new FileOutputStream("sample.jpg");
                byte[] buffer = new byte[1024];
                int count = 0;
                while ((count = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, count);
                }

                fileOutputStream.close();

                System.out.println("Image downloaded successfully.");
            }
        }
    }
}
