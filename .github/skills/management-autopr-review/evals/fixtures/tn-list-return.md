# Pull request snapshot

Title: `[AutoPR azure-resourcemanager-contosowidgets]-generated-from-SDK Generation`
Author: `app/azure-sdk-automation`
Base: `main`
Draft: `false`
Release Plan link: https://azsdk-releaseplan-dashboard-hveph5aqhhcfhtgu.westus-01.azurewebsites.net/?releaseplan=35926

PR: `Azure/azure-sdk-for-java#12345`
Head SHA: `2222222222222222222222222222222222222222`
Package: `azure-resourcemanager-contosowidgets`
Package version: `1.2.0-beta.1`

Changed user-facing interface
`sdk/contosowidgets/azure-resourcemanager-contosowidgets/src/main/java/com/azure/resourcemanager/contosowidgets/models/Widgets.java`:

```java
PagedIterable<Widget> list();

PagedIterable<Widget> listByResourceGroup(String resourceGroupName);
```

Changed user-facing async client:

```java
PagedFlux<Widget> list();
```

New genuine non-pageable action operations:

```java
CommunicationServiceKeys listKeys();

FleetCredentialResults listCredentials();
```

Their response models do not expose `value`:

```java
interface CommunicationServiceKeys {
    String primaryKey();
    String secondaryKey();
}

interface FleetCredentialResults {
    List<FleetCredentialResult> kubeconfigs();
}
```

Modified pre-existing beta operation:

```diff
- WidgetListResult listBySubscription();
+ UpdatedWidgetListResult listBySubscription();
```

`listBySubscription` existed on the base branch, so this is not a newly added
operation. Both return models expose a collection-valued `value`.

Changed implementation details:

```java
public Response<WidgetListResult> listWithResponse(Context context) { ... }

Mono<Response<WidgetListResultInner>> list(
    String subscriptionId, String apiVersion, Context context);

private Mono<PagedResponse<WidgetInner>> listSinglePageAsync(Context context) { ... }
```
