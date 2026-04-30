This wiki is to help you find the corresponding Spring Cloud Azure version and its support status with the given Spring Boot / Spring Cloud version.

## Spring Cloud Azure Support Status

Table 1: The support status of **Spring Cloud Azure** with the given **Spring Boot** / **Spring Cloud** version.

| Spring Boot version | Spring Cloud version | Spring Cloud Azure version | Spring Cloud Azure support status | Spring Framework version | JDK Version Range |
| ------------------- | -------------------- | -------------------------- | --------------------------------- | ------------------------ | ----------------- |
| 4.0.x               | 2025.1.x             | 7.x.x                      | In Support                        | 7.0.x                    | JDK 17-25         |
| 3.5.x               | 2025.0.x             | 6.x.x or 5.x.x             | In Support                        | 6.2.x                    | JDK 17-24         |
| 3.4.x               | 2024.0.x             | 5.x.x                      | In Support                        | 6.2.x                    | JDK 17-23         |
| 3.3.x               | 2023.0.x             | 5.x.x                      | In Support                        | 6.1.x                    | JDK 17-23         |
| 3.2.x               | 2023.0.x             | 5.x.x                      | In Support                        | 6.1.x                    | JDK 17-23         |
| 3.1.x               | 2022.0.x             | 5.x.x                      | In Support                        | 6.0.x                    | JDK 17-21         |
| 3.0.x               | 2022.0.x             | 5.x.x                      | In Support                        | 6.0.x                    | JDK 17-21         |
| 2.7.x               | 2021.0 (Jubilee)     | 4.x.x                      | In Support                        | 5.3.x                    | JDK 8-21          |
| 2.6.x               | 2021.0 (Jubilee)     | 4.x.x                      | End of Life                       | 5.3.x                    | JDK 8-21          |
| 2.5.x               | 2020.0 (ilford)      | 4.x.x                      | End of Life                       | 5.3.x                    | JDK 8-21          |
| 2.4.x               | 2020.0 (ilford)      | 3.x.x                      | End of Life                       | 5.3.x                    | JDK 8-21          | 
| 2.3.x               | Hoxton               | 3.x.x                      | End of Life                       | 5.2.x                    | JDK 8-13                  |


