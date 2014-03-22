package com.microsoft.windowsazure.management;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.windowsazure.management.models.ManagementCertificateGetResponse;
import com.microsoft.windowsazure.management.models.ManagementCertificateListResponse;

public class ManagementCertificateOperationsTests extends ManagementIntegrationTestBase {
    @BeforeClass
    public static void setup() throws Exception {
        createService();       
    }   
    
    @Test
    public void createManagementCertificate() throws Exception {
        // Arrange
    	
//    	ManagementCertificateCreateParameters createParameters = new ManagementCertificateCreateParameters();
////        createParameters.setData(dataValue);
////        createParameters.setPublicKey(publicKeyValue);
////        createParameters.setThumbprint(thumbprintValue);
//        
//        // Act
//        OperationResponse operationResponse = managementClient.getManagementCertificatesOperations().create(createParameters);
//        
//        // Assert
//        Assert.assertEquals(201, operationResponse.getStatusCode());
//        Assert.assertNotNull(operationResponse.getRequestId());
    }
    
    @Test
    public void getManagementCertificateSuccess() throws Exception {
    	
        // arrange
    	String thumbprint = "78669C92859BC1CAE1FA6A90BDA7B015048B2B1E";
    	ManagementCertificateGetResponse managementCertificateResponse = managementClient.getManagementCertificatesOperations().get(thumbprint);

        // Assert
        Assert.assertEquals(200, managementCertificateResponse.getStatusCode());
        Assert.assertNotNull(managementCertificateResponse.getRequestId()); 
        Assert.assertEquals(thumbprint, managementCertificateResponse.getThumbprint());
        Assert.assertEquals(GregorianCalendar.YEAR, managementCertificateResponse.getCreated().YEAR);        
    }
    
    @Test
    public void listManagementCertificateSuccess() throws Exception {
        // Arrange  
    	 ManagementCertificateListResponse managementCertificateListResponse = managementClient.getManagementCertificatesOperations().list();
    	 ArrayList<ManagementCertificateListResponse.SubscriptionCertificate> managementCertificatelist = managementCertificateListResponse.getSubscriptionCertificates();
         
    	 Assert.assertNotNull(managementCertificatelist);;        
    }
}
