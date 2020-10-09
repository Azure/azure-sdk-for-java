# Release History

## 1.0.0-beta.2 (Unreleased)


## 1.0.0-beta.1 (2020-10-07)
Version 1.0.0-beta.1 is a preview of our efforts in creating a Azure Metrics Advisor client library that is developer-friendly
and idiomatic to the Java ecosystem. The principles that guide
our efforts can be found in the [Azure SDK Design Guidelines for Java](https://azure.github.io/azure-sdk/java_introduction.html).

For more information about this, and preview releases of other Azure SDK libraries, please visit
https://azure.github.io/azure-sdk/releases/latest/java.html.

- Two client design:
    - `MetricsAdvisorAdministrationClient` to perform creation, updation and deletion of Metrics Advisor resources.
    - `MetricsAdvisorClient` helps with querying API's to helps with listing incidents, listing root causes of incidents
    and adding feedback to tune your model.
- Authentication with API key supported using `MetricsAdvisorKeyCredential("<subscription_key>", "<api_key>")`.
- Reactive streams support using [Project Reactor](https://projectreactor.io/).

This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/metricsadvisor/azure-ai-metricsadvisor/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/metricsadvisor/azure-ai-metricsadvisor/src/samples)
demonstrate the new API.
