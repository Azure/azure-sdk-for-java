// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.DialogInputType;
import com.azure.communication.callautomation.models.DialogStateResult;
import com.azure.communication.callautomation.models.StartDialogOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Test;

import java.util.*;

import static com.azure.communication.callautomation.CallAutomationUnitTestBase.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CallDialogUnitTests {

    private CallDialog callDialog;

    @Test
    public void startDialogTest() {
        // override callDialog to mock 201 response code
        CallConnection callConnection =
            CallAutomationUnitTestBase.getCallConnection(new ArrayList<>(
                Collections.singletonList(new AbstractMap.SimpleEntry<>(generateDialogStateResponse(), 201)))
            );
        callDialog = callConnection.getCallDialog();

        Map<String, Object> dialogContext = new HashMap<>();
        StartDialogOptions options = new StartDialogOptions(
            DIALOG_ID,
            DialogInputType.POWER_VIRTUAL_AGENTS,
            dialogContext);

        DialogStateResult response = callDialog.startDialog(options);

        assertNotNull(response);
        assertEquals(response.getDialogId(), DIALOG_ID);
    }

    @Test
    public void startDialogWithResponseTest() {
        // override callDialog to mock 201 response code
        CallConnection callConnection =
            CallAutomationUnitTestBase.getCallConnection(new ArrayList<>(
                Collections.singletonList(new AbstractMap.SimpleEntry<>(generateDialogStateResponse(), 201)))
            );
        callDialog = callConnection.getCallDialog();

        Map<String, Object> dialogContext = new HashMap<>();
        StartDialogOptions options = new StartDialogOptions(
            DialogInputType.AZURE_OPEN_AI,
            dialogContext);

        options.setOperationContext("operationContext");
        options.setBotId(BOT_APP_ID);

        Response<DialogStateResult> response = callDialog.startDialogWithResponse(
            options,
            Context.NONE);

        assertNotNull(response);
        assertEquals(response.getStatusCode(), 201);
    }

    @Test
    public void stopDialogWithResponseTest() {
        // override callDialog to mock 204 response code
        CallConnection callConnection =
            CallAutomationUnitTestBase.getCallConnection(new ArrayList<>(
                Collections.singletonList(new AbstractMap.SimpleEntry<>("", 204)))
            );
        callDialog = callConnection.getCallDialog();

        Response<Void> response = callDialog.stopDialogWithResponse(
            DIALOG_ID,
            Context.NONE);

        assertEquals(response.getStatusCode(), 204);
    }
}
