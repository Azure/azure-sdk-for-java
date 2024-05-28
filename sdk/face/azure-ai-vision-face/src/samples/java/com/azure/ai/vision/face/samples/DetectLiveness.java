// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.samples;

import com.azure.ai.vision.face.FaceSessionClient;
import com.azure.ai.vision.face.FaceSessionClientBuilder;
import com.azure.ai.vision.face.models.CreateLivenessSessionContent;
import com.azure.ai.vision.face.models.CreateLivenessSessionResult;
import com.azure.ai.vision.face.models.LivenessOperationMode;
import com.azure.ai.vision.face.models.LivenessSession;
import com.azure.ai.vision.face.models.LivenessSessionAuditEntry;
import com.azure.ai.vision.face.models.LivenessSessionItem;
import com.azure.ai.vision.face.samples.utils.ConfigurationHelper;
import com.azure.ai.vision.face.samples.utils.Resources;
import com.azure.ai.vision.face.samples.utils.Utils;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;

import java.util.List;
import java.util.UUID;

import static com.azure.ai.vision.face.samples.utils.Utils.log;
import static com.azure.ai.vision.face.samples.utils.Utils.logObject;

public class DetectLiveness {
    public static void main(String[] args) {
        // This sample follows below documentation
        // https://learn.microsoft.com/en-us/azure/ai-services/computer-vision/tutorials/liveness
        // We will the steps in
        // https://learn.microsoft.com/en-us/azure/ai-services/computer-vision/tutorials/liveness#orchestrate-the-liveness-solution
        // to demo the sample code in app server

        // 1. A client device will send a request to start liveness check to us
        waitingForLivenessRequest();

        // 2.Send a request to Face API to create a liveness session
        // Create a FaceSessionClient
        FaceSessionClient faceSessionClient = new FaceSessionClientBuilder()
            .endpoint(ConfigurationHelper.getEndpoint())
            .credential(new AzureKeyCredential(ConfigurationHelper.getAccountKey()))
            .buildClient();

        // Create a liveness session
        CreateLivenessSessionContent parameters = new CreateLivenessSessionContent(LivenessOperationMode.PASSIVE)
            .setDeviceCorrelationId(UUID.randomUUID().toString())
            .setSendResultsToClient(false)
            .setAuthTokenTimeToLiveInSeconds(60);
        BinaryData data = Utils.loadFromFile(Resources.TEST_IMAGE_PATH_DETECTLIVENESS_VERIFYIMAGE);
        CreateLivenessSessionResult livenessSessionCreationResult = faceSessionClient.createLivenessSession(parameters);
        String sessionId = livenessSessionCreationResult.getSessionId();
        logObject("Create a liveness session: ", livenessSessionCreationResult, true);
        String token = livenessSessionCreationResult.getAuthToken();

        try {
            // 3. Pass the AuthToken to client device
            // Client device will process the step 4, 5, 6 in the documentation 'Orchestrate the liveness solution'
            sendTokenToClientDevices(token);

            // 7. Wait for client device notify us that liveness session has completed.
            waitingForLivenessSessionComplete();

            // 8. After client devices perform the action, we can get the result from the following API
            LivenessSession sessionResult = faceSessionClient.getLivenessSessionResult(livenessSessionCreationResult.getSessionId());
            logObject("Get liveness session result after client device complete liveness check: ", sessionResult);

            // Get the details of all the request/response for liveness check for this sessions
            List<LivenessSessionAuditEntry> auditEntries = faceSessionClient.getLivenessSessionAuditEntries(
                livenessSessionCreationResult.getSessionId());
            logObject("Get audit entries: ", auditEntries);

            // We can also list all the liveness sessions of this face account.
            List<LivenessSessionItem> sessions = faceSessionClient.getLivenessSessions();
            logObject("List all the liveness sessions: ", sessions, true);
        } finally {
            // Delete this session
            faceSessionClient.deleteLivenessSession(livenessSessionCreationResult.getSessionId());
        }
    }

    private static void waitingForLivenessSessionComplete() {
        log("Please refer to https://learn.microsoft.com/en-us/azure/ai-services/computer-vision/tutorials/liveness and use the mobile client SDK to perform liveness detection on your mobile application.");
        Utils.pressAnyKeyToContinue("Press any key to continue when you complete these steps to run sample to get session results ...");
    }

    private static void sendTokenToClientDevices(String token) {
        // Logic to send token to client devices
    }

    private static void waitingForLivenessRequest() {
        // Logic to wait for request from client device
    }
}
