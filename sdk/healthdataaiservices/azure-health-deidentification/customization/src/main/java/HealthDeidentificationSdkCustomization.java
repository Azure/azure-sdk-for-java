// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.*;
import org.slf4j.Logger;

public class HealthDeidentificationSdkCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        PackageCustomization clientImplementation = customization.getPackage("com.azure.health.deidentification");
        ClassCustomization deidentificationAsyncClient = clientImplementation.getClass("DeidentificationAsyncClient");
        MethodCustomization listJobs = deidentificationAsyncClient.getMethod("listJobs(String continuationToken)");
        listJobs.replaceBody("// Generated convenience method for listJobs\n" +
            "RequestOptions requestOptions = new RequestOptions();\n" +
            "if (continuationToken != null) {\n" +
            "    requestOptions.addQueryParam(\"continuationToken\", continuationToken, false);\n" +
            "}\n" +
            "PagedFlux<BinaryData> pagedFluxResponse = listJobs(requestOptions);\n" +
            "return PagedFlux.create(() -> (nextPageToken, pageSize) -> {\n" +
            "    Flux<PagedResponse<BinaryData>> flux = (nextPageToken == null)\n" +
            "        ? pagedFluxResponse.byPage().take(1)\n" +
            "        : pagedFluxResponse.byPage(nextPageToken).take(1);\n" +
            "    return flux\n" +
            "        .map(pagedResponse -> new PagedResponseBase<Void, DeidentificationJob>(pagedResponse.getRequest(),\n" +
            "            pagedResponse.getStatusCode(), pagedResponse.getHeaders(),\n" +
            "            pagedResponse.getValue()\n" +
            "                .stream()\n" +
            "                .map(protocolMethodData -> protocolMethodData.toObject(DeidentificationJob.class))\n" +
            "                .collect(Collectors.toList()),\n" +
            "            pagedResponse.getContinuationToken(), null));\n" +
            "});");
        
        MethodCustomization listJobFiles = deidentificationAsyncClient.getMethod("listJobFiles(String name, String continuationToken)");
        listJobFiles.replaceBody("// Generated convenience method for listJobFiles\n" +
            "RequestOptions requestOptions = new RequestOptions();\n" +
            "if (continuationToken != null) {\n" +
            "    requestOptions.addQueryParam(\"continuationToken\", continuationToken, false);\n" +
            "}\n" +
            "PagedFlux<BinaryData> pagedFluxResponse = listJobFiles(name, requestOptions);\n" +
            "return PagedFlux.create(() -> (nextPageToken, pageSize) -> {\n" +
            "    Flux<PagedResponse<BinaryData>> flux = (nextPageToken == null)\n" +
            "        ? pagedFluxResponse.byPage().take(1)\n" +
            "        : pagedFluxResponse.byPage(nextPageToken).take(1);\n" +
            "    return flux.map(pagedResponse -> new PagedResponseBase<Void, HealthFileDetails>(pagedResponse.getRequest(),\n" +
            "        pagedResponse.getStatusCode(), pagedResponse.getHeaders(),\n" +
            "        pagedResponse.getValue()\n" +
            "            .stream()\n" +
            "            .map(protocolMethodData -> protocolMethodData.toObject(HealthFileDetails.class))\n" +
            "            .collect(Collectors.toList()),\n" +
            "        pagedResponse.getContinuationToken(), null));\n" +
            "});");
    }

}
