# Spring Cloud Azure Native Reachability client library for Java

Hey, if you want to use Spring Boot 3 and Spring Cloud Azure 5.x to build a native image, you can add this `spring-cloud-azure-native-reachability:5.0.0-beta.1` to your dependencies, but please note this native support is not GA ready, it is still in beta support, so please expect breaking changes in further.

## Getting started

### Prerequisites
- A [Java Development Kit (JDK)][jdk_link], version 17.
- [Azure Subscription][azure_subscription]
- [Docker](https://docs.docker.com/installation/#installation) for [Buildpacks](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#native-image.developing-your-first-application.buildpacks) usage
- [GraalVM 22.3 - Java 17](https://www.graalvm.org/downloads/) and [Native Image](https://www.graalvm.org/22.0/reference-manual/native-image/) for [Native Build Tools](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#native-image.developing-your-first-application.native-build-tools) usage

### Include the package

[//]: # ({x-version-update-start;com.azure.spring:spring-cloud-azure-native-reachability;current})
```xml
<dependency>
  <groupId>com.azure.spring</groupId>
  <artifactId>spring-cloud-azure-native-reachability</artifactId>
  <version>5.0.0-beta.1</version>
</dependency>
```

This package is required to enable Spring Cloud Azure AOT support.

### Generate a native executable using Buildpacks

Using Maven

```shell
mvn -Pnative spring-boot:build-image
```

Using Gradle:

```shell
gradle bootBuildImage
```

### Generate a native executable using Native Build Tools

Using Maven

```shell
mvn -Pnative native:compile
```

Using Gradle:

```shell
gradle nativeCompile
```

For more details, please refer to [Getting started with GraalVM Native Application](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#native-image.developing-your-first-application)
documentation.

## Troubleshooting

- Fatal error: java.lang.SecurityException: class "com.azure.spring.cloud.xxx"'s signer information does not match signer information of other classes in the same package

  This is a known [issue](https://github.com/Azure/azure-sdk-for-java/issues/30320), the root cause is that the generated classes of Spring AOT are saved into the same package, and the JVM doesn't accept this operation in a signed Jar.

  Create a file *custom.security* with follow content:
    
    ```properties
    jdk.jar.disabledAlgorithms=MD2, MD5, RSA, DSA
    ```

  Update your maven plugin with below configuration:
  
    ```xml
    <build>
      <plugins>
        <plugin>
          <groupId>org.graalvm.buildtools</groupId>
          <artifactId>native-maven-plugin</artifactId>
          <configuration>
            <buildArgs>
              <arg>-Djava.security.properties=src/main/resources/custom.security</arg>
            </buildArgs>
          </configuration>
        </plugin>
        <plugin>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-maven-plugin</artifactId>
          <configuration>
            <image>
              <env>
                <BP_NATIVE_IMAGE_BUILD_ARGUMENTS>-Djava.security.properties=/workspace/BOOT-INF/classes/custom.security</BP_NATIVE_IMAGE_BUILD_ARGUMENTS>
              </env>
            </image>
          </configuration>
        </plugin>
      </plugins>
    </build>
    ```

  Add the below gradle configuration to your *build.gradle* file:

    ```groovy
    graalvmNative {
      binaries {
        main {
          buildArgs('-Djava.security.properties=' + file("$rootDir/custom.security").absolutePath)
        }
      }  
    }
    bootRun {
      systemProperty("java.security.properties", file("$rootDir/custom.security").absolutePath)
      systemProperty('spring.aot.enabled', 'true')
    }
    ```

[jdk_link]: https://learn.microsoft.com/azure/developer/java/fundamentals/java-jdk-install
[azure_subscription]: https://azure.microsoft.com/free