**NOTE**: 
 - Spring Boot 2.x requires Java 8 as minimum version, Spring Boot 3.x and Spring Boot 4.x requires Java 17 or higher. Please reference to [Spring Framework JDK Version Range](https://github.com/spring-projects/spring-framework/wiki/Spring-Framework-Versions#jdk-version-range) for detailed JDK version range a Spring Framework version supports. 
 - For detailed information about the Spring Boot version and Spring Cloud version supported in specific Spring Cloud Azure version, please refer to the [CHANGELOG.md](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/CHANGELOG.md).

## Which Version of Spring Cloud Azure Should I Use

Table 2: Mapping from **Spring Boot** / **Spring Cloud** version to **Spring Cloud Azure** / **azure-sdk-bom** version

| Spring Boot version | Spring Cloud version | Spring Cloud Azure version                                                                                                                                                                                    | azure-sdk-bom version                                                                                                                                       |
|---------------------|----------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 4.0.x               | 2025.1.x             | [7.0.0-beta.1](https://search.maven.org/artifact/com.azure.spring/spring-cloud-azure-autoconfigure/7.0.0-beta.1/jar)~[7.2.0](https://search.maven.org/artifact/com.azure.spring/spring-cloud-azure-autoconfigure/7.2.0/jar)                                                                           | [1.3.3](https://search.maven.org/artifact/com.azure/azure-sdk-bom/1.3.3/pom)~[1.3.6](https://search.maven.org/artifact/com.azure/azure-sdk-bom/1.3.6/pom) |
| 3.5.x               | 2025.0.x             | [5.23.0](https://search.maven.org/artifact/com.azure.spring/spring-cloud-azure-autoconfigure/5.23.0/jar)~[6.2.0](https://search.maven.org/artifact/com.azure.spring/spring-cloud-azure-autoconfigure/6.2.0/jar)                                                                                                        | [1.2.37](https://search.maven.org/artifact/com.azure/azure-sdk-bom/1.2.37/pom)~[1.3.3](https://search.maven.org/artifact/com.azure/azure-sdk-bom/1.3.3/pom) |
| 3.4.x               | 2024.0.x             | [5.19.0](https://search.maven.org/artifact/com.azure.spring/spring-cloud-azure-autoconfigure/5.19.0/jar)~[5.25.0](https://search.maven.org/artifact/com.azure.spring/spring-cloud-azure-autoconfigure/5.25.0/jar)                                                                                                        | [1.2.30](https://search.maven.org/artifact/com.azure/azure-sdk-bom/1.2.30/pom)~[1.2.37](https://search.maven.org/artifact/com.azure/azure-sdk-bom/1.2.37/pom) |
| 3.3.x               | 2023.0.x             | [5.13.0](https://search.maven.org/artifact/com.azure.spring/spring-cloud-azure-autoconfigure/5.13.0/jar)~[5.25.0](https://search.maven.org/artifact/com.azure.spring/spring-cloud-azure-autoconfigure/5.25.0/jar)                                                                                                        | [1.2.24](https://search.maven.org/artifact/com.azure/azure-sdk-bom/1.2.24/pom)~[1.2.37](https://search.maven.org/artifact/com.azure/azure-sdk-bom/1.2.37/pom) |
| 3.2.x               | 2023.0.x             | [5.7.0](https://search.maven.org/artifact/com.azure.spring/spring-cloud-azure-autoconfigure/5.7.0/jar)~[5.25.0](https://search.maven.org/artifact/com.azure.spring/spring-cloud-azure-autoconfigure/5.25.0/jar)                                                                                                        | [1.2.18](https://search.maven.org/artifact/com.azure/azure-sdk-bom/1.2.18/pom)~[1.2.37](https://search.maven.org/artifact/com.azure/azure-sdk-bom/1.2.37/pom) |
| 3.1.x               | 2022.0.x             | [5.3.0](https://search.maven.org/artifact/com.azure.spring/spring-cloud-azure-autoconfigure/5.3.0/jar)~[5.25.0](https://search.maven.org/artifact/com.azure.spring/spring-cloud-azure-autoconfigure/5.25.0/jar)                                                                                                        | [1.2.8](https://search.maven.org/artifact/com.azure/azure-sdk-bom/1.2.8/pom)~[1.2.37](https://search.maven.org/artifact/com.azure/azure-sdk-bom/1.2.37/pom) |
| 3.0.x               | 2022.0.x             | [5.0.0](https://search.maven.org/artifact/com.azure.spring/spring-cloud-azure-autoconfigure/5.0.0/jar)~[5.24.1](https://search.maven.org/artifact/com.azure.spring/spring-cloud-azure-autoconfigure/5.24.1/jar)                                                                                                        | [1.2.8](https://search.maven.org/artifact/com.azure/azure-sdk-bom/1.2.8/pom)~[1.2.37](https://search.maven.org/artifact/com.azure/azure-sdk-bom/1.2.37/pom) |
| 2.7.x               | 2021.0.x             | [4.0.0](https://search.maven.org/artifact/com.azure.spring/spring-cloud-azure-autoconfigure/4.0.0/jar)~[4.20.0](https://search.maven.org/artifact/com.azure.spring/spring-cloud-azure-autoconfigure/4.20.0/jar) | [1.2.0](https://search.maven.org/artifact/com.azure/azure-sdk-bom/1.2.0/pom)~[1.2.33](https://search.maven.org/artifact/com.azure/azure-sdk-bom/1.2.33/pom) |
| 2.6.x               | 2021.0.x             | [4.0.0](https://search.maven.org/artifact/com.azure.spring/spring-cloud-azure-autoconfigure/4.0.0/jar)~[4.20.0](https://search.maven.org/artifact/com.azure.spring/spring-cloud-azure-autoconfigure/4.20.0/jar) | [1.2.0](https://search.maven.org/artifact/com.azure/azure-sdk-bom/1.2.0/pom)~[1.2.33](https://search.maven.org/artifact/com.azure/azure-sdk-bom/1.2.33/pom) |
| 2.5.x               | 2020.0.x             | [4.0.0](https://search.maven.org/artifact/com.azure.spring/spring-cloud-azure-autoconfigure/4.0.0/jar)~[4.20.0](https://search.maven.org/artifact/com.azure.spring/spring-cloud-azure-autoconfigure/4.20.0/jar) | [1.2.0](https://search.maven.org/artifact/com.azure/azure-sdk-bom/1.2.0/pom)~[1.2.33](https://search.maven.org/artifact/com.azure/azure-sdk-bom/1.2.33/pom) |
| 2.4.x               | 2020.0.x             | [3.14.0](https://search.maven.org/artifact/com.azure.spring/azure-spring-boot/3.14.0/jar)                                                                                                                     | [1.1.1](https://search.maven.org/artifact/com.azure/azure-sdk-bom/1.1.1/pom)                                                                             |
| 2.3.x               | Hoxton               | [3.2.0](https://search.maven.org/artifact/com.azure.spring/azure-spring-boot/3.2.0/jar) | [1.0.2](https://search.maven.org/artifact/com.azure/azure-sdk-bom/1.0.2/pom) |

**NOTE**: **Spring Cloud Azure** 7.x now support Spring Boot 4. So if you're using Spring Boot 4, please upgrade to 7.x.

**NOTE**: **Spring Cloud Azure** 4.x.x have all reached end of life status and only support hotfixes of 4.19.0 until June 2025. So if you use Spring Boot 3, please upgrade to 5.x.

**NOTE**: **Spring Cloud Azure** 3.x.x have all reached end of life status and are no longer supported. Please upgrade to 4.x. You can refer to [Migration Guide for 4.0](https://learn.microsoft.com/azure/developer/java/spring-framework/migration-guide-for-4.0) to migrate to 4.x.

### I'm Using Spring Boot Version X
If you are using **Spring Boot** in your project, you can find related **Spring Cloud Azure** version from above table. For example: if you are using **Spring Boot** 4.0.x, you should use **Spring Cloud Azure**.

### I'm Using Spring Cloud Version Y
If you are using **Spring Cloud** in your project, you can also find related **Spring Cloud Azure** version from above table. For example, if you are using **Spring Cloud** 2025.1.x, you should use **Spring Cloud Azure**.

## How To Use This Version in My Project

Now that you know which version to use, you can add the **spring-cloud-azure-dependencies** to your application.

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.azure.spring</groupId>
      <artifactId>spring-cloud-azure-dependencies</artifactId>
      <version>7.2.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

You can refer to [Reference Doc](https://learn.microsoft.com/azure/developer/java/spring-framework/developer-guide-overview#setting-up-dependencies) to get more information about setting up dependencies.

## If My Spring Boot Version Can Not Found in Above Table
If your **Spring Boot** version cannot be found in above table, you can upgrade your **Spring Boot** version, or asking help by creating a new issue in [azure-sdk-for-java repo](https://github.com/Azure/azure-sdk-for-java/issues).

## If I'm Using Spring Cloud Azure 3.x.x
**Spring Cloud Azure** 3.x.x has reached end of life status and is no longer supported. 

 - It's suggested to upgrade to the latest version of 4.x. Please refer to [Migration Guide for 4.0](https://learn.microsoft.com/azure/developer/java/spring-framework/migration-guide-for-4.0) to get more information about how to do migration.
 - If you still want to use 3.x.x, please import bom like this:

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.azure.spring</groupId>
      <artifactId>azure-spring-boot-bom</artifactId>
      <version>3.14.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
    <dependency>
      <groupId>com.azure.spring</groupId>
      <artifactId>azure-spring-cloud-dependencies</artifactId>
      <version>2.14.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```
