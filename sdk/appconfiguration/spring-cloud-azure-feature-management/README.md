# Spring Cloud for Azure feature management client library for Java

## Feature Management

Feature flags provide a way for Spring Boot applications to turn features on or off dynamically. Developers can use feature flags in simple use cases like conditional statement to more advanced scenarios like conditionally adding routes. Feature Flags are not dependent of any spring-cloud-azure dependencies, but may be used in conjunction with spring-cloud-azure-appconfiguration-config.

Here are some of the benefits of using this library:

* A common convention for feature management
* Low barrier-to-entry
  * Supports application.yml file feature flag setup
* Feature Flag lifetime management
  * Configuration values can change in real-time, feature flags can be consistent across the entire request

## Feature Flags

Feature flags are composed of two parts, a name and a list of feature-filters that are used to turn the feature on.

## Feature Filters

Feature filters define a scenario for when a feature should be enabled. When a feature is evaluated for whether it is on or off, its list of feature-filters are traversed until one of the filters decides the feature should be enabled. At this point the feature is considered enabled and traversal through the feature filters stops. If no feature filter indicates that the feature should be enabled, then it will be considered disabled.

As an example, a Microsoft Edge browser feature filter could be designed. This feature filter would activate any features it is attached to as long as an HTTP request is coming from Microsoft Edge.

## Registration

The Spring Configuration system is used to determine the state of feature flags. Any system can be used to have them read in, such as application.yml, spring-cloud-azure-appconfiguration-config and more.

## Feature Flag Declaration

The feature management library supports application.yml or bootstrap.yml as a feature flag source. Below we have an example of the format used to set up feature flags in a application.yml file.

```yaml
feature-management:
  feature-flags:
    feature-t: false
    feature-u:
      enabled-for:
        -
          name: Random
    feature-v:
      enabled-for:
        -
          name: TimeWindowFilter
          parameters:
            time-window-filter-setting-start: "Wed, 01 May 2019 13:59:59 GMT"
            time-window-filter-setting-end: "Mon, 01 July 2019 00:00:00 GMT"
    feature-w:
      evaluate: false
      enabled-for:
        -
          name: AlwaysOnFilter
```

The `feature-management` section of the YAML document is used by convention to load feature flags. In the section above, we see that we have provided three different features. Features define their filters using the `enabled-for`  property. We can see that feature `feature-t` is set to false with no filters set. `feature-t` will always return false, this can also be done for true. `feature-u` which has only one feature filter `Random` which does not require any configuration so it only has the name property. `feature-v` it specifies a feature filter named `TimeWindow`. This is an example of a configurable feature filter. We can see in the example that the filter has a parameter's property. This is used to configure the filter. In this case, the start and end times for the feature to be active are configured.

The `AlwaysOnFilter` is a Filter that always evaluates as `true`. This filter can be used to turn this feature flag on, without removing the other feature filters. The `evaluate` field is used to stop the evaluation of the feature filters, and results in the feature flag to always return `false`.

### Supported properties

Name | Description | Required | Default
---|---|---|---
spring.cloud.azure.feature.management.fail-fast | Whether throw RuntimeException or not when exception occurs | No |  true

## Consumption

The simplest use case for feature flags is to do a conditional check for whether a feature is enabled to take different paths in code. The use cases grow when additional using spring-cloud-azure-feature-flag-web to manage web based features.

### Feature Check

The basic form of feature management is checking if a feature is enabled and then performing actions based on the result. This is done through the autowiring `FeatureManager` and calling it's `isEnabledAsync` method.

```java
@Autowired
FeatureManager featureManager;

if(featureManager.isEnabledAsync("feature-t").block()) {
    // Do Something
}
```

`FeatureManager` can also be accessed by `@Component` classes.

### Controllers

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

### Disabled Action Handling

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

### Routing

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

## Implementing a Feature Filter

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

### Parameterized Feature Filters

