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

package com.microsoft.windowsazure.management.website;

import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.management.websites.models.ServerFarmListResponse;
import com.microsoft.windowsazure.management.websites.models.WebSite;
import com.microsoft.windowsazure.management.websites.models.WebSiteListParameters;
import com.microsoft.windowsazure.management.websites.models.WebSpaceAvailabilityState;
import com.microsoft.windowsazure.management.websites.models.WebSpacesCreatePublishingUserParameters;
import com.microsoft.windowsazure.management.websites.models.WebSpacesCreatePublishingUserResponse;
import com.microsoft.windowsazure.management.websites.models.WebSpacesGetDnsSuffixResponse;
import com.microsoft.windowsazure.management.websites.models.WebSpacesGetResponse;
import com.microsoft.windowsazure.management.websites.models.WebSpacesListGeoRegionsResponse;
import com.microsoft.windowsazure.management.websites.models.WebSpacesListPublishingUsersResponse;
import com.microsoft.windowsazure.management.websites.models.WebSpacesListResponse;
import com.microsoft.windowsazure.management.websites.models.WebSpacesListWebSitesResponse;

public class WebSpaceOperationsTests extends WebSiteManagementIntegrationTestBase {
    @BeforeClass
    public static void setup() throws Exception {
        createService();
        cleanup();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        String webSpaceName = "northcentraluswebspace"; 
        try {
            webSiteManagementClient.getServerFarmsOperations().delete(webSpaceName);
        }
        catch (ServiceException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Ignore("Currently, when there are co-admin on the subscription, this test cannot pass.")
    public void createWebSpaceSuccess() throws Exception {
        String webSpaceName = "northcentraluswebspace"; 

        String username = "testWebSpaceUsername01";
        String userpassword = "testWebSpacePWD01";
        
        // Arrange
        WebSpacesCreatePublishingUserParameters createParameters = new WebSpacesCreatePublishingUserParameters();
        createParameters.setName(webSpaceName); 
        createParameters.setPublishingUserName(username);
        createParameters.setPublishingPassword(userpassword);
        
        // Act
        WebSpacesCreatePublishingUserResponse webSpaceCreateResponse = webSiteManagementClient.getWebSpacesOperations().createPublishingUser(username, userpassword, createParameters);
        
        // Assert
        Assert.assertEquals(200,  webSpaceCreateResponse.getStatusCode());
        Assert.assertNotNull( webSpaceCreateResponse.getRequestId());
        Assert.assertEquals(webSpaceName, webSpaceCreateResponse.getName());
    }

    @Test
    public void getWebSpaceSuccess() throws Exception {
        String webSpaceName = "eastuswebspace";

        // Act
        WebSpacesGetResponse webSpaceGetResponse = webSiteManagementClient.getWebSpacesOperations().get(webSpaceName);

        // Assert
        Assert.assertEquals(200, webSpaceGetResponse.getStatusCode());
        Assert.assertNotNull(webSpaceGetResponse.getRequestId()); 
        
        //Assert.assertEquals(3, webSpaceGetResponse.getCurrentNumberOfWorkers());
        //Assert.assertEquals(WebSpaceWorkerSize.Medium, webSpaceGetResponse.getCurrentWorkerSize());       
        //Assert.assertEquals("eastuswebspace", webSpaceGetResponse.getName());  
//        Assert.assertEquals(WebSpaceStatus.Ready, webSpaceGetResponse.getStatus());
//        Assert.assertEquals(WebSpaceWorkerSize.Medium, webSpaceGetResponse.getWorkerSize());
//        
//        Assert.assertEquals(WebSpaceAvailabilityState.Normal, webSpaceGetResponse.getAvailabilityState());  
//        Assert.assertEquals("East US", webSpaceGetResponse.getGeoLocation());  
//        Assert.assertEquals(3, webSpaceGetResponse.getWorkerSize());
    }

    @Test
    public void getDnsSuffixSuccess() throws Exception {    	
        WebSpacesGetDnsSuffixResponse  webSpacesGetDnsSuffixResponse = webSiteManagementClient.getWebSpacesOperations().getDnsSuffix();
        // Assert
        Assert.assertEquals(200, webSpacesGetDnsSuffixResponse.getStatusCode());
        Assert.assertNotNull(webSpacesGetDnsSuffixResponse.getRequestId()); 
        Assert.assertEquals("azurewebsites.net", webSpacesGetDnsSuffixResponse.getDnsSuffix());
    }

    @Test
    public void listPublishingUsersSuccess() throws Exception {
        // Act
        WebSpacesListPublishingUsersResponse webSpacesListPublishingUsersResponse = webSiteManagementClient.getWebSpacesOperations().listPublishingUsers();

        // Assert
        Assert.assertEquals(200,   webSpacesListPublishingUsersResponse.getStatusCode());
        Assert.assertNotNull( webSpacesListPublishingUsersResponse.getRequestId()); 

        ArrayList< WebSpacesListPublishingUsersResponse.User> userlist =  webSpacesListPublishingUsersResponse.getUsers(); 
        for (WebSpacesListPublishingUsersResponse.User user : userlist) { 
             Assert.assertNotNull(user.getName());
        }
    }
    
    @Test
    public void listGeoRegionsSuccess() throws Exception {
        // Act
        WebSpacesListGeoRegionsResponse  webSpacesListGeoRegionsResponse = webSiteManagementClient.getWebSpacesOperations().listGeoRegions();
        // Assert
        Assert.assertEquals(200,  webSpacesListGeoRegionsResponse.getStatusCode());
        Assert.assertNotNull(webSpacesListGeoRegionsResponse.getRequestId());    

        ArrayList<WebSpacesListGeoRegionsResponse.GeoRegion> geoRegionslist = webSpacesListGeoRegionsResponse.getGeoRegions(); 
        for (WebSpacesListGeoRegionsResponse.GeoRegion geoRegion : geoRegionslist) { 
            Assert.assertNotNull(geoRegion.getName());
        }
    }
    
    @Test
    public void listWebSpaceSuccess() throws Exception {
        // Act
        WebSpacesListResponse webSpacesListResponse = webSiteManagementClient.getWebSpacesOperations().list();
        // Assert
        Assert.assertEquals(200,  webSpacesListResponse.getStatusCode());
        Assert.assertNotNull( webSpacesListResponse.getRequestId());

        ArrayList<WebSpacesListResponse.WebSpace> webSpacelist = webSpacesListResponse.getWebSpaces(); 
        for (WebSpacesListResponse.WebSpace  webspace : webSpacelist) {
            Assert.assertNotNull(webspace.getAvailabilityState());
            Assert.assertNotNull(webspace.getName()); 
        }
    }
    
    @Test
    public void listWebSitesSuccess() throws Exception {
        String webSpaceName = "eastuswebspace"; 
        WebSiteListParameters  webSiteListParameters = new  WebSiteListParameters();
        ArrayList<String> propertiesToInclude = new ArrayList<String>();
        webSiteListParameters.setPropertiesToInclude(propertiesToInclude);

        // Act
        WebSpacesListWebSitesResponse webSpacesListWebSitesResponse = webSiteManagementClient.getWebSpacesOperations().listWebSites(webSpaceName, webSiteListParameters);

        // Assert
        Assert.assertEquals(200, webSpacesListWebSitesResponse.getStatusCode());
        Assert.assertNotNull(webSpacesListWebSitesResponse.getRequestId());
        
        ArrayList<WebSite> webSiteslist = webSpacesListWebSitesResponse.getWebSites(); 
        for (WebSite  webSite : webSiteslist) { 
             //Assert
             Assert.assertEquals(WebSpaceAvailabilityState.Normal, webSite.getAvailabilityState());
             Assert.assertNotNull(webSite.getName()); 
        }
    }
}