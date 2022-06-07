# Azure Communication Job Router client library for Java

Azure Communication Job Router contains the APIs used in job router applications for Azure Communication Services.

[Source code][source] | [Package (Maven)][package] | [API reference documentation][api_documentation]
| [Product documentation][product_docs]

## Getting started

### Prerequisites

- An Azure account with an active subscription. [Create an account for free](https://azure.microsoft.com/free/?WT.mc_id=A261C142F).
- [Java Development Kit (JDK)](https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable) version 8 or above.
- [Apache Maven](https://maven.apache.org/download.cgi).
- A deployed Communication Services resource. You can use the [Azure Portal](https://docs.microsoft.com/azure/communication-services/quickstarts/create-communication-resource?tabs=windows&pivots=platform-azp) or the [Azure PowerShell](https://docs.microsoft.com/powershell/module/az.communication/new-azcommunicationservice) to set it up.
### Include the package
#### Include the BOM file

Please include the azure-sdk-bom to your project to take dependency on the General Availability (GA) version of the library. In the following snippet, replace the {bom_version_to_target} placeholder with the version number.
To learn more about the BOM, see the [AZURE SDK BOM README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/boms/azure-sdk-bom/README.md).

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.azure</groupId>
            <artifactId>azure-sdk-bom</artifactId>
            <version>{bom_version_to_target}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```
and then include the direct dependency in the dependencies section without the version tag.

```xml
<dependencies>
  <dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-communication-jobrouter</artifactId>
  </dependency>
</dependencies>
```

#### Include direct dependency
If you want to take dependency on a particular version of the library that is not present in the BOM,
add the direct dependency to your project as follows.

[//]: # ({x-version-update-start;com.azure:azure-communication-jobrouter;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-communication-jobrouter</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```

## Key concepts

### Job

A Job is a unit of work (demand), which must be routed to an available Worker (supply). A real-world example is an incoming call or chat in the context of a call center.

### Worker

A Worker is the supply available to handle a Job. When you use the SDK to register a Worker to receive jobs, you can specify:

- One or more queues to listen on.
- The number of concurrent jobs per Channel that the Worker can handle.
- A set of Labels that can be used to group and select workers.

A real-world example is an agent in a call center.

### Queue

A Queue is an ordered list of jobs, that are waiting to be served to a worker. Workers register with a queue to receive work from it.

A real-world example is a call queue in a call center.

### Channel

A Channel is a grouping of jobs by some type. When a worker registers to receive work, they must also specify for which channels they can handle work, and how much of each can they handle concurrently. Channels are just a string discriminator and aren't explicitly created.

Real-world examples are `voice calls` or `chats` in a call center.

### Offer

An Offer is extended by Job Router to a worker to handle a particular job when it determines a match. You can either accept or decline the offer with the JobRouter SDK. If you ignore the offer, it expires according to the time to live configured on the Distribution Policy.

A real-world example is the ringing of an agent in a call center.


### Distribution Policy

A Distribution Policy is a configuration set that controls how jobs in a queue are distributed to workers registered with that queue. This configuration includes:

- How long an Offer is valid before it expires.
- The distribution mode, which define the order in which workers are picked when there are multiple available.
- How many concurrent offers can there be for a given job.

### Labels

You can attach labels to workers, jobs, and queues. Labels are key value pairs that can be of `string`, `number`, or `boolean` data types.

A real-world example is the skill level of a particular worker or the team or geographic location.

### Worker selectors

Worker selectors can be attached to a job in order to target a subset of workers on the queue.

A real-world example is a condition on an incoming call that the agent must have a minimum level of knowledge of a particular product.

### Classification policy

A classification policy can be used to programmatically select a queue, determine job priority, or attach worker label selectors to a job.

### Queue selectors

Queue selectors can be attached to a classification policy in order to target a queue which fulfills certain conditions.
This queue is used enqueueing an incoming job.

A real-world example is a condition on an incoming call that the call has to get queued to a queue which supports `chat`.


### Exception policy

An exception policy controls the behavior of a Job based on a trigger and executes a desired action. The exception policy is attached to a Queue so it can control the behavior of Jobs in the Queue.


## Examples

### Client Initialization
To initialize the SMS Client, the connection string can be used to instantiate.
Alternatively, you can also use Active Directory authentication using DefaultAzureCredential.

```java sample-client
RouterClient routerClient = new RouterClientBuilder()
    .connectionString(connectionString);
    .buildClient();
```

### Create or update exception policy

Upsert an exception policy using `RouterClient` created from builder.

```java 
String connectionString = System.getenv("AZURE_TEST_JOBROUTER_CONNECTION_STRING");
RouterClient routerClient = new RouterClientBuilder()
    .connectionString(connectionString)
    .buildClient();

/**
 * Define an exception trigger.
 * This sets off exception when there are at least 10 jobs in a queue.
 */
QueueLengthExceptionTrigger exceptionTrigger = new QueueLengthExceptionTrigger();
exceptionTrigger.setThreshold(10);

/**
 * Define an exception action.
 * This sets up what action to take when an exception trigger condition is fulfilled.
 */
ExceptionAction exceptionAction = new CancelExceptionAction();

/**
 * Defining exception rule combining the trigger and action.
 */
ExceptionRule exceptionRule = new ExceptionRule();
exceptionRule.setActions(Collections.singletonMap("CancelJobActionWhenQueueIsFull", exceptionAction));
exceptionRule.setTrigger(exceptionTrigger);

/**
 * Create the exception policy.
 */
ExceptionPolicy exceptionPolicy = new ExceptionPolicy();
exceptionPolicy.setExceptionRules(Collections.singletonMap("TriggerJobCancellationWhenQueueLenIs10", exceptionRule));
routerClient.upsertExceptionPolicy(exceptionPolicyId, exceptionPolicy);

System.out.printf("Successfully created exception policy with id: %s %n", exceptionPolicyId);

/**
 * Add additional exception rule to policy.
 */
WaitTimeExceptionTrigger waitTimeExceptionTrigger = new WaitTimeExceptionTrigger();
waitTimeExceptionTrigger.setThreshold("PT1H");

ExceptionRule waitTimeExceptionRule = new ExceptionRule();
waitTimeExceptionRule.setTrigger(waitTimeExceptionTrigger);
waitTimeExceptionRule.setActions(Collections.singletonMap("CancelJobActionWhenJobInQFor1Hr", exceptionAction));

exceptionPolicy.setExceptionRules(Collections.singletonMap("CancelJobWhenInQueueFor1Hr", waitTimeExceptionRule));

/**
 * Upsert policy using routerClient.
 */
routerClient.upsertExceptionPolicy(exceptionPolicyId, exceptionPolicy);

System.out.println("Exception policy has been successfully updated.");
```

### Get an exception policy

Using `RouterClient` retrieve an existing exception policy.

```java 
String connectionString = System.getenv("AZURE_TEST_JOBROUTER_CONNECTION_STRING");
RouterClient routerClient = new RouterClientBuilder()
    .connectionString(connectionString)
    .buildClient();

Response<ExceptionPolicy> exceptionPolicyResponse = routerClient.getExceptionPolicy(exceptionPolicyId);
System.out.printf("Response headers are %s. Url %s  and status code %d %n", exceptionPolicyResponse.getHeaders(),
    exceptionPolicyResponse.getRequest().getUrl(), exceptionPolicyResponse.getStatusCode());
System.out.printf("Successfully fetched exception policy with id: %s %n", exceptionPolicyResponse.getValue().getId());
```

### List exception policies

Using `RouterClient` to retrieve a list of exception policies that have been already created.

```java 
String connectionString = System.getenv("AZURE_TEST_JOBROUTER_CONNECTION_STRING");
RouterClient routerClient = new RouterClientBuilder()
    .connectionString(connectionString)
    .buildClient();

PagedIterable<PagedExceptionPolicy> exceptionPolicyPagedIterable = routerClient.listExceptionPolicies();
exceptionPolicyPagedIterable.iterableByPage().forEach(resp -> {
    System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
        resp.getRequest().getUrl(), resp.getStatusCode());
    resp.getElements().forEach(exceptionPolicy -> {
        System.out.printf("Retrieved exception policy with id %s %n.", exceptionPolicy.getId());
    });
});
```

### Delete an exception policy

Using `RouterClient` delete an exception policy.

```java 
String connectionString = System.getenv("AZURE_TEST_JOBROUTER_CONNECTION_STRING");
RouterClient routerClient = new RouterClientBuilder()
    .connectionString(connectionString)
    .buildClient();
routerClient.deleteExceptionPolicy(exceptionPolicyId);
```

## Troubleshooting

Running into issues? This section should contain details as to what to do there.

## Next steps
- [Read more about Router in Azure Communication Services][router_concepts]

### More sample code
Please take a look at the [samples](https://github.com/cparisineti/azure-sdk-for-java/tree/feature/jobrouter-initial-commit/sdk/communication/azure-communication-jobrouter/src/samples/) directory for detailed examples of how to use this library.


## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a [Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.