Some feature filters require parameters to decide whether a feature should be turned on or not. For example a browser feature filter may turn on a feature for a certain set of browsers. It may be desired that Edge and Chrome browsers enable a feature, while FireFox does not. To do this a feature filter can be designed to expect parameters. These parameters would be specified in the feature configuration, and in code would be accessible via the `FeatureFilterEvaluationContext` parameter of `evaluate`. `FeatureFilterEvaluationContext` has a property `parameters` which is a `HashMap<String, Object>`.

## Request Based Features/Snapshot

There are scenarios which require the state of a feature to remain consistent during the lifetime of a request. The values returned from the standard `FeatureManager` may change if the configuration source which it is pulling from is updated during the request. This can be prevented by using `FeatureManagerSnapshot` and `@FeatureOn( snapshot = true )`. `FeatureManagerSnapshot` can be retrieved in the same manner as `FeatureManager`. `FeatureManagerSnapshot` calls `FeatureManager`, but it caches the first evaluated state of a feature during a request and will return the same state of a feature during its lifetime.

## Built-In Feature Filters

There are a few feature filters that come with the `azure-spring-cloud-feature-management` package. These feature filters are not added automatically, but can be referenced and registered as soon as the package is registered.

Each of the built-in feature filters have their own parameters. Here is the list of feature filters along with examples.

### PercentageFilter

This filter provides the capability to enable a feature based on a set percentage.

```yaml
feature-management:
  feature-flags:
    feature-v:
      enabled-for:
        -
          name: PercentageFilter
          parameters:
            percentage-filter-setting: 50
```

### TimeWindowFilter

This filter provides the capability to enable a feature based on a time window. If only `time-window-filter-setting-end` is specified, the feature will be considered on until that time. If only start is specified, the feature will be considered on at all points after that time. If both are specified the feature will be considered valid between the two times.

```yaml
feature-management:
  feature-flags:
    feature-v:
      enabled-for:
        -
         name: TimeWindowFilter
          parameters:
            time-window-filter-setting-start: "Wed, 01 May 2019 13:59:59 GMT",
            time-window-filter-setting-end: "Mon, 01 July 2019 00:00:00 GMT"
```

### TargetingFilter

This filter provides the capability to enable a feature for a target audience. An in-depth explanation of targeting is explained in the targeting section below. The filter parameters include an audience object which describes users, groups, and a default percentage of the user base that should have access to the feature. Each group object that is listed in the target audience must also specify what percentage of the group's members should have access. If a user is specified in the users section directly, or if the user is in the included percentage of any of the group rollouts, or if the user falls into the default rollout percentage then that user will have the feature enabled.

```yml
feature-management:
  feature-flags:
    target:
      enabled-for:
        -
          name: targetingFilter
          parameters:
            users:
              - Jeff
              - Alicia
            groups:
              -
                name: Ring0
                rolloutPercentage: 100
              -
                name: Ring1
                rolloutPercentage: 100
            defaultRolloutPercentage: 50
```

## Targeting

Targeting is a feature management strategy that enables developers to progressively roll out new features to their user base. The strategy is built on the concept of targeting a set of users known as the target audience. An audience is made up of specific users, groups, and a designated percentage of the entire user base. The groups that are included in the audience can be broken down further into percentages of their total members.

The following steps demonstrate an example of a progressive rollout for a new 'Beta' feature:

1. Individual users Jeff and Alicia are granted access to the Beta
1. Another user, Mark, asks to opt-in and is included.
1. Twenty percent of a group known as "Ring1" users are included in the Beta.
1. The number of "Ring1" users included in the beta is bumped up to 100 percent.
1. Five percent of the user base is included in the beta.
1. The rollout percentage is bumped up to 100 percent and the feature is completely rolled out.
1. This strategy for rolling out a feature is built in to the library through the included TargetingFilter feature filter.

### Targeting in an Application

An example web application that uses the targeting feature filter is available in the [example project][example_project].

