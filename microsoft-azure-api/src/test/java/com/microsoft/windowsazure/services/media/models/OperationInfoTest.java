/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.services.media.models;

import static org.junit.Assert.*;

import org.junit.Test;

import com.microsoft.windowsazure.services.media.implementation.content.OperationType;

public class OperationInfoTest {

    @SuppressWarnings("rawtypes")
    @Test
    public void testGetSetId() {
        // Arrange
        String expectedId = "expectedId";
        OperationInfo operationInfo = new OperationInfo(null, new OperationType().setId(expectedId));

        // Act 
        String actualId = operationInfo.getId();

        // Assert
        assertEquals(expectedId, actualId);

    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testGetSetTargetEntityId() {
        // Arrange
        String expectedTargetEntityId = "expectedTargetEntityId";
        OperationInfo operationInfo = new OperationInfo(null,
                new OperationType().setTargetEntityId(expectedTargetEntityId));

        // Act 
        String actualTargetEntityId = operationInfo.getTargetEntityId();

        // Assert
        assertEquals(expectedTargetEntityId, actualTargetEntityId);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testGetSetState() throws Exception {
        // Arrange
        OperationState expectedState = OperationState.Failed;

        OperationInfo operationInfo = new OperationInfo(null, new OperationType().setState(expectedState.toString()));

        // Act 
        OperationState actualState = operationInfo.getState();

        // Assert
        assertEquals(expectedState, actualState);

    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testGetSetErrorCode() throws Exception {
        // Arrange
        String expectedErrorCode = "expectedErrorCode";
        OperationInfo operationInfo = new OperationInfo(null, new OperationType().setErrorCode(expectedErrorCode));

        // Act
        String actualErrorCode = operationInfo.getErrorCode();

        // Assert
        assertEquals(expectedErrorCode, actualErrorCode);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testGetSetErrorMessage() {
        // Arrange
        String expectedErrorMessage = "testErrorMessage";
        OperationInfo operationInfo = new OperationInfo(null, new OperationType().setErrorMessage(expectedErrorMessage));

        // Act
        String actualErrorMessage = operationInfo.getErrorMessage();

        // Assert
        assertEquals(expectedErrorMessage, actualErrorMessage);
    }

}
