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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.windowsazure.services.media.implementation.content.AkamaiAccessControlType;
import com.microsoft.windowsazure.services.media.implementation.content.AkamaiSignatureHeaderAuthenticationKey;
import com.microsoft.windowsazure.services.media.implementation.content.CrossSiteAccessPoliciesType;
import com.microsoft.windowsazure.services.media.implementation.content.IPAccessControlType;
import com.microsoft.windowsazure.services.media.implementation.content.IPRangeType;
import com.microsoft.windowsazure.services.media.implementation.content.StreamingEndpointAccessControlType;
import com.microsoft.windowsazure.services.media.implementation.content.StreamingEndpointCacheControlType;
import com.microsoft.windowsazure.services.media.models.ListResult;
import com.microsoft.windowsazure.services.media.models.OperationState;
import com.microsoft.windowsazure.services.media.models.StreamingEndpoint;
import com.microsoft.windowsazure.services.media.models.StreamingEndpointInfo;
import com.microsoft.windowsazure.services.media.models.StreamingEndpointState;

public class StreamingEndopointIntegrationTest extends IntegrationTestBase {

	@Test
    public void streamingEndpointCreateListByNameAndDelete() throws Exception {
        // Arrange
        String expectedName = testStreamingEndPointPrefix + "ListByNameTest";
        StreamingEndpointInfo streamingEndpointInfo = service.create(StreamingEndpoint.create().setName(expectedName));
        
        OperationUtils.await(service, streamingEndpointInfo);
        
        // Act
        ListResult<StreamingEndpointInfo> listStreamingEndpointResult = service.list(StreamingEndpoint.list()
                .set("$filter", "(Name eq '" + expectedName + "')"));

        // Assert
        assertNotNull(listStreamingEndpointResult);
        assertEquals(1, listStreamingEndpointResult.size());
        StreamingEndpointInfo info = listStreamingEndpointResult.get(0);
        assertNotNull(info);
        assertEquals(info.getName(), expectedName);
        
        // Cleanup
        String deleteOpId = service.delete(StreamingEndpoint.delete(info.getId()));
        OperationState state = OperationUtils.await(service, deleteOpId);
        // Assert Cleanup
        assertEquals(state, OperationState.Succeeded);
    }
    
    @Test
    public void streamingEndpointCreateStartStopDeleteTest() throws Exception {
        // Arrange
        String expectedName = testStreamingEndPointPrefix + "Startable";
        StreamingEndpointInfo streamingEndpointInfo = service.create(StreamingEndpoint.create().setName(expectedName));
        
        OperationUtils.await(service, streamingEndpointInfo);
        
        // Act
        String startingOpId = service.action(StreamingEndpoint.start(streamingEndpointInfo.getId()));
        OperationState state = OperationUtils.await(service, startingOpId);
        
        // Assert
        assertEquals(state, OperationState.Succeeded);        
        streamingEndpointInfo = service.get(StreamingEndpoint.get(streamingEndpointInfo.getId()));
        assertNotNull(streamingEndpointInfo);        
        assertEquals(StreamingEndpointState.Running, streamingEndpointInfo.getState());
        
        // Act 2
        startingOpId = service.action(StreamingEndpoint.stop(streamingEndpointInfo.getId())); 
        state = OperationUtils.await(service, startingOpId);
        
        // Assert 2
        assertEquals(state, OperationState.Succeeded);
        
        // Cleanup
        String deleteOpId = service.delete(StreamingEndpoint.delete(streamingEndpointInfo.getId()));
        state = OperationUtils.await(service, deleteOpId);
        // Assert Cleanup
        assertEquals(state, OperationState.Succeeded);
    }
    
