// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * @author Warren Zhu
 */
@RestController
@RequestMapping("blob")
public class BlobController {

    @Value("${blob}")
    private Resource blobFile;

    @GetMapping
    public String readBlobFile() throws IOException {
        return StreamUtils.copyToString(
            this.blobFile.getInputStream(),
            Charset.defaultCharset());
    }

    @PostMapping
    public String writeBlobFile(@RequestBody String data) throws IOException {
        try (OutputStream os = ((WritableResource) this.blobFile).getOutputStream()) {
            os.write(data.getBytes());
        }
        return "file was updated";
    }
}
