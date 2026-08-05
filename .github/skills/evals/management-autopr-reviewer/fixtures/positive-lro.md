# Pull request snapshot

Title: `[AutoPR azure-resourcemanager-contosowidgets]-generated-from-SDK Generation`
Author: `app/azure-sdk-automation`
Base: `main`
Draft: `false`
Release plan: https://example.invalid/releaseplan/16

Changed method:

```java
public Response<WidgetCreateResponse> createWithResponse(...)
```

New generated models:

```java
public final class WidgetCreateResponse { ... }

public final class WidgetCreateHeaders {
    public String getLocation();
    public Integer getRetryAfter();
}
```

The headers map `Location` and `Retry-After`. These response and headers models
did not exist on the base branch.
