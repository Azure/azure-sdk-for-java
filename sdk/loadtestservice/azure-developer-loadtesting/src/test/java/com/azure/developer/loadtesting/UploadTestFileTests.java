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
    private final String fileJmxName = "sample-JMX-file.jmx";
    private final URL fileJmxUrl = UploadTestFileTests.class.getClassLoader().getResource(fileJmxName);

    @Test
    public void uploadTestFile() throws IOException {
        BinaryData file = BinaryData.fromFile(new File(fileJmxUrl.getPath()).toPath());
        RequestOptions requestOptions = new RequestOptions().addQueryParam("fileType", "2");
        Response<BinaryData> response = client.getLoadTestAdministrationClient().uploadTestFileWithResponse(
                                                defaultTestId,
                                                defaultFileId,
                                                fileJmxName,
                                                file,
                                                requestOptions);
        Assertions.assertEquals(201, response.getStatusCode());
    }
}
