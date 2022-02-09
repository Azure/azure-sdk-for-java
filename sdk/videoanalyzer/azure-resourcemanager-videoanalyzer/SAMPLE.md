# Code snippets and samples


## AccessPolicies

- [CreateOrUpdate](#accesspolicies_createorupdate)
- [Delete](#accesspolicies_delete)
- [Get](#accesspolicies_get)
- [List](#accesspolicies_list)
- [Update](#accesspolicies_update)

## EdgeModules

- [CreateOrUpdate](#edgemodules_createorupdate)
- [Delete](#edgemodules_delete)
- [Get](#edgemodules_get)
- [List](#edgemodules_list)
- [ListProvisioningToken](#edgemodules_listprovisioningtoken)

## LivePipelineOperationStatuses

- [Get](#livepipelineoperationstatuses_get)

## LivePipelines

- [Activate](#livepipelines_activate)
- [CreateOrUpdate](#livepipelines_createorupdate)
- [Deactivate](#livepipelines_deactivate)
- [Delete](#livepipelines_delete)
- [Get](#livepipelines_get)
- [List](#livepipelines_list)
- [Update](#livepipelines_update)

## Locations

- [CheckNameAvailability](#locations_checknameavailability)

## OperationResults

- [Get](#operationresults_get)

## OperationStatuses

- [Get](#operationstatuses_get)

## Operations

- [List](#operations_list)

## PipelineJobOperationStatuses

- [Get](#pipelinejoboperationstatuses_get)

## PipelineJobs

- [Cancel](#pipelinejobs_cancel)
- [CreateOrUpdate](#pipelinejobs_createorupdate)
- [Delete](#pipelinejobs_delete)
- [Get](#pipelinejobs_get)
- [List](#pipelinejobs_list)
- [Update](#pipelinejobs_update)

## PipelineTopologies

- [CreateOrUpdate](#pipelinetopologies_createorupdate)
- [Delete](#pipelinetopologies_delete)
- [Get](#pipelinetopologies_get)
- [List](#pipelinetopologies_list)
- [Update](#pipelinetopologies_update)

## PrivateEndpointConnections

- [CreateOrUpdate](#privateendpointconnections_createorupdate)
- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [List](#privateendpointconnections_list)

## PrivateLinkResources

- [Get](#privatelinkresources_get)
- [List](#privatelinkresources_list)

## VideoAnalyzerOperationResults

- [Get](#videoanalyzeroperationresults_get)

## VideoAnalyzerOperationStatuses

- [Get](#videoanalyzeroperationstatuses_get)

## VideoAnalyzers

- [CreateOrUpdate](#videoanalyzers_createorupdate)
- [Delete](#videoanalyzers_delete)
- [GetByResourceGroup](#videoanalyzers_getbyresourcegroup)
- [List](#videoanalyzers_list)
- [ListBySubscription](#videoanalyzers_listbysubscription)
- [Update](#videoanalyzers_update)

## Videos

- [CreateOrUpdate](#videos_createorupdate)
- [Delete](#videos_delete)
- [Get](#videos_get)
- [List](#videos_list)
- [ListContentToken](#videos_listcontenttoken)
- [Update](#videos_update)
### AccessPolicies_CreateOrUpdate

```java
import com.azure.resourcemanager.videoanalyzer.models.AccessPolicyEccAlgo;
import com.azure.resourcemanager.videoanalyzer.models.AccessPolicyRsaAlgo;
import com.azure.resourcemanager.videoanalyzer.models.EccTokenKey;
import com.azure.resourcemanager.videoanalyzer.models.JwtAuthentication;
import com.azure.resourcemanager.videoanalyzer.models.RsaTokenKey;
import com.azure.resourcemanager.videoanalyzer.models.TokenClaim;
import java.util.Arrays;

/** Samples for AccessPolicies CreateOrUpdate. */
public final class AccessPoliciesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/access-policy-create.json
     */
    /**
     * Sample code: Register access policy entity.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void registerAccessPolicyEntity(
        com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager
            .accessPolicies()
            .define("accessPolicyName1")
            .withExistingVideoAnalyzer("testrg", "testaccount2")
            .withAuthentication(
                new JwtAuthentication()
                    .withIssuers(Arrays.asList("issuer1", "issuer2"))
                    .withAudiences(Arrays.asList("audience1"))
                    .withClaims(
                        Arrays
                            .asList(
                                new TokenClaim().withName("claimname1").withValue("claimvalue1"),
                                new TokenClaim().withName("claimname2").withValue("claimvalue2")))
                    .withKeys(
                        Arrays
                            .asList(
                                new RsaTokenKey()
                                    .withKid("123")
                                    .withAlg(AccessPolicyRsaAlgo.RS256)
                                    .withN("YmFzZTY0IQ==")
                                    .withE("ZLFzZTY0IQ=="),
                                new EccTokenKey()
                                    .withKid("124")
                                    .withAlg(AccessPolicyEccAlgo.ES256)
                                    .withX("XX==")
                                    .withY("YY=="))))
            .create();
    }
}
```

### AccessPolicies_Delete

```java
import com.azure.core.util.Context;

/** Samples for AccessPolicies Delete. */
public final class AccessPoliciesDeleteSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/access-policy-delete.json
     */
    /**
     * Sample code: Deletes an access policy entity.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void deletesAnAccessPolicyEntity(
        com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager.accessPolicies().deleteWithResponse("testrg", "testaccount2", "accessPolicyName1", Context.NONE);
    }
}
```

### AccessPolicies_Get

```java
import com.azure.core.util.Context;

/** Samples for AccessPolicies Get. */
public final class AccessPoliciesGetSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/access-policy-get.json
     */
    /**
     * Sample code: Gets an access policy entity.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void getsAnAccessPolicyEntity(com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager.accessPolicies().getWithResponse("testrg", "testaccount2", "accessPolicyName1", Context.NONE);
    }
}
```

### AccessPolicies_List

```java
import com.azure.core.util.Context;

/** Samples for AccessPolicies List. */
public final class AccessPoliciesListSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/access-policy-list.json
     */
    /**
     * Sample code: Lists access policy entities.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void listsAccessPolicyEntities(com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager.accessPolicies().list("testrg", "testaccount2", 2, Context.NONE);
    }
}
```

### AccessPolicies_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.videoanalyzer.models.AccessPolicyEccAlgo;
import com.azure.resourcemanager.videoanalyzer.models.AccessPolicyEntity;
import com.azure.resourcemanager.videoanalyzer.models.AccessPolicyRsaAlgo;
import com.azure.resourcemanager.videoanalyzer.models.EccTokenKey;
import com.azure.resourcemanager.videoanalyzer.models.JwtAuthentication;
import com.azure.resourcemanager.videoanalyzer.models.RsaTokenKey;
import com.azure.resourcemanager.videoanalyzer.models.TokenClaim;
import java.util.Arrays;

/** Samples for AccessPolicies Update. */
public final class AccessPoliciesUpdateSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/access-policy-patch.json
     */
    /**
     * Sample code: Update access policy entity.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void updateAccessPolicyEntity(com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        AccessPolicyEntity resource =
            manager
                .accessPolicies()
                .getWithResponse("testrg", "testaccount2", "accessPolicyName1", Context.NONE)
                .getValue();
        resource
            .update()
            .withAuthentication(
                new JwtAuthentication()
                    .withIssuers(Arrays.asList("issuer1", "issuer2"))
                    .withAudiences(Arrays.asList("audience1"))
                    .withClaims(
                        Arrays
                            .asList(
                                new TokenClaim().withName("claimname1").withValue("claimvalue1"),
                                new TokenClaim().withName("claimname2").withValue("claimvalue2")))
                    .withKeys(
                        Arrays
                            .asList(
                                new RsaTokenKey()
                                    .withKid("123")
                                    .withAlg(AccessPolicyRsaAlgo.RS256)
                                    .withN("YmFzZTY0IQ==")
                                    .withE("ZLFzZTY0IQ=="),
                                new EccTokenKey()
                                    .withKid("124")
                                    .withAlg(AccessPolicyEccAlgo.ES256)
                                    .withX("XX==")
                                    .withY("YY=="))))
            .apply();
    }
}
```

### EdgeModules_CreateOrUpdate

```java
/** Samples for EdgeModules CreateOrUpdate. */
public final class EdgeModulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/edge-modules-create.json
     */
    /**
     * Sample code: Registers an edge module.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void registersAnEdgeModule(com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager.edgeModules().define("edgeModule1").withExistingVideoAnalyzer("testrg", "testaccount2").create();
    }
}
```

### EdgeModules_Delete

```java
import com.azure.core.util.Context;

/** Samples for EdgeModules Delete. */
public final class EdgeModulesDeleteSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/edge-modules-delete.json
     */
    /**
     * Sample code: Deletes an edge module registration.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void deletesAnEdgeModuleRegistration(
        com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager.edgeModules().deleteWithResponse("testrg", "testaccount2", "edgeModule1", Context.NONE);
    }
}
```

### EdgeModules_Get

```java
import com.azure.core.util.Context;

/** Samples for EdgeModules Get. */
public final class EdgeModulesGetSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/edge-modules-get.json
     */
    /**
     * Sample code: Gets edge module registration.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void getsEdgeModuleRegistration(
        com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager.edgeModules().getWithResponse("testrg", "testaccount2", "edgeModule1", Context.NONE);
    }
}
```

### EdgeModules_List

```java
import com.azure.core.util.Context;

/** Samples for EdgeModules List. */
public final class EdgeModulesListSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/edge-modules-list.json
     */
    /**
     * Sample code: Lists the registered edge modules.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void listsTheRegisteredEdgeModules(
        com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager.edgeModules().list("testrg", "testaccount2", null, Context.NONE);
    }
}
```

### EdgeModules_ListProvisioningToken

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.videoanalyzer.models.ListProvisioningTokenInput;
import java.time.OffsetDateTime;

/** Samples for EdgeModules ListProvisioningToken. */
public final class EdgeModulesListProvisioningTokenSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/edge-modules-listProvisioningToken.json
     */
    /**
     * Sample code: Generate the Provisioning token for an edge module registration.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void generateTheProvisioningTokenForAnEdgeModuleRegistration(
        com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager
            .edgeModules()
            .listProvisioningTokenWithResponse(
                "testrg",
                "testaccount2",
                "edgeModule1",
                new ListProvisioningTokenInput()
                    .withExpirationDate(OffsetDateTime.parse("2023-01-23T11:04:49.0526841-08:00")),
                Context.NONE);
    }
}
```

### LivePipelineOperationStatuses_Get

```java
import com.azure.core.util.Context;

/** Samples for LivePipelineOperationStatuses Get. */
public final class LivePipelineOperationStatusesGetSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/live-pipeline-operation-status-get.json
     */
    /**
     * Sample code: Get the live pipeline operation status.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void getTheLivePipelineOperationStatus(
        com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager
            .livePipelineOperationStatuses()
            .getWithResponse(
                "testrg", "testaccount2", "livePipeline1", "00000000-0000-0000-0000-000000000001", Context.NONE);
    }
}
```

### LivePipelines_Activate

```java
import com.azure.core.util.Context;

/** Samples for LivePipelines Activate. */
public final class LivePipelinesActivateSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/live-pipeline-activate.json
     */
    /**
     * Sample code: Activate live pipeline.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void activateLivePipeline(com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager.livePipelines().activate("testrg", "testaccount2", "livePipeline1", Context.NONE);
    }
}
```

### LivePipelines_CreateOrUpdate

```java
import com.azure.resourcemanager.videoanalyzer.models.ParameterDefinition;
import java.util.Arrays;

/** Samples for LivePipelines CreateOrUpdate. */
public final class LivePipelinesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/live-pipeline-create.json
     */
    /**
     * Sample code: Create or update a live pipeline.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void createOrUpdateALivePipeline(
        com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager
            .livePipelines()
            .define("livePipeline1")
            .withExistingVideoAnalyzer("testrg", "testaccount2")
            .withTopologyName("pipelinetopology1")
            .withDescription("Live Pipeline 1 Description")
            .withBitrateKbps(500)
            .withParameters(
                Arrays
                    .asList(
                        new ParameterDefinition().withName("rtspUrlParameter").withValue("rtsp://contoso.com/stream")))
            .create();
    }
}
```

### LivePipelines_Deactivate

```java
import com.azure.core.util.Context;

/** Samples for LivePipelines Deactivate. */
public final class LivePipelinesDeactivateSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/live-pipeline-deactivate.json
     */
    /**
     * Sample code: Deactivate Live pipeline.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void deactivateLivePipeline(com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager.livePipelines().deactivate("testrg", "testaccount2", "livePipeline1", Context.NONE);
    }
}
```

### LivePipelines_Delete

```java
import com.azure.core.util.Context;

/** Samples for LivePipelines Delete. */
public final class LivePipelinesDeleteSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/live-pipeline-delete.json
     */
    /**
     * Sample code: Delete a live pipeline.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void deleteALivePipeline(com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager.livePipelines().deleteWithResponse("testrg", "testaccount2", "livePipeline1", Context.NONE);
    }
}
```

### LivePipelines_Get

```java
import com.azure.core.util.Context;

/** Samples for LivePipelines Get. */
public final class LivePipelinesGetSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/live-pipeline-get-by-name.json
     */
    /**
     * Sample code: Retrieves a specific live pipeline by name.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void retrievesASpecificLivePipelineByName(
        com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager.livePipelines().getWithResponse("testrg", "testaccount2", "livePipeline1", Context.NONE);
    }
}
```

### LivePipelines_List

```java
import com.azure.core.util.Context;

/** Samples for LivePipelines List. */
public final class LivePipelinesListSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/live-pipeline-list.json
     */
    /**
     * Sample code: List live pipelines.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void listLivePipelines(com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager.livePipelines().list("testrg", "testaccount2", null, 2, Context.NONE);
    }
}
```

### LivePipelines_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.videoanalyzer.models.LivePipeline;

/** Samples for LivePipelines Update. */
public final class LivePipelinesUpdateSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/live-pipeline-patch.json
     */
    /**
     * Sample code: Updates a live pipeline.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void updatesALivePipeline(com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        LivePipeline resource =
            manager.livePipelines().getWithResponse("testrg", "testaccount2", "livePipeline1", Context.NONE).getValue();
        resource.update().withDescription("Live Pipeline 1 Description").apply();
    }
}
```

### Locations_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.videoanalyzer.models.CheckNameAvailabilityRequest;

/** Samples for Locations CheckNameAvailability. */
public final class LocationsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/accounts-check-name-availability.json
     */
    /**
     * Sample code: Check Name Availability.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void checkNameAvailability(com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager
            .locations()
            .checkNameAvailabilityWithResponse(
                "japanwest",
                new CheckNameAvailabilityRequest().withName("contosotv").withType("videoAnalyzers"),
                Context.NONE);
    }
}
```

### OperationResults_Get

```java
import com.azure.core.util.Context;

/** Samples for OperationResults Get. */
public final class OperationResultsGetSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/video-analyzer-private-endpoint-connection-operation-result-by-id.json
     */
    /**
     * Sample code: Get status of private endpoint connection asynchronous operation.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void getStatusOfPrivateEndpointConnectionAsynchronousOperation(
        com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager
            .operationResults()
            .getWithResponse(
                "contoso",
                "contososports",
                "6FBA62C4-99B5-4FF8-9826-FC4744A8864F",
                "10000000-0000-0000-0000-000000000000",
                Context.NONE);
    }
}
```

### OperationStatuses_Get

```java
import com.azure.core.util.Context;

/** Samples for OperationStatuses Get. */
public final class OperationStatusesGetSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/video-analyzer-private-endpoint-connection-operation-status-by-id-terminal-state.json
     */
    /**
     * Sample code: Get status of private endpoint asynchronous operation when it is completed.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void getStatusOfPrivateEndpointAsynchronousOperationWhenItIsCompleted(
        com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager
            .operationStatuses()
            .getWithResponse(
                "contoso",
                "contososports",
                "D612C429-2526-49D5-961B-885AE11406FD",
                "CDE44A33-DD32-4FFA-A1BC-601DC4D52B03",
                Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/operations-list-all.json
     */
    /**
     * Sample code: List Operations.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void listOperations(com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager.operations().listWithResponse(Context.NONE);
    }
}
```

### PipelineJobOperationStatuses_Get

```java
import com.azure.core.util.Context;

/** Samples for PipelineJobOperationStatuses Get. */
public final class PipelineJobOperationStatusesGetSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/pipeline-job-operation-status-get.json
     */
    /**
     * Sample code: Get the pipeline job operation status.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void getThePipelineJobOperationStatus(
        com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager
            .pipelineJobOperationStatuses()
            .getWithResponse(
                "testrg", "testaccount2", "pipelineJob1", "00000000-0000-0000-0000-000000000001", Context.NONE);
    }
}
```

### PipelineJobs_Cancel

```java
import com.azure.core.util.Context;

/** Samples for PipelineJobs Cancel. */
public final class PipelineJobsCancelSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/pipeline-job-cancel.json
     */
    /**
     * Sample code: Cancels a pipeline job.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void cancelsAPipelineJob(com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager.pipelineJobs().cancel("testrg", "testaccount2", "pipelineJob1", Context.NONE);
    }
}
```

### PipelineJobs_CreateOrUpdate

```java
import com.azure.resourcemanager.videoanalyzer.models.ParameterDefinition;
import java.util.Arrays;

/** Samples for PipelineJobs CreateOrUpdate. */
public final class PipelineJobsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/pipeline-job-create.json
     */
    /**
     * Sample code: Create or update a pipeline job.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void createOrUpdateAPipelineJob(
        com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager
            .pipelineJobs()
            .define("pipelineJob1")
            .withExistingVideoAnalyzer("testrg", "testaccount2")
            .withTopologyName("pipelinetopology1")
            .withDescription("Pipeline Job 1 Dsecription")
            .withParameters(
                Arrays
                    .asList(
                        new ParameterDefinition()
                            .withName("timesequences")
                            .withValue("[[\"2020-10-05T03:30:00Z\", \"2020-10-05T04:30:00Z\"]]"),
                        new ParameterDefinition().withName("videoSourceName").withValue("camera001")))
            .create();
    }
}
```

### PipelineJobs_Delete

```java
import com.azure.core.util.Context;

/** Samples for PipelineJobs Delete. */
public final class PipelineJobsDeleteSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/pipeline-job-delete.json
     */
    /**
     * Sample code: Deletes a pipeline job.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void deletesAPipelineJob(com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager.pipelineJobs().deleteWithResponse("testrg", "testaccount2", "pipelineJob1", Context.NONE);
    }
}
```

### PipelineJobs_Get

```java
import com.azure.core.util.Context;

/** Samples for PipelineJobs Get. */
public final class PipelineJobsGetSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/pipeline-job-get-by-name.json
     */
    /**
     * Sample code: Get a pipeline job by name.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void getAPipelineJobByName(com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager.pipelineJobs().getWithResponse("testrg", "testaccount2", "pipelineJob1", Context.NONE);
    }
}
```

### PipelineJobs_List

```java
import com.azure.core.util.Context;

/** Samples for PipelineJobs List. */
public final class PipelineJobsListSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/pipeline-job-list.json
     */
    /**
     * Sample code: List all pipeline jobs.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void listAllPipelineJobs(com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager.pipelineJobs().list("testrg", "testaccount2", null, 2, Context.NONE);
    }
}
```

### PipelineJobs_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.videoanalyzer.models.PipelineJob;

/** Samples for PipelineJobs Update. */
public final class PipelineJobsUpdateSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/pipeline-job-patch.json
     */
    /**
     * Sample code: Updates a pipeline job.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void updatesAPipelineJob(com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        PipelineJob resource =
            manager.pipelineJobs().getWithResponse("testrg", "testaccount2", "pipelineJob1", Context.NONE).getValue();
        resource.update().withDescription("Pipeline Job 1 description").apply();
    }
}
```

### PipelineTopologies_CreateOrUpdate

```java
import com.azure.resourcemanager.videoanalyzer.models.Kind;
import com.azure.resourcemanager.videoanalyzer.models.NodeInput;
import com.azure.resourcemanager.videoanalyzer.models.ParameterDeclaration;
import com.azure.resourcemanager.videoanalyzer.models.ParameterType;
import com.azure.resourcemanager.videoanalyzer.models.RtspSource;
import com.azure.resourcemanager.videoanalyzer.models.RtspTransport;
import com.azure.resourcemanager.videoanalyzer.models.Sku;
import com.azure.resourcemanager.videoanalyzer.models.SkuName;
import com.azure.resourcemanager.videoanalyzer.models.UnsecuredEndpoint;
import com.azure.resourcemanager.videoanalyzer.models.UsernamePasswordCredentials;
import com.azure.resourcemanager.videoanalyzer.models.VideoCreationProperties;
import com.azure.resourcemanager.videoanalyzer.models.VideoPublishingOptions;
import com.azure.resourcemanager.videoanalyzer.models.VideoSink;
import java.util.Arrays;

/** Samples for PipelineTopologies CreateOrUpdate. */
public final class PipelineTopologiesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/pipeline-topology-create.json
     */
    /**
     * Sample code: Create or update a pipeline topology with an Rtsp source and video sink.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void createOrUpdateAPipelineTopologyWithAnRtspSourceAndVideoSink(
        com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager
            .pipelineTopologies()
            .define("pipelineTopology1")
            .withExistingVideoAnalyzer("testrg", "testaccount2")
            .withKind(Kind.LIVE)
            .withSku(new Sku().withName(SkuName.LIVE_S1))
            .withDescription("Pipeline Topology 1 Description")
            .withParameters(
                Arrays
                    .asList(
                        new ParameterDeclaration()
                            .withName("rtspUrlParameter")
                            .withType(ParameterType.STRING)
                            .withDescription("rtsp source url parameter")
                            .withDefaultProperty("rtsp://microsoft.com/video.mp4"),
                        new ParameterDeclaration()
                            .withName("rtspPasswordParameter")
                            .withType(ParameterType.SECRET_STRING)
                            .withDescription("rtsp source password parameter")
                            .withDefaultProperty("password")))
            .withSources(
                Arrays
                    .asList(
                        new RtspSource()
                            .withName("rtspSource")
                            .withTransport(RtspTransport.HTTP)
                            .withEndpoint(
                                new UnsecuredEndpoint()
                                    .withCredentials(
                                        new UsernamePasswordCredentials()
                                            .withUsername("username")
                                            .withPassword("${rtspPasswordParameter}"))
                                    .withUrl("${rtspUrlParameter}"))))
            .withSinks(
                Arrays
                    .asList(
                        new VideoSink()
                            .withName("videoSink")
                            .withInputs(Arrays.asList(new NodeInput().withNodeName("rtspSource")))
                            .withVideoName("camera001")
                            .withVideoCreationProperties(
                                new VideoCreationProperties()
                                    .withTitle("Parking Lot (Camera 1)")
                                    .withDescription("Parking lot south entrance")
                                    .withSegmentLength("PT30S"))
                            .withVideoPublishingOptions(
                                new VideoPublishingOptions()
                                    .withDisableArchive("false")
                                    .withDisableRtspPublishing("true"))))
            .create();
    }
}
```

### PipelineTopologies_Delete

```java
import com.azure.core.util.Context;

/** Samples for PipelineTopologies Delete. */
public final class PipelineTopologiesDeleteSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/pipeline-topology-delete.json
     */
    /**
     * Sample code: Delete a pipeline topology.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void deleteAPipelineTopology(com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager.pipelineTopologies().deleteWithResponse("testrg", "testaccount2", "pipelineTopology1", Context.NONE);
    }
}
```

### PipelineTopologies_Get

```java
import com.azure.core.util.Context;

/** Samples for PipelineTopologies Get. */
public final class PipelineTopologiesGetSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/pipeline-topology-get-by-name.json
     */
    /**
     * Sample code: Get a pipeline topology by name.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void getAPipelineTopologyByName(
        com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager.pipelineTopologies().getWithResponse("testrg", "testaccount2", "pipelineTopology1", Context.NONE);
    }
}
```

### PipelineTopologies_List

```java
import com.azure.core.util.Context;

/** Samples for PipelineTopologies List. */
public final class PipelineTopologiesListSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/pipeline-topology-list.json
     */
    /**
     * Sample code: List all pipeline topologies.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void listAllPipelineTopologies(com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager.pipelineTopologies().list("testrg", "testaccount2", null, 2, Context.NONE);
    }
}
```

### PipelineTopologies_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.videoanalyzer.models.PipelineTopology;

/** Samples for PipelineTopologies Update. */
public final class PipelineTopologiesUpdateSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/pipeline-topology-patch.json
     */
    /**
     * Sample code: Update pipeline topology.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void updatePipelineTopology(com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        PipelineTopology resource =
            manager
                .pipelineTopologies()
                .getWithResponse("testrg", "testaccount2", "pipelineTopology1", Context.NONE)
                .getValue();
        resource.update().withDescription("Pipeline Topology 1 Description").apply();
    }
}
```

### PrivateEndpointConnections_CreateOrUpdate

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.videoanalyzer.models.PrivateEndpointConnection;
import com.azure.resourcemanager.videoanalyzer.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.videoanalyzer.models.PrivateLinkServiceConnectionState;

/** Samples for PrivateEndpointConnections CreateOrUpdate. */
public final class PrivateEndpointConnectionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/video-analyzer-private-endpoint-connection-put.json
     */
    /**
     * Sample code: Update private endpoint connection.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void updatePrivateEndpointConnection(
        com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        PrivateEndpointConnection resource =
            manager
                .privateEndpointConnections()
                .getWithResponse("contoso", "contososports", "10000000-0000-0000-0000-000000000000", Context.NONE)
                .getValue();
        resource
            .update()
            .withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionState()
                    .withStatus(PrivateEndpointServiceConnectionStatus.APPROVED)
                    .withDescription("Test description."))
            .apply();
    }
}
```

### PrivateEndpointConnections_Delete

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Delete. */
public final class PrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/video-analyzer-private-endpoint-connection-delete.json
     */
    /**
     * Sample code: Delete private endpoint connection.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void deletePrivateEndpointConnection(
        com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager
            .privateEndpointConnections()
            .deleteWithResponse("contoso", "contososports", "connectionName1", Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Get. */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/video-analyzer-private-endpoint-connection-get-by-name.json
     */
    /**
     * Sample code: Get private endpoint connection.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void getPrivateEndpointConnection(
        com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager
            .privateEndpointConnections()
            .getWithResponse("contoso", "contososports", "10000000-0000-0000-0000-000000000000", Context.NONE);
    }
}
```

### PrivateEndpointConnections_List

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections List. */
public final class PrivateEndpointConnectionsListSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/video-analyzer-private-endpoint-connection-list.json
     */
    /**
     * Sample code: Get all private endpoint connections.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void getAllPrivateEndpointConnections(
        com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager.privateEndpointConnections().listWithResponse("contoso", "contososports", Context.NONE);
    }
}
```

### PrivateLinkResources_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResources Get. */
public final class PrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/video-analyzer-private-link-resources-get-by-name.json
     */
    /**
     * Sample code: Get details of a group ID.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void getDetailsOfAGroupID(com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager.privateLinkResources().getWithResponse("contoso", "contososports", "integration", Context.NONE);
    }
}
```

### PrivateLinkResources_List

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResources List. */
public final class PrivateLinkResourcesListSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/video-analyzer-private-link-resources-list.json
     */
    /**
     * Sample code: Get list of all group IDs.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void getListOfAllGroupIDs(com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager.privateLinkResources().listWithResponse("contoso", "contososports", Context.NONE);
    }
}
```

### VideoAnalyzerOperationResults_Get

```java
import com.azure.core.util.Context;

/** Samples for VideoAnalyzerOperationResults Get. */
public final class VideoAnalyzerOperationResultsGetSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/video-analyzer-operation-result-by-id.json
     */
    /**
     * Sample code: Get status of asynchronous operation.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void getStatusOfAsynchronousOperation(
        com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager
            .videoAnalyzerOperationResults()
            .getWithResponse("westus", "6FBA62C4-99B5-4FF8-9826-FC4744A8864F", Context.NONE);
    }
}
```

### VideoAnalyzerOperationStatuses_Get

```java
import com.azure.core.util.Context;

/** Samples for VideoAnalyzerOperationStatuses Get. */
public final class VideoAnalyzerOperationStatusesGetSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/video-analyzer-operation-status-by-id-non-terminal-state-failed.json
     */
    /**
     * Sample code: Get status of asynchronous operation when it is completed with error.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void getStatusOfAsynchronousOperationWhenItIsCompletedWithError(
        com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager
            .videoAnalyzerOperationStatuses()
            .getWithResponse("westus", "D612C429-2526-49D5-961B-885AE11406FD", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/video-analyzer-operation-status-by-id-terminal-state.json
     */
    /**
     * Sample code: Get status of asynchronous operation when it is completed.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void getStatusOfAsynchronousOperationWhenItIsCompleted(
        com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager
            .videoAnalyzerOperationStatuses()
            .getWithResponse("westus", "D612C429-2526-49D5-961B-885AE11406FD", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/video-analyzer-operation-status-by-id-non-terminal-state.json
     */
    /**
     * Sample code: Get status of asynchronous operation when it is ongoing.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void getStatusOfAsynchronousOperationWhenItIsOngoing(
        com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager
            .videoAnalyzerOperationStatuses()
            .getWithResponse("westus", "D612C429-2526-49D5-961B-885AE11406FD", Context.NONE);
    }
}
```

### VideoAnalyzers_CreateOrUpdate

```java
import com.azure.resourcemanager.videoanalyzer.models.AccountEncryption;
import com.azure.resourcemanager.videoanalyzer.models.AccountEncryptionKeyType;
import com.azure.resourcemanager.videoanalyzer.models.IotHub;
import com.azure.resourcemanager.videoanalyzer.models.ResourceIdentity;
import com.azure.resourcemanager.videoanalyzer.models.StorageAccount;
import com.azure.resourcemanager.videoanalyzer.models.UserAssignedManagedIdentity;
import com.azure.resourcemanager.videoanalyzer.models.VideoAnalyzerIdentity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for VideoAnalyzers CreateOrUpdate. */
public final class VideoAnalyzersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/video-analyzer-accounts-create-or-update.json
     */
    /**
     * Sample code: Create a Video Analyzer account.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void createAVideoAnalyzerAccount(
        com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager
            .videoAnalyzers()
            .define("contosotv")
            .withRegion("South Central US")
            .withExistingResourceGroup("contoso")
            .withTags(mapOf("tag1", "value1", "tag2", "value2"))
            .withIdentity(
                new VideoAnalyzerIdentity()
                    .withType("UserAssigned")
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id1",
                            new UserAssignedManagedIdentity(),
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id2",
                            new UserAssignedManagedIdentity(),
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id3",
                            new UserAssignedManagedIdentity())))
            .withStorageAccounts(
                Arrays
                    .asList(
                        new StorageAccount()
                            .withId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg/providers/Microsoft.Storage/storageAccounts/storage1")
                            .withIdentity(
                                new ResourceIdentity()
                                    .withUserAssignedIdentity(
                                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id2"))))
            .withEncryption(new AccountEncryption().withType(AccountEncryptionKeyType.SYSTEM_KEY))
            .withIotHubs(
                Arrays
                    .asList(
                        new IotHub()
                            .withId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg/providers/Microsoft.Devices/IotHubs/hub1")
                            .withIdentity(
                                new ResourceIdentity()
                                    .withUserAssignedIdentity(
                                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id3")),
                        new IotHub()
                            .withId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg/providers/Microsoft.Devices/IotHubs/hub2")
                            .withIdentity(
                                new ResourceIdentity()
                                    .withUserAssignedIdentity(
                                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id3"))))
            .create();
    }

    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }
}
```

### VideoAnalyzers_Delete

```java
import com.azure.core.util.Context;

/** Samples for VideoAnalyzers Delete. */
public final class VideoAnalyzersDeleteSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/video-analyzer-accounts-delete.json
     */
    /**
     * Sample code: Delete a Video Analyzer account.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void deleteAVideoAnalyzerAccount(
        com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager.videoAnalyzers().deleteWithResponse("contoso", "contosotv", Context.NONE);
    }
}
```

### VideoAnalyzers_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for VideoAnalyzers GetByResourceGroup. */
public final class VideoAnalyzersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/video-analyzer-accounts-get-by-name.json
     */
    /**
     * Sample code: Get a Video Analyzer account by name.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void getAVideoAnalyzerAccountByName(
        com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager.videoAnalyzers().getByResourceGroupWithResponse("contoso", "contosotv", Context.NONE);
    }
}
```

### VideoAnalyzers_List

```java
import com.azure.core.util.Context;

/** Samples for VideoAnalyzers List. */
public final class VideoAnalyzersListSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/video-analyzer-accounts-list-all-accounts.json
     */
    /**
     * Sample code: List all Video Analyzer accounts.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void listAllVideoAnalyzerAccounts(
        com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager.videoAnalyzers().listWithResponse("contoso", Context.NONE);
    }
}
```

### VideoAnalyzers_ListBySubscription

```java
import com.azure.core.util.Context;

/** Samples for VideoAnalyzers ListBySubscription. */
public final class VideoAnalyzersListBySubscriptionSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/video-analyzer-accounts-subscription-list-all-accounts.json
     */
    /**
     * Sample code: List all Video Analyzer accounts in the specified subscription.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void listAllVideoAnalyzerAccountsInTheSpecifiedSubscription(
        com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager.videoAnalyzers().listBySubscriptionWithResponse(Context.NONE);
    }
}
```

### VideoAnalyzers_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.videoanalyzer.models.VideoAnalyzer;
import java.util.HashMap;
import java.util.Map;

/** Samples for VideoAnalyzers Update. */
public final class VideoAnalyzersUpdateSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/video-analyzer-accounts-update.json
     */
    /**
     * Sample code: Update a Video Analyzer accounts.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void updateAVideoAnalyzerAccounts(
        com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        VideoAnalyzer resource =
            manager.videoAnalyzers().getByResourceGroupWithResponse("contoso", "contosotv", Context.NONE).getValue();
        resource.update().withTags(mapOf("key1", "value3")).apply();
    }

    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }
}
```

### Videos_CreateOrUpdate

```java
/** Samples for Videos CreateOrUpdate. */
public final class VideosCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/video-create.json
     */
    /**
     * Sample code: Register video entity.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void registerVideoEntity(com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager
            .videos()
            .define("video1")
            .withExistingVideoAnalyzer("testrg", "testaccount2")
            .withTitle("Sample Title 1")
            .withDescription("Sample Description 1")
            .create();
    }
}
```

### Videos_Delete

```java
import com.azure.core.util.Context;

/** Samples for Videos Delete. */
public final class VideosDeleteSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/video-delete.json
     */
    /**
     * Sample code: Deletes a video entity.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void deletesAVideoEntity(com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager.videos().deleteWithResponse("testrg", "testaccount2", "video1", Context.NONE);
    }
}
```

### Videos_Get

```java
import com.azure.core.util.Context;

/** Samples for Videos Get. */
public final class VideosGetSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/video-get.json
     */
    /**
     * Sample code: Gets a video entity.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void getsAVideoEntity(com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager.videos().getWithResponse("testrg", "testaccount2", "video1", Context.NONE);
    }
}
```

### Videos_List

```java
import com.azure.core.util.Context;

/** Samples for Videos List. */
public final class VideosListSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/video-list.json
     */
    /**
     * Sample code: Lists video entities.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void listsVideoEntities(com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager.videos().list("testrg", "testaccount2", 2, Context.NONE);
    }
}
```

### Videos_ListContentToken

```java
import com.azure.core.util.Context;

/** Samples for Videos ListContentToken. */
public final class VideosListContentTokenSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/video-listContentToken.json
     */
    /**
     * Sample code: Generate a content token for media endpoint authorization.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void generateAContentTokenForMediaEndpointAuthorization(
        com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        manager.videos().listContentTokenWithResponse("testrg", "testaccount2", "video3", Context.NONE);
    }
}
```

### Videos_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.videoanalyzer.models.VideoEntity;

/** Samples for Videos Update. */
public final class VideosUpdateSamples {
    /*
     * x-ms-original-file: specification/videoanalyzer/resource-manager/Microsoft.Media/preview/2021-11-01-preview/examples/video-patch.json
     */
    /**
     * Sample code: Update video entity.
     *
     * @param manager Entry point to VideoAnalyzerManager.
     */
    public static void updateVideoEntity(com.azure.resourcemanager.videoanalyzer.VideoAnalyzerManager manager) {
        VideoEntity resource =
            manager.videos().getWithResponse("testrg", "testaccount2", "video1", Context.NONE).getValue();
        resource.update().withDescription("Parking Lot East Entrance").apply();
    }
}
```

