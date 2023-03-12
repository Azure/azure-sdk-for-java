# Spring Native Sample

This is a sample application that demonstrates the usage of the Azure SDK for Java, as well as its GraalVM support, within a Spring Native application. The sample application itself is referred to as 'Azure Storage Explorer', and with a simplified user interface it allows for a person to easily upload, download, and delete files from an Azure Blob Storage container. 

## Getting Started

### Required environment variables

Before this application can run, you must set the following environment variables:

| Variable Name | Description |
|---------------|-------------|
|`azure_blob_storage_connection_string` | Set to the connection string available from the [Azure Portal](https://portal.azure.com) for a specific Azure Blob Storage account. |
|`azure_blob_storage_container_name` | Specifies which container should be created (if it does not already exist) and used to store the uploaded files. |

### Running the sample

To run the sample application without native image compilation, simply use the following command:

```shell
./mvnw spring-boot:run
```

To compile as a GraalVM native image, use the following command:

```shell
./mvnw -Pnative clean package
```

Once this completes, you will find an executable binary file in the `./target` directory called `spring-native-sample`, which you may execute as per usual, e.g:

```shell
./target/spring-native-sample
```

## Docker support

This project has been configured to let you generate a lightweight container running a native executable.
Docker should be installed and configured on your machine prior to creating the image, see [the Getting Started section of the reference guide](https://docs.spring.io/spring-native/docs/0.10.0-SNAPSHOT/reference/htmlsingle/#getting-started-buildpacks).

To create the image, run the following goal:

```
$ ./mvnw spring-boot:build-image
```

Then, you can run the app like any other container:

```
$ docker run --rm spring-native-sample:1.0.0-SNAPSHOT
```

```
┌───────────────────────────────────────────────────────────────┐
│  azure-groupname-servicename                                  │
│    │                                                          │
│    │                                                          │
│    └────── src/main/java                                      │
│              │                                                │
│              │                                                │
│              ├──── com.azure.groupname.servicename            │
│              │        │                                       │
│              │        │                                       │
│              │        ├─────FooClientBuilder.java             │
│              │        │                                       │
│              │        │                                       │
│              │        ├─────FooClient.java                    │
│              │        │                                       │
│              │        │                                       │
│              │        └─────FooAsyncClient.java               │
│              │                                                │
│              │                                                │
│              └──── com.azure.groupname.servicename.models     │
│                       │                                       │
│                       │                                       │
│                       ├──────InputModel.java                  │
│                       │                                       │
│                       │                                       │
│                       └──────OuputModel.java                  │
│                                                               │
└───────────────────────────────────────────────────────────────┘
```
