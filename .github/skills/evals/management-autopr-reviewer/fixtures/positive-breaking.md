# Pull request snapshot

Title: `[AutoPR azure-resourcemanager-contosowidgets]-generated-from-SDK Generation`
Author: `app/azure-sdk-automation`
Base: `main`
Draft: `false`
Release plan: https://example.invalid/releaseplan/17

Base:

```java
public interface Widgets {
    Widget getByResourceGroup(String resourceGroupName, String widgetName);
}
```

Head:

```java
public interface Widgets {
    Response<Widget> getByResourceGroup(String resourceGroupName, String widgetName);
}
```

The existing public method's return type changed. No compatibility overload was
added.
