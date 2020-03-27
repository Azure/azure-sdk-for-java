// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.ExtractedReceipt;
import com.azure.core.util.IterableStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Contains helper methods for generating inputs for test methods
 */
final class TestUtils {

    static final String INVALID_KEY = "invalid key";
    static final String INVALID_URL = "htttttttps://localhost:8080";
    static final String VALID_HTTPS_LOCALHOST = "https://localhost:8080";
    static final String RECEIPT_LOCAL_URL = "../../../../../resources/sample-files/contoso-receipt.png";
    static final Long FILE_LENGTH = new File(RECEIPT_LOCAL_URL).length();

    // Receipts
    static final String RECEIPT_URL = "https://github.com/Azure-Samples/cognitive-services-REST-api-samples/blob/master/"
        + "curl/form-recognizer/contoso-receipt.png?raw=true";

    static IterableStream<ExtractedReceipt> getExtractedReceipts() {
        return new IterableStream<ExtractedReceipt>(new ArrayList<ExtractedReceipt>());
    }

    static InputStream getReceiptFileData() throws FileNotFoundException {
        return new FileInputStream(RECEIPT_LOCAL_URL);
    }

    private TestUtils() {
    }
}
