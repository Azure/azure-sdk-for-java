# Pull request snapshot

Title: `[AutoPR azure-resourcemanager-contosowidgets]-generated-from-SDK Generation`
Author: `app/azure-sdk-automation`
Base: `main`
Draft: `false`
Release Plan link: https://azsdk-releaseplan-dashboard-hveph5aqhhcfhtgu.westus-01.azurewebsites.net/?releaseplan=35926

PR: `Azure/azure-sdk-for-java#12345`
Head SHA: `2222222222222222222222222222222222222222`
Package: `azure-resourcemanager-contosowidgets`
Package version: `2.0.0-beta.1`

Changed method in
`sdk/contosowidgets/azure-resourcemanager-contosowidgets/src/main/java/com/azure/resourcemanager/contosowidgets/WidgetManager.java`:

```java
public Response<WidgetCreateResponse> createWithResponse(...)
```

New response models in
`sdk/contosowidgets/azure-resourcemanager-contosowidgets/src/main/java/com/azure/resourcemanager/contosowidgets/models/WidgetCreateResponse.java`
and
`sdk/contosowidgets/azure-resourcemanager-contosowidgets/src/main/java/com/azure/resourcemanager/contosowidgets/models/WidgetCreateHeaders.java`:

```java
public final class WidgetCreateResponse { ... }

public final class WidgetCreateHeaders {
    public String getLocation();
    public Integer getRetryAfter();
}
```

The headers map `Location` and `Retry-After`. These response and headers models
did not exist on the base branch.
