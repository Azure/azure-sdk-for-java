// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.extendedlocation.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.extendedlocation.models.EnabledResourceTypesListResult;

public final class EnabledResourceTypesListResultTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        EnabledResourceTypesListResult model = BinaryData.fromString(
            "{\"nextLink\":\"gjvtbv\",\"value\":[{\"properties\":{\"clusterExtensionId\":\"dnrujqguhmuouqfp\",\"extensionType\":\"zw\",\"typesMetadata\":[{\"apiVersion\":\"itnwuizgazxufi\",\"resourceProviderNamespace\":\"ckyfih\",\"resourceType\":\"idf\"}]},\"id\":\"wdzuhtymwisd\",\"name\":\"fthwxmnteiwa\",\"type\":\"pvkmijcmmxdcuf\"},{\"properties\":{\"clusterExtensionId\":\"rpymzidnsez\",\"extensionType\":\"tbzsgfyccs\",\"typesMetadata\":[{\"apiVersion\":\"dwzjeiach\",\"resourceProviderNamespace\":\"osfln\",\"resourceType\":\"sfqpteehz\"},{\"apiVersion\":\"ypyqrimzinp\",\"resourceProviderNamespace\":\"wjdk\",\"resourceType\":\"soodqxhcrmnoh\"},{\"apiVersion\":\"ckwhds\",\"resourceProviderNamespace\":\"fiyipjxsqwpgrj\",\"resourceType\":\"norcjxvsnbyxqab\"},{\"apiVersion\":\"ocpcy\",\"resourceProviderNamespace\":\"urzafb\",\"resourceType\":\"j\"}]},\"id\":\"btoqcjmkljavbqid\",\"name\":\"qajzyulpkudjkr\",\"type\":\"khbzhfepgzg\"}]}")
            .toObject(EnabledResourceTypesListResult.class);
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        EnabledResourceTypesListResult model = new EnabledResourceTypesListResult();
        model = BinaryData.fromObject(model).toObject(EnabledResourceTypesListResult.class);
    }
}