To begin using the `TargetingFilter` in an application it must be added as a `@Bean` like any other Feature Filter. `TargetingFilter` relies on another `@Bean` to be added to the application, `ITargetingContextAccessor`. The `ITargetingContextAccessor` allows for defining the current `TargetingContext` to be used for defining the current user id and groups. An example of this is:

```java
public class TargetingContextAccessor implements ITargetingContextAccessor {

    @Override
    public Mono<TargetingContext> getContextAsync() {
        TargetingContext context = new TargetingContext();
        context.setUserId("Jeff");
        ArrayList<String> groups = new ArrayList<String>();
        groups.add("Ring0");
        context.setGroups(groups);
        return Mono.just(context);
    }

}
```

### Targeting Evaluation Options

Options are available to customize how targeting evaluation is performed across a given `TargetingFilter`. An optional parameter `TargetingEvaluationOptions` can be set during `TargetingFilter` creation.

```java
    @Bean
    public TargetingFilter targetingFilter(ITargetingContextAccessor contextAccessor) {
        return new TargetingFilter(contextAccessor, new TargetingEvaluationOptions().setIgnoreCase(true));
    }
```

## Dynamic Features

When new features are being added to an application there may come a time when a feature has multiple different proposed design options. A common pattern when this happens is to do some form of A/B testing. That is, provide a different version of the feature to different segments of the user base, and judge off user interaction which is better. The dynamic feature functionality contained in this library aims to proivde a simplistic, standardized method for developers to perform this form of A/B testing.

In the scenario above, the different proposals for the design of a feature are referred to as variants of the feature. The feature itself is referred to as a dynamic feature. The variants of a dynamic feature can have types ranging from object, to string, to integer and so on. There is no limit to the amount of variants a dynamic feature may have. A developer is free to choose what type should be returned when a variant of a dynamic feature is requested. They are also free to choose how many variants are available to select from.

Each variant of a dynamic feature is associated with a different configuration of the feature. Additionally, each variant of a dynamic feature contains information describing under what circumstances the variant should be used.

### Consumption

Dynamic Features are accessible through `DynamicFeatureManager`.

The dynamic feature manager performs a resolution process that takes the name of a feature and returns a strongly typed value to represent the variant's value.

The following steps are performed during the retrieval of a dynamic feature's variant

1. Lookup the configuration of the specified dynamic feature to find the registered variants
1. Assign one of the registered variants to be used.
1. Resolve typed value based off of the assigned variant.

The Dynamic Feature Manager is made available by using `@Autwired` on `DynamicFeatureManager` and calling it's `getVariantAsync` method. In addition any required feature variant assigners need to be generated as `@Component`, such as the `TargetingEvaluator`.

**NOTE:** `TargetingEvaluator` extends `TargetingFilter` so it can be used for both at the same time.

### Usage Example

One possible example of when variants may be used is in a web application when there is a desire to test different visuals. In the following examples a mock of how one might assign different variants of a web page background to their users is shown.

```java
@Autowired
private DynamicFeatureManager dynamicFeatureManager;

...

//
// Modify view based off multiple possible variants
model.setBackgroundUrl(dynamicFeatureManager.GetVariantAsync("HomeBackground", String.class).block());
```

### Dynamic Feature Declaration

Dynamic features can be configured in a configuration file similarly to feature flags. Instead of being defined in the `FeatureManagement.FeatureFlags` section, they are defined in the `FeatureManagement.DynamicFeatures` section. Additionally, dynamic features have the following properties.

* Assigner: The assigner that should be used to select which variant should be used any time this feature is accessed.
* Variants: The different variants of the dynamic feature.
  * Name: The name of the variant.
  * Default: Whether the variant should be used if no variant could be explicitly assigned. One and only one default variant is required.
  * ConfigurationReference: A reference to the configuration of the variant to be used as typed options in the application.
  * AssignmentParameters: The parameters used in the assignment process to determine if this variant should be used.

An example of a dynamic feature named "ShoppingCart" is shown below.

