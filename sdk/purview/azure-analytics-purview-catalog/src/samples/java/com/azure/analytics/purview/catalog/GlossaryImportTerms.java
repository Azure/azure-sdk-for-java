package com.azure.analytics.purview.catalog;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.nio.file.Path;
import java.util.Collections;

public class GlossaryImportTerms {
    public static void main(String[] args) {

        GlossaryClient client =
            new PurviewCatalogClientBuilder()
                .endpoint(System.getenv("ENDPOINT"))
                .credential(new DefaultAzureCredentialBuilder().build())
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .addPolicy(new HttpDebugLoggingPolicy())
//                .addPolicy(new AddHeadersPolicy(new HttpHeaders(Collections.singletonList(new HttpHeader("Content-Type", "multipart/form-data; boundary=abcde12345")))))
                .buildGlossaryClient();
        BinaryData binaryData = BinaryData.fromFile(Path.of("C:\\Users\\fey\\Downloads\\import-terms-template-System-default.csv"));
        binaryData = BinaryData.fromString("------WebKitFormBoundarySfdITjdkB9iBsA0A\n" +
            "Content-Disposition: form-data; name=\"file\"\n" +
            "Content-Type: text/plain\n\n" +
            binaryData +
            "\n------WebKitFormBoundarySfdITjdkB9iBsA0A--");
        System.out.println(binaryData);
        RequestOptions requestOptions = new RequestOptions();
//        requestOptions.addHeader("Content-Type", "multipart/form-data; boundary=abcde12345");
        requestOptions.addQueryParam("includeTermHierarchy", "true");
        client.beginImportGlossaryTermsViaCsvByGlossaryName("Glossary", binaryData, requestOptions, null).getFinalResult();
    }
}
