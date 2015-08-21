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

public class VMAvailabilitySetTests extends ComputeTestBase {
    private static final int fdTooLow = -1;
    private static final int fdTooHigh = 4;
    private static final int udTooLow = 0;
    private static final int udTooHigh = 21;

    private static final int defaultFd = 3;
    private static final int defaultUd = 5;

    private static final int nonDefaultFd = 2;
    private static final int nonDefaultUd = 4;

    static {
        log = LogFactory.getLog(VMAvailabilitySetTests.class);
    }

    @BeforeClass
    public static void setup() throws Exception {
        ensureClientsInitialized();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        log.debug("after class, clean resource group: " + m_rgName);
        cleanupResourceGroup();
    }

    @Before
    public void beforeTest() throws Exception {
        setupTest();
        createOrUpdateResourceGroup(ComputeTestBase.m_rgName);
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
        AvailabilitySet availabilitySet = createAvailabilitySetWithUDFD("defaultUDFD", defaultUd, defaultFd);

        ResourceContext context = ComputeTestBase.createTestResourceContext(true);
        AvailabilitySetCreateOrUpdateResponse response = computeManagementClient.getAvailabilitySetsOperations()
                .createOrUpdate(context.getResourceGroupName(), availabilitySet);

        validateCreateOrUpdateAvailabilitySetResponse(response, availabilitySet);
        verifyAvailabilitySetInResourceGroup(availabilitySet, m_rgName);
        verifyGetAvailabilitySet(availabilitySet, m_rgName);
        verifyGetVmSizeInAvailabilitySet(availabilitySet);

        deleteAvailabilitySet(availabilitySet.getName());
    }

    @Test
    public void verifyNonDefaultFDUDValuesSucceed() throws Exception {
        // Negative tests for a bug in 5.0.0 that read-only fields have side-effect on the request body
        InstanceViewStatus testStatus = new InstanceViewStatus();
        testStatus.setCode("test");
        testStatus.setDisplayStatus("test");
        testStatus.setMessage("test");

        AvailabilitySet availabilitySet = createAvailabilitySetWithUDFD("nondefaultUDFD", nonDefaultUd, nonDefaultFd);
        availabilitySet.setStatuses(new ArrayList<InstanceViewStatus>(Arrays.asList(testStatus)));

        ResourceContext context = ComputeTestBase.createTestResourceContext(true);
        AvailabilitySetCreateOrUpdateResponse response = computeManagementClient.getAvailabilitySetsOperations()
                .createOrUpdate(context.getResourceGroupName(), availabilitySet);

        validateCreateOrUpdateAvailabilitySetResponse(response, availabilitySet);
        verifyAvailabilitySetInResourceGroup(availabilitySet, m_rgName);
        verifyGetAvailabilitySet(availabilitySet, m_rgName);
        verifyGetVmSizeInAvailabilitySet(availabilitySet);

        deleteAvailabilitySet(availabilitySet.getName());
    }

    @Test
    public void testGetAvailabilitySetId() {
        String uuid = UUID.randomUUID().toString();
        Assert.assertEquals(ComputeTestHelper.getAvailabilitySetRef(uuid, "rg", "avset"),
                String.format("/subscriptions/%s/resourceGroups/rg/providers/Microsoft.Compute/availabilitySets/avset", uuid));
    }

