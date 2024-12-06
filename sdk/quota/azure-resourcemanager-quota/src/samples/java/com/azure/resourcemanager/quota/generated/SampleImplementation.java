package com.example;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.ProxyResource;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.quota.QuotaManager;
import com.azure.resourcemanager.quota.fluent.models.GroupQuotasEntityInner;
import com.azure.resourcemanager.quota.fluent.models.QuotaAllocationRequestStatusInner;
import com.azure.resourcemanager.quota.fluent.models.SubmittedResourceRequestStatusInner;
import com.azure.resourcemanager.quota.models.AdditionalAttributes;
import com.azure.resourcemanager.quota.models.EnvironmentType;
import com.azure.resourcemanager.quota.models.GroupQuotaLimit;
import com.azure.resourcemanager.quota.models.GroupQuotaRequestBase;
import com.azure.resourcemanager.quota.models.GroupQuotaSubscriptionId;
import com.azure.resourcemanager.quota.models.GroupQuotasEntity;
import com.azure.resourcemanager.quota.models.GroupQuotasEntityBase;
import com.azure.resourcemanager.quota.models.GroupingId;
import com.azure.resourcemanager.quota.models.GroupingIdType;
import com.azure.resourcemanager.quota.models.QuotaAllocationRequestBase;
import com.azure.resourcemanager.quota.models.SubmittedResourceRequestStatus;
import com.azure.resourcemanager.quota.models.SubmittedResourceRequestStatusProperties;
import com.azure.resourcemanager.quota.models.SubscriptionQuotaAllocations;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
        TokenCredential credential = new DefaultAzureCredentialBuilder()
            .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
            .build();
        QuotaManager manager = QuotaManager
            .authenticate(credential, profile);
        System.out.println( "Hello Test sdsdsds World!" );

        //Create Group Quota
        //createGroupQuota(manager);
        getGroupQuota(manager);

        //Add Subscription
        addSubscription(manager);
        getSubscription(manager);

        //Update Group Quota Limit
        updateGroupQuotaLimit(manager);
        getGroupQuotaLimit(manager);

        //AllocateQuotaToSub
        allocateQuotaToSubscription(manager);
        getSubscriptionAllocation(manager);

        deleteSubscription(manager);
    }

    public static void createGroupQuota(QuotaManager manager){
        ProxyResource resource = manager.groupQuotas()
            .createOrUpdate("testMgIdRoot", "java-sdk-test-gq",
                new GroupQuotasEntityInner()
                    .withProperties(
                        new GroupQuotasEntityBase().withDisplayName("javaTestGq")
                            .withAdditionalAttributes(
                                new AdditionalAttributes()
                                    .withGroupId(new GroupingId().withGroupingIdType(GroupingIdType.BILLING_ID)
                                        .withValue("yourBillingId")))),
                com.azure.core.util.Context.NONE);

        System.out.println(resource);
    }

    public static void getGroupQuota(QuotaManager manager){
        Response<GroupQuotasEntity> groupQuota = manager.groupQuotas()
        .getWithResponse("testMgIdRoot", "java-sdk-test-gq", com.azure.core.util.Context.NONE);

        System.out.println(groupQuota);
    }

    public static void addSubscription(QuotaManager manager){
        manager.groupQuotaSubscriptions()
            .createOrUpdate("testMgIdRoot", "java-sdk-test-gq", com.azure.core.util.Context.NONE);
    }

    public static void getSubscription(QuotaManager manager){
        GroupQuotaSubscriptionId subId = manager.groupQuotaSubscriptions().get("testMgIdRoot", "java-sdk-test-gq");
        System.out.println(subId);
    }

    public static void deleteSubscription(QuotaManager manager){
        manager.groupQuotaSubscriptions().deleteByResourceGroup("testMgIdRoot", "java-sdk-test-gq");
    }

    public static void updateGroupQuotaLimit(QuotaManager manager) {
        manager.groupQuotaLimitsRequests()
            .createOrUpdate(
                "testMgIdRoot", 
                "java-sdk-test-gq", 
                "Microsoft.Compute",
                "cores", 
                new SubmittedResourceRequestStatusInner().withProperties(
                    new SubmittedResourceRequestStatusProperties().withRequestedResource(
                        new GroupQuotaRequestBase()
                            .withRegion("westus")
                            .withComments("test java sdk group quota limit request")
                            .withLimit((long)200)
                    )
                ), 
                com.azure.core.util.Context.NONE);
    }

    public static void getGroupQuotaLimit(QuotaManager manager){
        PagedIterable<SubmittedResourceRequestStatus> limitList = manager.groupQuotaLimitsRequests().list("testMgIdRoot","java-sdk-test-gq", "Microsoft.Compute", "location eq westus");
        System.out.println(limitList);
        
        Response<GroupQuotaLimit> limit =  manager.groupQuotaLimits()
            .getWithResponse("testMgIdRoot", "java-sdk-test-gq", "Microsoft.Compute", "cores",
                "location eq westus", com.azure.core.util.Context.NONE);

        System.out.println(limit);
    }

    public static void allocateQuotaToSubscription(QuotaManager manager){
        manager.groupQuotaSubscriptionAllocationRequests()
            .createOrUpdate("testMgIdRoot", "java-sdk-test-gq", "Microsoft.Compute",
                "cores",
                new QuotaAllocationRequestStatusInner()
                    .withRequestedResource(new QuotaAllocationRequestBase().withLimit(20L).withRegion("westus")),
                com.azure.core.util.Context.NONE);
    }

    public static void getSubscriptionAllocation(QuotaManager manager){
        SubscriptionQuotaAllocations allocatedToSubs = manager.groupQuotaSubscriptionAllocations().get("testMgIdRoot", "java-sdk-test-gq", "cores",
                "location eq westus");

        System.out.println(allocatedToSubs);
    }


}
