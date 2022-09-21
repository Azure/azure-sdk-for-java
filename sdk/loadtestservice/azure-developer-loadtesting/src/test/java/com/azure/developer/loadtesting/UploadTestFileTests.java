// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.loadtesting;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class UploadTestFileTests extends LoadTestingClientTestBase {
    private final String FILE_JXM_NAME = "sample-JMX-file.jmx";
    private final URL FILE_JMX_URL = UploadTestFileTests.class.getClassLoader().getResource(FILE_JXM_NAME);

    @Test
    public void uploadTestFile() throws IOException {
        BinaryData file = BinaryData.fromFile(new File(FILE_JMX_URL.getPath()).toPath());
        RequestOptions requestOptions = new RequestOptions().addQueryParam("fileType", "2");
        Response<BinaryData> response = client.getAdministration().uploadTestFileWithResponse(
                                                DEFAULT_TEST_ID,
                                                DEFAULT_FILE_ID,
                                                FILE_JXM_NAME,
                                                file,
                                                requestOptions);
        Assertions.assertEquals(201, response.getStatusCode());
    }
}