    @Test
    public void streamingEndpointCreateStartScaleStopDeleteTest() throws Exception {
        // Arrange
        int expectedScaleUnits = 2;
        String expectedName = testStreamingEndPointPrefix + "Scalable";
        StreamingEndpointInfo streamingEndpointInfo = service.create(StreamingEndpoint.create().setName(expectedName));
        
        OperationUtils.await(service, streamingEndpointInfo);
        
        // Act
        String startingOpId = service.action(StreamingEndpoint.start(streamingEndpointInfo.getId()));
        OperationState state = OperationUtils.await(service, startingOpId);
        
        // Assert
        assertEquals(state, OperationState.Succeeded);        
        streamingEndpointInfo = service.get(StreamingEndpoint.get(streamingEndpointInfo.getId()));
        assertNotNull(streamingEndpointInfo);        
        assertEquals(StreamingEndpointState.Running, streamingEndpointInfo.getState());
        
        startingOpId = service.action(StreamingEndpoint.scale(streamingEndpointInfo.getId(), expectedScaleUnits)); 
        state = OperationUtils.await(service, startingOpId);
        // Assert 3
        assertEquals(state, OperationState.Succeeded);
        streamingEndpointInfo = service.get(StreamingEndpoint.get(streamingEndpointInfo.getId()));
        assertNotNull(streamingEndpointInfo);
        assertEquals(expectedScaleUnits, streamingEndpointInfo.getScaleUnits());
        
        // Act 3
        startingOpId = service.action(StreamingEndpoint.stop(streamingEndpointInfo.getId())); 
        state = OperationUtils.await(service, startingOpId);
        // Assert 3
        assertEquals(state, OperationState.Succeeded);
        
        // Cleanup
        String deleteOpId = service.delete(StreamingEndpoint.delete(streamingEndpointInfo.getId()));
        state = OperationUtils.await(service, deleteOpId);
        // Assert Cleanup
        assertEquals(state, OperationState.Succeeded);
    }
    
    @Test
    public void streamingEndpointEnableCDNTest() throws Exception {
        // Arrange
        int expectedScaleUnits = 1;
        String expectedName = testStreamingEndPointPrefix + "EnableCDN";
        
        // Act 1
        StreamingEndpointInfo streamingEndpointInfo = service.create(StreamingEndpoint.create().setName(expectedName));
        OperationState state = OperationUtils.await(service, streamingEndpointInfo);
        // Assert 1
        assertEquals(state, OperationState.Succeeded); 
        
        // Act 2
        String opId = service.action(StreamingEndpoint.scale(streamingEndpointInfo.getId(), expectedScaleUnits));
        state = OperationUtils.await(service, opId);
        // Assert 2
        assertEquals(state, OperationState.Succeeded); 
        
        // Act 3
        opId = service.update(StreamingEndpoint.update(streamingEndpointInfo).setCdnEnabled(true));
        state = OperationUtils.await(service, opId);
        // Assert 3
        assertEquals(state, OperationState.Succeeded); 
        
        // Act 4
        streamingEndpointInfo = service.get(StreamingEndpoint.get(streamingEndpointInfo.getId()));
        // Assert 4
        assertTrue(streamingEndpointInfo.isCdnEnabled());
        
        // Cleanup
        String deleteOpId = service.delete(StreamingEndpoint.delete(streamingEndpointInfo.getId()));
        state = OperationUtils.await(service, deleteOpId);
        // Assert Cleanup
        assertEquals(state, OperationState.Succeeded);
    }
    
    @Test
    public void createAndRetrieveTheSameStreamingEndpointTest() throws Exception {
        // Arrange
        int expectedScaleUnits = 1;
        boolean expectedCdnState = false;
        String expectedName = testStreamingEndPointPrefix + "createAndRetrieve";
        String expectedDesc = "expected description";
        int expectedMaxAge = 1800;
        String expectedClientAccessPolicy = "<access-policy><cross-domain-access><policy><allow-from http-request-headers='*'><domain uri='http://*' /></allow-from><grant-to><resource path='/' include-subpaths='false' /></grant-to></policy></cross-domain-access></access-policy>";
        String expectedCrossDomainPolicy = "<?xml version='1.0'?><!DOCTYPE cross-domain-policy SYSTEM 'http://www.macromedia.com/xml/dtds/cross-domain-policy.dtd'><cross-domain-policy><allow-access-from domain='*' /></cross-domain-policy>";
        String expectedAkamaiIdentifier = "akamaikey";
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.HOUR, 48);
        Date expectedAkamaiExpiration = cal.getTime();
        String expectedAkamaiB64 = "/31iWKdqNC7YUnj8zQ3XHA==";
        String expectedIPAddress = "0.0.0.0";
        String expectedIPName = "Allow All";
        
        CrossSiteAccessPoliciesType expectedCrossSiteAccessPolicies = new CrossSiteAccessPoliciesType();
        expectedCrossSiteAccessPolicies.setClientAccessPolicy(expectedClientAccessPolicy);
        expectedCrossSiteAccessPolicies.setCrossDomainPolicy(expectedCrossDomainPolicy);
        
