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

package com.microsoft.windowsazure.services.media;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.media.models.EncodingReservedUnit;
import com.microsoft.windowsazure.services.media.models.EncodingReservedUnitInfo;
import com.microsoft.windowsazure.services.media.models.EncodingReservedUnitType;
import com.microsoft.windowsazure.services.media.models.OperationState;

public class EncodingReservedUnitTypeIntegrationTest extends IntegrationTestBase {
    private static EncodingReservedUnitInfo encodingReservedUnitInfo;
    
    @BeforeClass
    public static void beforeTesting() throws ServiceException {
        encodingReservedUnitInfo = service.get(EncodingReservedUnit.get());
    }
    
    @AfterClass
    public static void afterTesting() throws ServiceException {
        String opId = service.update(EncodingReservedUnit.update(encodingReservedUnitInfo)
                .setCurrentReservedUnits(encodingReservedUnitInfo.getCurrentReservedUnits())
                .setReservedUnitType(encodingReservedUnitInfo.getReservedUnitType()));
        
        OperationUtils.await(service, opId);
    }

    @Test
    public void getEncodingReservedUnitType() throws Exception {
        // Arrange
        // Act
        EncodingReservedUnitInfo encodingReservedUnitInfo = service.get(EncodingReservedUnit.get());
        
        // Assert
        assertNotNull(encodingReservedUnitInfo);        
    }
    
    @Test
    public void updateCurrentEncodingReservedUnits() throws Exception {
        // Arrange
        int expectedReservedUnits = 2;
        EncodingReservedUnitInfo encodingReservedUnitInfo = service.get(EncodingReservedUnit.get());
        assertNotNull(encodingReservedUnitInfo);
        
        // Act
        String opId = service.update(EncodingReservedUnit.update(encodingReservedUnitInfo)
                .setCurrentReservedUnits(expectedReservedUnits));
        
        OperationState state = OperationUtils.await(service, opId);
        
        encodingReservedUnitInfo = service.get(EncodingReservedUnit.get());
        
        // Assert
        assertEquals(OperationState.Succeeded, state);
        assertNotNull(encodingReservedUnitInfo);
        assertEquals(encodingReservedUnitInfo.getCurrentReservedUnits(), expectedReservedUnits);
    }
    
    @Test
    public void updateTypeofEncodingReservedUnits() throws Exception {
        // Arrange
        EncodingReservedUnitType expectedReservedUnitType = EncodingReservedUnitType.Standard;
        EncodingReservedUnitInfo encodingReservedUnitInfo = service.get(EncodingReservedUnit.get());
        assertNotNull(encodingReservedUnitInfo);
        
        // Act
        String opId = service.update(EncodingReservedUnit.update(encodingReservedUnitInfo)
                .setReservedUnitType(expectedReservedUnitType));
        
        OperationState state = OperationUtils.await(service, opId);
        
        encodingReservedUnitInfo = service.get(EncodingReservedUnit.get());
        
        // Assert
        assertEquals(OperationState.Succeeded, state);
        assertNotNull(encodingReservedUnitInfo);
        assertEquals(encodingReservedUnitInfo.getReservedUnitType(), expectedReservedUnitType);
    }
}
