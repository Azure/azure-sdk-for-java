# Spring Cloud for Azure feature management client library for Java

## Key concepts

### Feature Management

Feature flags provide a way for Spring Boot applications to turn features on or off dynamically. Developers can use feature flags in simple use cases like conditional statement to more advanced scenarios like conditionally adding routes. Feature Flags are not dependent of any spring-cloud-azure dependencies, but may be used in conjunction with spring-cloud-azure-appconfiguration-config.

Here are some of the benefits of using this library:

* A common convention for feature management
* Low barrier-to-entry
  * Supports application.yml file feature flag setup
* Feature Flag lifetime management
  * Configuration values can change in real-time, feature flags can be consistent across the entire request

### Feature Flags

Feature flags are composed of two parts, a name and a list of feature-filters that are used to turn the feature on.

### Feature Filters

Feature filters define a scenario for when a feature should be enabled. When a feature is evaluated for whether it is on or off, its list of feature-filters are traversed until one of the filters decides the feature should be enabled. At this point the feature is considered enabled and traversal through the feature filters stops. If no feature filter indicates that the feature should be enabled, then it will be considered disabled.

As an example, a Microsoft Edge browser feature filter could be designed. This feature filter would activate any features it is attached to as long as an HTTP request is coming from Microsoft Edge.

### Registration

The Spring Configuration system is used to determine the state of feature flags. Any system can be used to have them read in, such as application.yml, spring-cloud-azure-appconfiguration-config and more.

### Feature Flag Declaration

The feature management library supports application.yml or bootstrap.yml as a feature flag source. Below we have an example of the format used to set up feature flags in a application.yml file.

```yaml
feature-management:
  feature-t: false
  feature-u:
    enabled-for:
      -
        name: Random
  feature-v:
    enabled-for:
      -
        name: TimeWindow
        parameters:
          start: "Wed, 01 May 2019 13:59:59 GMT",
          end: "Mon, 01 July 2019 00:00:00 GMT"
```

The `feature-management` section of the YAML document is used by convention to load feature flags. In the section above, we see that we have provided three different features. Features define their filters using the `enabled-for`  property. We can see that feature `feature-t` is set to false with no filters set. `feature-t` will always return false, this can also be done for true. `feature-u` which has only one feature filter `Random` which does not require any configuration so it only has the name property. `feature-v` it specifies a feature filter named `TimeWindow`. This is an example of a configurable feature filter. We can see in the example that the filter has a parameter's property. This is used to configure the filter. In this case, the start and end times for the feature to be active are configured.

#### Supported properties

Name | Description | Required | Default
---|---|---|---
spring.cloud.azure.feature.management.fail-fast | Whether throw RuntimeException or not when exception occurs | No |  true

### Consumption

The simplest use case for feature flags is to do a conditional check for whether a feature is enabled to take different paths in code. The use cases grow when additional using spring-cloud-azure-feature-flag-web to manage web based features.

#### Feature Check

The basic form of feature management is checking if a feature is enabled and then performing actions based on the result. This is done through the autowiring `FeatureManager` and calling it's `isEnabledAsync` method.

```java
@Autowired
FeatureManager featureManager;

if(featureManager.isEnabledAsync("feature-t").block()) {
    // Do Something
}
```

`FeatureManager` can also be accessed by `@Component` classes.

#### Controllers

When using the Feature Management Web library you can require that a given feature is enabled in order to execute. This can be done by using the `@FeatureOn` annotation.

```java
@GetMapping("/featureT")
@FeatureGate(feature = "feature-t")
@ResponseBody
public String featureT() {
    ...
}
```

The `featureT` endpoint can only be accessed if "feature-t" is enabled.

#### Disabled Action Handling

When a controller is blocked because the feature it specifies is disabled, `IDisabledFeaturesHandler` will be invoked. By default, a HTTP 404 is returned. This can be overridden using implementing `IDisabledFeaturesHandler`.

```java
@Component
public class DisabledFeaturesHandler implements IDisabledFeaturesHandler{

    @Override
    public HttpServletResponse handleDisabledFeatures(HttpServletRequest request, HttpServletResponse response) {
        ...
        return response;
    }

}

```

#### Routing

Certain routes may expose application capabilites that are gated by features. These routes can redirected if a feature is disabled to another endpoint.

```java
@GetMapping("/featureT")
@FeatureGate(feature = "feature-t" fallback= "/oldEndpoint")
@ResponseBody
public String featureT() {
    ...
}

@GetMapping("/oldEndpoint")
@ResponseBody
public String oldEndpoint() {
    ...
}
```

### Implementing a Feature Filter

Creating a feature filter provides a way to enable features based on criteria that you define. To implement a feature filter, the `FeatureFilter` interface must be implemented. `FeatureFilter` has a single method `evaluate`. When a feature specifies that it can be enabled with a feature filter, the `evaluate` method is called. If `evaluate` returns `true` it means the feature should be enabled. If `false` it will continue evaluating the Feature's filters until one returns true. If all return `false` then the feature is off.

Feature filters are found by being defined as `@Component` where there name matches the expected filter defined in the configuration.

```java
@Component("Random")
public class Random implements FeatureFilter {

    @Override
    public boolean evaluate(FeatureFilterEvaluationContext context) {
        double chance = Double.valueOf((String) context.getParameters().get("chance"));
        return Math.random() > chance/100;
    }

}
```

#### Parameterized Feature Filters

Some feature filters require parameters to decide whether a feature should be turned on or not. For example a browser feature filter may turn on a feature for a certain set of browsers. It may be desired that Edge and Chrome browsers enable a feature, while FireFox does not. To do this a feature filter can be designed to expect parameters. These parameters would be specified in the feature configuration, and in code would be accessible via the `FeatureFilterEvaluationContext` parameter of `evaluate`. `FeatureFilterEvaluationContext` has a property `parameters` which is a `HashMap<String, Object>`.

### Request Based Features/Snapshot

There are scenarios which require the state of a feature to remain consistent during the lifetime of a request. The values returned from the standard `FeatureManager` may change if the configuration source which it is pulling from is updated during the request. This can be prevented by using `FeatureManagerSnapshot` and `@FeatureOn( snapshot = true )`. `FeatureManagerSnapshot` can be retrieved in the same manner as `FeatureManager`. `FeatureManagerSnapshot` calls `FeatureManager`, but it caches the first evaluated state of a feature during a request and will return the same state of a feature during its lifetime.

## Getting started
## Key concepts
## Examples
## Troubleshooting
## Next steps
## Contributing