```yml
feature-management:
  dynamic-features:
    ShoppingCart:
      assigner: Microsoft.Targeting
      variants:
      - default: true
        name: Big
        configuration-reference: ShoppingCart.Big
        assignment-parameters:
          audience:
            users:
            - Alec
            groups: []
      - name: Small
        configuration-reference: ShoppingCart.Small
        assignment-parameters:
          audience:
            users: []
            groups:
            - name: Ring1
              rollout-percentage: 50
            default-rollout-percentage: 30
ShoppingCart:
  Big:
    Size: 400
    Color: green
  Small:
    Size: 150
    Color: gray
```

In the example above we see the declaration of a dynamic feature in a json configuration file. The dynamic feature is defined in the `feature-management.dynamic-features` section of configuration. The name of this dynamic feature is `ShoppingCart`. A dynamic feature must declare a feature variant assigner that should be used to select a variant when requested. In this case the built-in `Microsoft.Targeting` feature variant assigner is used. The dynamic feature has two different variants that are available to the application. One variant is named `Big` and the other is named `Small`. Each variant contains a configuration reference denoted by the `configuration-reference` property. The configuration reference is a pointer to a section of application configuration that contains the options that should be used for that variant. The variant also contains assignment parameters denoted by the `assignment-parameters` property. The assignment parameters are used by the assigner associated with the dynamic feature. The assigner reads the assignment parameters at run time when a variant of the dynamic feature is requested to choose which variant should be returned.

