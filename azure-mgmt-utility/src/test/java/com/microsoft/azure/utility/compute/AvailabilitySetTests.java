package com.microsoft.azure.utility.compute;

import com.microsoft.azure.management.compute.models.*;
import com.microsoft.azure.utility.ComputeHelper;
import com.microsoft.azure.utility.ResourceContext;
import com.microsoft.windowsazure.core.OperationResponse;
import com.microsoft.windowsazure.exception.ServiceException;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.junit.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class AvailabilitySetTests extends ComputeTestBase {
    private static final int fdTooLow = -1;
    private static final int fdTooHigh = 4;
    private static final int udTooLow = 0;
    private static final int udTooHigh = 21;

    private static final int defaultFd = 3;
    private static final int defaultUd = 5;

    private static final int nonDefaultFd = 2;
    private static final int nonDefaultUd = 4;

    static {
        log = LogFactory.getLog(AvailabilitySetTests.class);
    }

    @BeforeClass
    public static void setup() throws Exception {
        ensureClientsInitialized();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        log.debug("after class, clean resource group: " + rgName);
        cleanupResourceGroup();
    }

    @Before
    public void beforeTest() throws Exception {
        setupTest();
        createOrUpdateResourceGroup(ComputeTestBase.rgName);
    }

    @After
    public void afterTest() throws Exception {
        resetTest();
    }

    @Test
    public void verifyInvalidFDFail() throws Exception {
        verifyInvalidFDValuesFail("fdTooHigh", fdTooHigh);
        verifyInvalidFDValuesFail("fdTooLow", fdTooLow);
    }

    @Test
    public void verifyInvalidUDFail() throws Exception {
        verifyInvalidUDValuesFail("udTooHigh", udTooHigh);
        verifyInvalidUDValuesFail("udTooLow", udTooLow);
    }

    @Test
    public void verifyDefaultFDUDValuesSucceed() throws Exception {
        AvailabilitySet avSet = createAvailabilitySetWithUDFD("defaultUDFD", defaultUd, defaultFd);

        ResourceContext context = ComputeTestBase.createTestResourceContext(true);
        AvailabilitySetCreateOrUpdateResponse response = computeManagementClient.getAvailabilitySetsOperations()
                .createOrUpdate(context.getResourceGroupName(), avSet);

        ValidateCreateOrUpdateAvSetResponse(response, avSet);
        VerifyAvSetInResourceGroup(avSet, rgName);
        VerifyGetAvSet(avSet, rgName);
        VerifyGetVmSizeInAvSet(avSet);

        DeleteAvSet(avSet.getName());
    }

    @Test
    public void verifyNonDefaultFDUDValuesSucceed() throws Exception {
        // Negative tests for a bug in 5.0.0 that read-only fields have side-effect on the request body
        InstanceViewStatus testStatus = new InstanceViewStatus();
        testStatus.setCode("test");
        testStatus.setDisplayStatus("test");
        testStatus.setMessage("test");

        AvailabilitySet avSet = createAvailabilitySetWithUDFD("nondefaultUDFD", nonDefaultUd, nonDefaultFd);
        avSet.setStatuses(new ArrayList<InstanceViewStatus>(Arrays.asList(testStatus)));

        ResourceContext context = ComputeTestBase.createTestResourceContext(true);
        AvailabilitySetCreateOrUpdateResponse response = computeManagementClient.getAvailabilitySetsOperations()
                .createOrUpdate(context.getResourceGroupName(), avSet);

        ValidateCreateOrUpdateAvSetResponse(response, avSet);
        VerifyAvSetInResourceGroup(avSet, rgName);
        VerifyGetAvSet(avSet, rgName);
        VerifyGetVmSizeInAvSet(avSet);

        DeleteAvSet(avSet.getName());
    }

    @Test
    public void testGetAVSetId() {
        String uuid = UUID.randomUUID().toString();
        Assert.assertEquals(ComputeTestHelper.getAvailabilitySetRef(uuid, "rg", "avset"),
                String.format("/subscriptions/%s/resourceGroups/rg/providers/Microsoft.Compute/availabilitySets/avset", uuid));
    }

    public void verifyInvalidUDValuesFail(String asName, int ud) throws Exception {
        AvailabilitySet avSet = createAvailabilitySetWithUDFD(asName, ud, defaultFd);
        avSet.setPlatformUpdateDomainCount(ud);

        ResourceContext context = ComputeTestBase.createTestResourceContext(true);
        try {
            ComputeHelper.createAvailabilitySet(computeManagementClient, avSet, context);
        } catch (ServiceException exception) {
            Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, exception.getHttpStatusCode());
            Assert.assertEquals(exception.getError().getCode(), "InvalidParameter");
        }
    }

    public void verifyInvalidFDValuesFail(String asName, int fd) throws Exception {
        AvailabilitySet avSet = createAvailabilitySetWithUDFD(asName, defaultUd, fd);
        avSet.setPlatformFaultDomainCount(fd);

        ResourceContext context = ComputeTestBase.createTestResourceContext(true);
        try {
            ComputeHelper.createAvailabilitySet(computeManagementClient, avSet, context);
        } catch (ServiceException exception) {
            Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, exception.getHttpStatusCode());
            Assert.assertEquals(exception.getError().getCode(), "InvalidParameter");
        }
    }

    private AvailabilitySet createAvailabilitySetWithUDFD(String asName, int ud, int fd) {
        HashMap<String, String> avTag = new HashMap<String, String>() {
            { put("RG", "rg"); }
            { put("testTag", "1"); }
        };

        AvailabilitySet avSet = new AvailabilitySet(m_location);
        avSet.setName(generateName(asName));
        avSet.setTags(avTag);
        avSet.setPlatformUpdateDomainCount(ud);
        avSet.setPlatformFaultDomainCount(fd);

        return avSet;
    }

    private void VerifyAvSetInResourceGroup(AvailabilitySet expectedAvSet, String rgName) throws Exception {
        AvailabilitySetListResponse response = computeManagementClient.getAvailabilitySetsOperations().list(rgName);
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatusCode());

        ArrayList<AvailabilitySet> avSetList = response.getAvailabilitySets();
        for(AvailabilitySet avSet : avSetList) {
            if (avSet.getName().equals(expectedAvSet.getName())) {
                ValidateAvSetModel(avSet, expectedAvSet);
                break;
            }
        }
    }

    private void ValidateAvSetModel(AvailabilitySet thisAvSet, AvailabilitySet thatAvSet) {
        Assert.assertEquals(thatAvSet.getName(), thisAvSet.getName());
        Assert.assertEquals(thatAvSet.getLocation().toLowerCase(), thisAvSet.getLocation().toLowerCase());
        Assert.assertTrue(thatAvSet.getPlatformFaultDomainCount() == thisAvSet.getPlatformFaultDomainCount());
        Assert.assertTrue(thatAvSet.getPlatformUpdateDomainCount() == thisAvSet.getPlatformUpdateDomainCount());

        Assert.assertNotNull(thatAvSet.getTags());
        Assert.assertNotNull(thisAvSet.getTags());

        for(String tag : thatAvSet.getTags().keySet()) {
            Assert.assertEquals(thatAvSet.getTags().get(tag), thisAvSet.getTags().get(tag));
        }
    }

    private void ValidateGetAvSetResponse(AvailabilitySetGetResponse response,
                                          AvailabilitySet expectedAvSet) {
        Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_OK);

        AvailabilitySet outputAvSet = response.getAvailabilitySet();
        ValidateAvSetModel(outputAvSet, expectedAvSet);
    }

    private void ValidateCreateOrUpdateAvSetResponse(AvailabilitySetCreateOrUpdateResponse response,
                                                     AvailabilitySet expectedAvSet) {
        Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_OK);

        AvailabilitySet outputAvSet = response.getAvailabilitySet();
        ValidateAvSetModel(outputAvSet, expectedAvSet);
    }

    private void VerifyGetAvSet(AvailabilitySet expectedAvSet, String rgName) throws Exception {
        AvailabilitySetGetResponse response = computeManagementClient.getAvailabilitySetsOperations().get(rgName, expectedAvSet.getName());
        Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_OK);
        ValidateGetAvSetResponse(response, expectedAvSet);
    }

    private void DeleteAvSet(String avSetName) throws Exception {
        OperationResponse response = computeManagementClient.getAvailabilitySetsOperations().delete(rgName, avSetName);
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatusCode());
    }

    private void VerifyGetVmSizeInAvSet(AvailabilitySet avSet) throws Exception {
        VirtualMachineSizeListResponse listVmSizesResponse = computeManagementClient.getAvailabilitySetsOperations().listAvailableSizes(rgName, avSet.getName());
        Assert.assertEquals(listVmSizesResponse.getStatusCode(), HttpStatus.SC_OK);
        ComputeTestHelper.validateVirtualMachineSizeListResponse(listVmSizesResponse);
    }
}
