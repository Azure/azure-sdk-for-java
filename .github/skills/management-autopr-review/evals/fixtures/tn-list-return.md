# Pull request snapshot

Title: `[AutoPR azure-resourcemanager-contosowidgets]-generated-from-SDK Generation`
Author: `app/azure-sdk-automation`
Base: `main`
Draft: `false`
Release Plan link: https://example.com/release-plan

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

Changed implementation details:

```java
public Response<WidgetListResult> listWithResponse(Context context) { ... }

Mono<Response<WidgetListResultInner>> list(
    String subscriptionId, String apiVersion, Context context);

private Mono<PagedResponse<WidgetInner>> listSinglePageAsync(Context context) { ... }
```
