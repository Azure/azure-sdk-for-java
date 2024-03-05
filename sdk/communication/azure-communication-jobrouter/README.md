# Azure Communication Job Router client library for Java

Azure Communication Job Router contains the APIs used in job router applications for Azure Communication Services.

[Source code][source_code] | [Package (Maven)][package] | [API reference documentation][api_documentation] | [Product documentation][product_docs]

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
    <version>1.1.1</version>
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
JobRouter has two clients, JobRouterAdministrationClient and JobRouterClient. Both of them
can be initialized using the endpoint and access key.

```java 
String endpoint = <endpoint>;
String accessKey = <accessKey>;
JobRouterAdministrationClient routerAdminClient = new JobRouterAdministrationClientBuilder()
        .endpoint(endpoint)
        .addPolicy(new HmacAuthenticationPolicy(new AzureKeyCredential(accessKey)))
        .buildClient();
JobRouterClient routerClient = new JobRouterClientBuilder()
        .endpoint(endpoint)
        .addPolicy(new HmacAuthenticationPolicy(new AzureKeyCredential(accessKey)))
        .buildClient();
```

Using `JobRouterClient` created from builder, create Job Router entities as described below.

### Create a Distribution Policy

```java 
CreateDistributionPolicyOptions createDistributionPolicyOptions = new CreateDistributionPolicyOptions(
    "distribution-policy-id",
    10.0,
    new LongestIdleMode()
        .setMinConcurrentOffers(1)
        .setMaxConcurrentOffers(10)
);
DistributionPolicy distributionPolicy = routerAdminClient.createDistributionPolicy(createDistributionPolicyOptions);
```

### Create a Queue

```java 
CreateQueueOptions createQueueOptions = new CreateQueueOptions("queue-id", distributionPolicy.getId());
RouterQueue jobQueue = routerAdminClient.createQueue(createQueueOptions);
```

### Create a Job

```java 
CreateJobOptions createJobOptions = new CreateJobOptions("job-id", "chat-channel", queueId)
            .setPriority(1)
            .setChannelReference("12345")
            .setRequestedWorkerSelectors(
                new ArrayList<>() {{
                    new RouterWorkerSelector()
                        .setKey("Some-skill")
                        .setLabelOperator(LabelOperator.GREATER_THAN)
                        .setValue(new LabelValue(10));
                }}
            );
RouterJob routerJob = routerClient.createJob(createJobOptions);
```

### Create a Worker

```java 
Map<String, RouterValue> labels = new HashMap<String, RouterValue>() {
    {
        put("Label", new RouterValue("Value"));
    }
};

Map<String, RouterValue> tags = new HashMap<String, RouterValue>() {
    {
        put("Tag", new RouterValue("Value"));
    }
};

RouterChannel channel = new RouterChannel("router-channel", 1);

List<RouterChannel> channels = new ArrayList<RouterChannel>() {
    {
        add(channel);
    }
};

List<String> queues = new ArrayList<String>() {
    {
        add(jobQueue.getId());
    }
};

CreateWorkerOptions createWorkerOptions = new CreateWorkerOptions(workerId, 10)
    .setLabels(labels)
    .setTags(tags)
    .setAvailableForOffers(true)
    .setChannels(channels)
    .setQueues(queues);

RouterWorker routerWorker = routerClient.createWorker(createWorkerOptions);
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

<!-- LINKS -->
[source_code]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/communication/azure-communication-jobrouter/src
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[package]: https://search.maven.org/artifact/com.azure/azure-communication-jobrouter
[api_documentation]: https://aka.ms/java-docs
[rest_docs]: https://learn.microsoft.com/rest/api/communication/
[product_docs]: https://learn.microsoft.com/azure/communication-services/
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcommunication%2Fazure-communication-jobrouter%2FREADME.png)