        IPAccessControlType expectedIP = new IPAccessControlType();
        expectedIP.setIpRange(new ArrayList<IPRangeType>());
        expectedIP.getIpRange().add(new IPRangeType());
        expectedIP.getIpRange().get(0).setAddress(expectedIPAddress);
        expectedIP.getIpRange().get(0).setName(expectedIPName);
        expectedIP.getIpRange().get(0).setSubnetPrefixLength(0);
        
        AkamaiAccessControlType expectedAkamai = new AkamaiAccessControlType();
        List<AkamaiSignatureHeaderAuthenticationKey> akamaiSignatureHeaderAuthenticationKeyList = 
                new ArrayList<AkamaiSignatureHeaderAuthenticationKey>();
        akamaiSignatureHeaderAuthenticationKeyList.add(new AkamaiSignatureHeaderAuthenticationKey());
        akamaiSignatureHeaderAuthenticationKeyList.get(0).setExpiration(expectedAkamaiExpiration);
        akamaiSignatureHeaderAuthenticationKeyList.get(0).setId(expectedAkamaiIdentifier);
        akamaiSignatureHeaderAuthenticationKeyList.get(0).setBase64Key(expectedAkamaiB64);
        expectedAkamai.setAkamaiSignatureHeaderAuthenticationKeyList(akamaiSignatureHeaderAuthenticationKeyList);
        
        StreamingEndpointAccessControlType expectedStreamingEndpointAccessControl = new StreamingEndpointAccessControlType();
        expectedStreamingEndpointAccessControl.setAkamai(expectedAkamai);       
        expectedStreamingEndpointAccessControl.setIP(expectedIP);
        
        StreamingEndpointCacheControlType expectedStreamingEndpointCacheControl = new StreamingEndpointCacheControlType();

        expectedStreamingEndpointCacheControl.setMaxAge(expectedMaxAge );
        
        // Act
        StreamingEndpointInfo streamingEndpointInfo = service.create(
                        StreamingEndpoint.create()
                            .setName(expectedName)
                            .setCdnEnabled(expectedCdnState)
                            .setDescription(expectedDesc)
                            .setScaleUnits(expectedScaleUnits)  
                            .setCrossSiteAccessPolicies(expectedCrossSiteAccessPolicies)
                            .setAccessControl(expectedStreamingEndpointAccessControl)
                            .setCacheControl(expectedStreamingEndpointCacheControl )
                        );
        
        OperationState state = OperationUtils.await(service, streamingEndpointInfo);
        
        // Act validations
        assertEquals(OperationState.Succeeded, state);
        assertNotNull(streamingEndpointInfo);
        
        // Retrieve the StramingEndpoint again.
        StreamingEndpointInfo result = service.get(StreamingEndpoint.get(streamingEndpointInfo.getId()));
        
        // Assert
        assertNotNull(result);        
        assertEquals(result.getScaleUnits(), expectedScaleUnits);
        assertEquals(result.getName(), expectedName);
        assertEquals(result.getDescription(), expectedDesc);
        assertEquals(result.getCrossSiteAccessPolicies().getClientAccessPolicy(), expectedClientAccessPolicy);
        assertEquals(result.getCrossSiteAccessPolicies().getCrossDomainPolicy(), expectedCrossDomainPolicy);
        assertEquals(result.getCacheControl().getMaxAge(), expectedMaxAge);
        List<AkamaiSignatureHeaderAuthenticationKey> akamai = result.getAccessControl().getAkamai().getAkamaiSignatureHeaderAuthenticationKeyList();
        assertNotNull(akamai);  
        assertEquals(akamai.size(), 1);
        assertEquals(akamai.get(0).getId(), expectedAkamaiIdentifier);
        assertEquals(akamai.get(0).getExpiration(), expectedAkamaiExpiration);
        assertEquals(akamai.get(0).getBase64Key(), expectedAkamaiB64);
        List<IPRangeType> ip = result.getAccessControl().getIP().getIpRange();
        assertNotNull(ip);
        assertEquals(ip.size(), 1);
        assertEquals(ip.get(0).getAddress(), expectedIPAddress);
        assertEquals(ip.get(0).getName(), expectedIPName);
        assertEquals(ip.get(0).getSubnetPrefixLength(), 0);
        
        // Cleanup
        String deleteOpId = service.delete(StreamingEndpoint.delete(result.getId()));
        state = OperationUtils.await(service, deleteOpId);
        // Assert Cleanup
        assertEquals(state, OperationState.Succeeded);
    }

}
