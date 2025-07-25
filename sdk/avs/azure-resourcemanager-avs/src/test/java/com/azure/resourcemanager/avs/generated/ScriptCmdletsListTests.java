// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.resourcemanager.avs.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.avs.implementation.models.ScriptCmdletsList;
import org.junit.jupiter.api.Assertions;

public final class ScriptCmdletsListTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        ScriptCmdletsList model = BinaryData.fromString(
            "{\"value\":[{\"properties\":{\"provisioningState\":\"Succeeded\",\"description\":\"urgkakmokzhjjk\",\"timeout\":\"fhmouwq\",\"audience\":\"Automation\",\"parameters\":[{\"type\":\"Float\",\"name\":\"yebizikayuh\",\"description\":\"bjbsybb\",\"visibility\":\"Visible\",\"optional\":\"Optional\"},{\"type\":\"SecureString\",\"name\":\"mfpgv\",\"description\":\"ipaslthaqfxssmwu\",\"visibility\":\"Visible\",\"optional\":\"Optional\"},{\"type\":\"Float\",\"name\":\"pdrhne\",\"description\":\"owqkdwytisi\",\"visibility\":\"Visible\",\"optional\":\"Required\"}]},\"id\":\"ikpzimejza\",\"name\":\"lfzxiavrmbzonoki\",\"type\":\"rjqc\"},{\"properties\":{\"provisioningState\":\"Succeeded\",\"description\":\"frl\",\"timeout\":\"szrnwo\",\"audience\":\"Automation\",\"parameters\":[{\"type\":\"Float\",\"name\":\"jylwbtlhflsj\",\"description\":\"hszfjvfb\",\"visibility\":\"Hidden\",\"optional\":\"Required\"},{\"type\":\"Int\",\"name\":\"rqmq\",\"description\":\"dvriiiojnal\",\"visibility\":\"Visible\",\"optional\":\"Required\"},{\"type\":\"Int\",\"name\":\"ex\",\"description\":\"wueluqhhahhxv\",\"visibility\":\"Visible\",\"optional\":\"Required\"}]},\"id\":\"pjgwwspug\",\"name\":\"ftqsxhqxujxuk\",\"type\":\"dxdigr\"},{\"properties\":{\"provisioningState\":\"Failed\",\"description\":\"zdmsyqtfi\",\"timeout\":\"hbotzingamvppho\",\"audience\":\"Automation\",\"parameters\":[{\"type\":\"Int\",\"name\":\"qamvdkfwynwcvtbv\",\"description\":\"yhmtnvyqiat\",\"visibility\":\"Visible\",\"optional\":\"Required\"},{\"type\":\"Float\",\"name\":\"zcjaesgvvsccy\",\"description\":\"g\",\"visibility\":\"Hidden\",\"optional\":\"Required\"},{\"type\":\"Bool\",\"name\":\"lvdnkfx\",\"description\":\"emdwzrmuhapfc\",\"visibility\":\"Visible\",\"optional\":\"Required\"},{\"type\":\"SecureString\",\"name\":\"psvuoymgc\",\"description\":\"lvez\",\"visibility\":\"Visible\",\"optional\":\"Required\"}]},\"id\":\"feo\",\"name\":\"erqwkyhkobopg\",\"type\":\"edkowepbqpcrfk\"},{\"properties\":{\"provisioningState\":\"Canceled\",\"description\":\"njv\",\"timeout\":\"wxlp\",\"audience\":\"Any\",\"parameters\":[{\"type\":\"Int\",\"name\":\"tjsyin\",\"description\":\"fq\",\"visibility\":\"Visible\",\"optional\":\"Required\"}]},\"id\":\"tmdvypgikdgs\",\"name\":\"ywkbirryuzhlhkjo\",\"type\":\"rvqqaatj\"}],\"nextLink\":\"rv\"}")
            .toObject(ScriptCmdletsList.class);
        Assertions.assertEquals("rv", model.nextLink());
    }
}
