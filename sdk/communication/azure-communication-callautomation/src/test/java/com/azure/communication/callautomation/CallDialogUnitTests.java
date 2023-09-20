package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.DialogInputType;
import com.azure.communication.callautomation.models.DialogStateResult;
import com.azure.communication.callautomation.models.StartDialogOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import static com.azure.communication.callautomation.CallAutomationUnitTestBase.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CallDialogUnitTests {

    private CallDialog callDialog;

    @Test
    public void startDialogWithResponseTest() {
        // override callDialog to mock 201 response code
        CallConnection callConnection =
            CallAutomationUnitTestBase.getCallConnection(new ArrayList<>(
                Collections.singletonList(new AbstractMap.SimpleEntry<>(generateDialogStateResponse(), 201)))
            );
        callDialog = callConnection.getCallDialog();

        StartDialogOptions options = new StartDialogOptions(
            BOT_APP_ID,
            DIALOG_ID,
            DialogInputType.POWER_VIRTUAL_AGENTS,
            Map.of());

        Response<DialogStateResult> response = callDialog.startDialogWithResponse(
            options,
            "operationContext",
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
