// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.DialogInputType;
import com.azure.communication.callautomation.models.DialogStateResult;
import com.azure.communication.callautomation.models.StartDialogOptions;
import com.azure.core.http.rest.Response;
import org.junit.jupiter.api.Test;

import java.util.*;

import static com.azure.communication.callautomation.CallAutomationUnitTestBase.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CallDialogAsyncUnitTests {

    private CallDialogAsync callDialogAsync;

    @Test
    public void startDialogWithResponseTest() {
        // override callDialog to mock 201 response code
        CallConnectionAsync callConnectionAsync =
            CallAutomationUnitTestBase.getCallConnectionAsync(new ArrayList<>(
                Collections.singletonList(new AbstractMap.SimpleEntry<>(generateDialogStateResponse(), 201)))
            );
        callDialogAsync = callConnectionAsync.getCallDialogAsync();

        Map<String, Object> dialogContext = new HashMap<>();
        StartDialogOptions options = new StartDialogOptions(
            DialogInputType.POWER_VIRTUAL_AGENTS,
            dialogContext);

        options.setOperationContext("operationContext");
        options.setBotId(BOT_APP_ID);

        Response<DialogStateResult> response = callDialogAsync.startDialogWithResponse(options).block();

        assertNotNull(response);
        assertEquals(response.getStatusCode(), 201);
    }

    @Test
    public void stopDialogWithResponseTest() {
        // override callDialog to mock 204 response code
        CallConnectionAsync callConnectionAsync =
            CallAutomationUnitTestBase.getCallConnectionAsync(new ArrayList<>(
                Collections.singletonList(new AbstractMap.SimpleEntry<>("", 204)))
            );
        callDialogAsync = callConnectionAsync.getCallDialogAsync();

        Response<Void> response = callDialogAsync.stopDialogWithResponse(DIALOG_ID).block();

        assertEquals(response.getStatusCode(), 204);
    }
}