    public void verifyInvalidUDValuesFail(String asName, int ud) throws Exception {
        AvailabilitySet availabilitySet = createAvailabilitySetWithUDFD(asName, ud, defaultFd);
        availabilitySet.setPlatformUpdateDomainCount(ud);

        ResourceContext context = ComputeTestBase.createTestResourceContext(true);
        try {
            ComputeHelper.createAvailabilitySet(computeManagementClient, availabilitySet, context);
        } catch (ServiceException exception) {
            Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, exception.getHttpStatusCode());
            Assert.assertEquals(exception.getError().getCode(), "InvalidParameter");
        }
    }

    public void verifyInvalidFDValuesFail(String asName, int fd) throws Exception {
        AvailabilitySet availabilitySet = createAvailabilitySetWithUDFD(asName, defaultUd, fd);
        availabilitySet.setPlatformFaultDomainCount(fd);

        ResourceContext context = ComputeTestBase.createTestResourceContext(true);
        try {
            ComputeHelper.createAvailabilitySet(computeManagementClient, availabilitySet, context);
        } catch (ServiceException exception) {
            Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, exception.getHttpStatusCode());
            Assert.assertEquals(exception.getError().getCode(), "InvalidParameter");
        }
    }

    private AvailabilitySet createAvailabilitySetWithUDFD(String availabilitySetName, int ud, int fd) {
        HashMap<String, String> avTag = new HashMap<String, String>() {
            { put("RG", "rg"); }
            { put("testTag", "1"); }
        };

        AvailabilitySet availabilitySet = new AvailabilitySet(m_location);
        availabilitySet.setName(generateName(availabilitySetName));
        availabilitySet.setTags(avTag);
        availabilitySet.setPlatformUpdateDomainCount(ud);
        availabilitySet.setPlatformFaultDomainCount(fd);

        return availabilitySet;
    }

    private void verifyAvailabilitySetInResourceGroup(AvailabilitySet expectedAvailabilitySet, String rgName)
            throws Exception {
        AvailabilitySetListResponse response = computeManagementClient.getAvailabilitySetsOperations().list(rgName);
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatusCode());

        ArrayList<AvailabilitySet> avSetList = response.getAvailabilitySets();
        for(AvailabilitySet avSet : avSetList) {
            if (avSet.getName().equals(expectedAvailabilitySet.getName())) {
                validateAvailabilitySetModel(avSet, expectedAvailabilitySet);
                break;
            }
        }
    }

    private void validateAvailabilitySetModel(AvailabilitySet availabilitySet, AvailabilitySet expectedAvailabilitySet) {
        Assert.assertEquals(expectedAvailabilitySet.getName(), availabilitySet.getName());
        Assert.assertEquals(expectedAvailabilitySet.getLocation().toLowerCase(), availabilitySet.getLocation().toLowerCase());
        Assert.assertTrue(expectedAvailabilitySet.getPlatformFaultDomainCount() == availabilitySet.getPlatformFaultDomainCount());
        Assert.assertTrue(expectedAvailabilitySet.getPlatformUpdateDomainCount() == availabilitySet.getPlatformUpdateDomainCount());

        Assert.assertNotNull(expectedAvailabilitySet.getTags());
        Assert.assertNotNull(availabilitySet.getTags());

        for(String tag : expectedAvailabilitySet.getTags().keySet()) {
            Assert.assertEquals(expectedAvailabilitySet.getTags().get(tag), availabilitySet.getTags().get(tag));
        }
    }

    private void validateGetAvailabilitySetResponse(AvailabilitySetGetResponse response,
                                                    AvailabilitySet expectedAvailabilitySet) {
        Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_OK);

        AvailabilitySet outputAvailabilitySet = response.getAvailabilitySet();
        validateAvailabilitySetModel(outputAvailabilitySet, expectedAvailabilitySet);
    }

    private void validateCreateOrUpdateAvailabilitySetResponse(AvailabilitySetCreateOrUpdateResponse response,
                                                               AvailabilitySet expectedAvailabilitySet) {
        Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_OK);

        AvailabilitySet outputAvailabilitySet = response.getAvailabilitySet();
        validateAvailabilitySetModel(outputAvailabilitySet, expectedAvailabilitySet);
    }

    private void verifyGetAvailabilitySet(AvailabilitySet expectedAvailabilitySet, String rgName) throws Exception {
        AvailabilitySetGetResponse response =
                computeManagementClient.getAvailabilitySetsOperations().get(rgName, expectedAvailabilitySet.getName());
        Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_OK);
        validateGetAvailabilitySetResponse(response, expectedAvailabilitySet);
    }

    private void deleteAvailabilitySet(String availabilitySetName) throws Exception {
        OperationResponse response =
                computeManagementClient.getAvailabilitySetsOperations().delete(m_rgName, availabilitySetName);
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatusCode());
    }

    private void verifyGetVmSizeInAvailabilitySet(AvailabilitySet availabilitySet) throws Exception {
        VirtualMachineSizeListResponse listVmSizesResponse =
                computeManagementClient.getAvailabilitySetsOperations()
                        .listAvailableSizes(m_rgName, availabilitySet.getName());

        Assert.assertEquals(listVmSizesResponse.getStatusCode(), HttpStatus.SC_OK);
        ComputeTestHelper.validateVirtualMachineSizeListResponse(listVmSizesResponse);
    }
}