An application that is configured with this `ShoppingCart` dynamic feature may request the value of a variant of the feature at runtime through the use of `DynamicFeatureManager.getVariantAsync`. The dynamic feature uses targeting for [variant assignment](#feature-variant-assignment) so each of the variants' assignment parameters specify a target audience that should receive the variant. For a walkthrough of how the targeting assigner would choose a variant in this scenario reference the [Microsoft.Targeting Assigner](#microsofttargeting-feature-variant-assigner) section. When the feature manager chooses one of the variants it resolves the value of the variant by resolving the configuration reference declared in the variant. The example above includes the configuration that is referenced by the `configuration-reference` of each variant.

### Feature Variant Assigners

A feature variant assigner is a component that uses contextual information within an application to decide which feature variant should be chosen when a variant of a dynamic feature is requested.

### Feature Variant Assignment

When requesting the value of a dynamic feature, the feature manager needs to determine which variant to use. The act of choosing which of the variants to be used is called "assignment." A built-in method of assignment allows the variants of a dynamic feature to be assigned to segments of an application's audience. This is the same [targeting](#microsofttargeting-feature-variant-assigner) strategy used by the targeting feature filter.

To perform assignments, the feature manager uses components known as feature variant assigners. Feature variant assigners choose which of the variants of a dynamic feature should be assigned when a dynamic feature is requested. Each variant of a dynamic feature defines assignment parameters so that when an assigner is invoked, the assigner can tell under which conditions each variant should be selected. It is possible that an assigner is unable to choose between the list of available variants based on the configured assignment parameters. In this case, the feature manager chooses the **default variant**. The default variant is a variant that is marked explicitly as the default. It is required to have a default variant when configuring a dynamic feature in order to handle the possibility that an assigner is not able to select a variant of a dynamic feature.

### Custom Assignment

There may come a time when custom criteria is needed to decide which variant of a feature should be assigned when a feature is referenced. This is made possible by an extensibility model that allows the act of assignment to be overridden. Every feature registered in the feature management system that uses feature variants specifies what assigner should be used to choose a variant.

```java
public interface IFeatureVariantAssigner extends IFeatureVariantAssignerMetadata {
    
    /**
     * Assign a variant of a dynamic feature to be used based off of customized criteria.
     * @param featureDefinition A variant assignment context that contains information needed to assign a variant for a
     *  dynamic feature.
     * @return The variant that should be assigned for a given dynamic feature.
     */
    public Mono<FeatureVariant> assignVariantAsync(FeatureDefinition featureDefinition);

}
```

### Built-In Feature Variant Assigners

There is a built-in feature variant assigner that uses targeting. It comes with the `azure-spring-cloud-feature-management` package. This assigner is not added automatically, but it can be referenced and configured as a `@Component`.

#### Microsoft.Targeting Feature Variant Assigner

This feature variant assigner provides the capability to assign the variants of a dynamic feature to targeted audiences. An in-depth explanation of targeting is explained in the [targeting](#targeting) section.

The assignment parameters used by the targeting feature variant assigner include an audience object which describes the user base that should receive the associated variant. The audience is made of users, groups, and a percentage of the entire user base. Each group object that is listed in the target audience is required to specify what percentage of the group's members should have receive the variant. If a user is specified in the users section directly, or if the user is in the included percentage of any of the group rollouts, or if the user falls into the default rollout percentage then that user will receive the associated variant.

```yml
ShoppingCart:
  assigner: Microsoft.Targeting
  variants:
  - default: true
    name: Big
    configuration-reference: ShoppingCart.Big
    assignment-parameters:
      audience:
        users:
        - 
          Alec
        groups:
        - 
          name: Ring0
          rollout-percentage: 100
        - 
          name: Ring1
          rollout-percentage: 50
  - name: Small
    configuration-reference: ShoppingCart.Small
    assignment-parameters:
      audience:
        users:
          - Susan
        groups:
        - 
          name: Ring1
          rollout-percentage: 50
        default-rollout-percentage: 80
```

Based on the configured audiences for the variants included in this feature, if the application is executed under the context of a user named `Alec` then the value of the `Big` variant will be returned. If the application is executing under the context of a user named `Susan` then the value of the `Small` variant will be returned. If a user match does not occur, then group matches are evaluated. If the application is executed under the context of a user in the group `Ring0` then the `Big` variant will be returned. If the user's group is `Ring1` instead, then the user has a 50% chance of being assigned to `Small`. If there is no user match nor group match, then the default rollout percentage is used. In this case, 80% of unmatched users will get the `Small` variant, leaving the other 20% to get the `Big` variant since it is marked as the Default.

When using the targeting feature variant assigner, make sure to register it as well as an implementation of [ITargetingContextAccessor](#itargetingcontextaccessor).

```java
@Component
@RequestScope
public class TargetingContextImpl implements ITargetingContextAccessor {
    
    @Autowired
    private HttpServletRequest request;

    @Override
    public Mono<TargetingContext> getContextAsync() {
        ...
    }

}
```

### Variant Resolution

When a variant of a dynamic feature has been chosen, the feature management system resolves the configuration reference associated with that variant. The resolution is done through the `configuration-reference` property. In the "[Configuring a Dynamic Feature](#configuring-a-dynamic-feature)" section we see a dynamic feature named `ShoppingCart`. The first variant of the feature, is named "Big", and is being referenced in the feature variant as `ShoppingCart.Big` in the configuration reference. The referenced section is shown below.

```yml
feature-variants:
  ShoppingCart:
    Big:
      size: 400
      color: "green"
```

In this case, there is a `@ConfigurationProperties` that implements `IDynamicFeatureProperties`

```java
@ConfigurationProperties(prefix = "feature-variants")
public class ApplicationProperties implements IDynamicFeatureProperties {

    private Map<String, ShoppingCart> shoppingCart;

    public Map<String, ShoppingCart> getShoppingCart() {
        return shoppingCart;
    }

    public void setShoppingCart(Map<String, ShoppingCart> shoppingCart) {
        this.shoppingCart = shoppingCart;
    }

}
```

`IDynamicFeatureProperties` flags `@ConfigurationProperties` as containing `FeatureVariants`. Multiple `@ConfigurationProperties` can have `IDynamicFeatureProperties`. The feature management system resolves the configuration reference by accessing the `@ConfigurationProperties` and getting the variant from the `Map`.

<!-- Links -->
[example_project]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/appconfiguration/feature-management-web-sample
