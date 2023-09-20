package com.azure.communication.callautomation;

import com.azure.communication.callautomation.implementation.models.DialogInputType;
import com.azure.communication.callautomation.models.DialogStateResult;
import com.azure.communication.callautomation.models.DtmfTone;
import com.azure.communication.callautomation.models.StartDialogOptions;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        StartDialogOptions options = new StartDialogOptions(
            BOT_APP_ID,
            DIALOG_ID,
            DialogInputType.POWER_VIRTUAL_AGENTS,
            Map.of());

        Response<DialogStateResult> response = callDialogAsync.startDialogWithResponse(
            options,
            "operationContext").block();

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

        Response<Void> response = callDialogAsync.stopDialogWithResponse(
            DIALOG_ID,
            "operationContext").block();

        assertEquals(response.getStatusCode(), 204);
    }
}
