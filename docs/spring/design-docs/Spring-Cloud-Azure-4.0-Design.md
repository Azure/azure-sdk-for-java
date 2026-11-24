# Overview

The vision of **Spring Cloud Azure** is offering a convenient way to interact with **Azure** provided services using well-known Spring idioms and APIs for Spring developers. With this vision, we can confidently tell experienced Spring developers, "you already know how to create value with Azure."

# Where we fall short of delivering this vision

* **We do not enable the complete set of Azure configurations using only Spring configuration mechanism.** One of the main features in Spring Cloud Azure is to autoconfigure the SDK clients, but some autoconfigurations are not supported with our libraries, which will result our user fall back to sdk.
  * Our modules don't cover all SDK's configurations.
      - For example: **CosmosClientBuilder** support `permissions` and `gatewayMode` in sdk, but azure-spring-boot-starter-cosmos doesn't support user configure, if users want to configure these properties, they have to build azure sdk client by themselves instead of leverage our auto-configuration's support.
  * Our modules don't support some credentials supported by sdk.
     - For example: **azure-spring-boot-starter-cosmos** only support AzureKeyCredential but **azure-cosmos** also support TokenCredential.


* **We limit our modules to require Spring Boot in all cases.** Our modules are all based on spring boot, which limits the user's scenarios to use our libaries. 
     - For example: If user only use spring-integration with eventhubs through **azure-spring-integration-eventhubs** library, it should not import spring boot libraries, but **azure-spring-integration-eventhubs** dependends on Spring boot.


* **We force users to understand inconsistencies that arise from haphazard development choices.** As an open source project base on Spring eco-system, in some scenarios, our modules don't follow Spring's well-known convention, which will increase the learning cost for users who are familiar with Spring.
    - Naming of modules. 
      For starters, we have modules named `azure-spring-boot-starter-cosmos`,`azure-spring-cloud-starter-eventhubs`.
    - Starters should have pom only.   
      <img width="601" alt="" src="https://user-images.githubusercontent.com/4465723/134610787-31bf78fb-e1a5-41d4-96ca-3a809e618df9.png">  
      The spring boot starter convention is that it is responsible for dependency management only. If we look at the code for [spring-cloud-gcp-starters](https://github.com/GoogleCloudPlatform/spring-cloud-gcp/tree/main/spring-cloud-gcp-starters), we will see that their starters are very simple, which allows the user to see very visually what dependencies are introduced by the starter. We should follow this convention too.

    - Our dependency structure doesn't have a clear and layered structure compared with Spring.  
        - **azure-spring-cloud-messaging** depends on **azure-spring-integration-core**, which is incorrect and confusing to users. If we look at how Spring organizes these modules, we will find spring has a spring-messaging module and a spring-integration-kafka module, `spring-integration-kafka` is dependent on `spring-messaging`. 
    
    * For historical reasons, our modules are divided into **azure-spring-boot-xxx** and **azure-spring-cloud-xxx**
       * This increases user's learning cost, users may not know the difference between the two and don't how to choose.
       * This increase maintenance costs, some codes need to be maintained in two modules that can't be shared very well.




# Goals
The main goal is to make it easier and more convenient for users to use Azure service with Spring. 
1. Simplify users' configuration, make it easier for users to configure Azure service.

2. Users are not blocked to use Azure SDK features.

3. Support all credentials supported by Azure SDKs.

4. Make it easy to support a new azure sdk.

5. Support all programming models supported by Spring which can be integrated with our Azure service.

6. Make the dependency structure between azure Spring modules clearer, each project has clear responsibility.

7. Enable users with different Spring abstractions such as Spring Data, Spring Integration to integrate Azure service easily.

8. Make our modules more Spring native, follow Spring's convention, reduce our customer's learning effort.



# Analysis
In order to achieve the goals, we are going to making the following changes:	
1. In order to achieve the goals 1~4, we redesigned our Spring-Cloud-Azure-Core-design module, which provides an abstraction layer between upper Azure Spring projects and Azure SDKs. 
2. In order to achieve the goals 5~7, we redesigned our directories, module structures, naming and dependencies.
   1. We divided our modules from one folder into several different folders, which is align with spring project.
   2. We unified the naming of our modules.
   3. We changed the structure of modules' dependencies.
3. In order to achieve the goals 8, We changed the content in our starter modules, to make the starter modules contain pom only.



## 1. Design Spring Cloud Azure Core

There are two design docs for the spring cloud azure core module:

- [Spring Cloud Azure Core design]
- [Spring Credential]

The spring cloud azure core module is the key component to achieve goals 1~4, to be more specific, it needs to cover below sub-goals:

- Provide a common template of configuring all the Azure SDK clients, no matter which protocol it uses, HTTP or AMQP.
- Provide the ability to configure every single configuration item the Azure SDK clients expose. To be more specific:
  - All the authentication methods each Service or SDK supports.
  - Proxy configurations.
  - Retry configurations.
  - Logging configurations.
  - Underlying client configurations, for example, HttpClient.
  - Other sdk configurations.
- Provide an extensible way for replacing Azure SDK provided implementations with Spring common implementations, such as using WebClient as HttpClient and configuring loggings with Spring properties.
- Provide abstraction support for upper-layer functions, such as:
  - `spring-retry` support
  - micrometer
  - tracing



## 2. Design modules' structure

### 2.1 Break down the directory from one to several different folders

We have only one folder called `spring` before, we placed all our modules into this folder, there are more than 50 modules in the folder. We created several more folders to categorize our modules, below is the overall structure.


![image](https://user-images.githubusercontent.com/4465723/137646152-8c0581b4-90a0-4b4e-ab70-531a044162b0.png)    

![image](https://user-images.githubusercontent.com/4465723/137681109-ec83b220-e94d-44e0-bdef-a87a6d2e6fa9.png)

###  2.2 Unified the naming of our modules.

We unified our module names to follow the same convention: `spring-[cloud|messaging|integration|security]-azure-xxx`, below is the detailed list.
![image](https://user-images.githubusercontent.com/4465723/137646542-6cdb5d40-8c4a-415f-bf49-eec8389314da.png)

### 2.3 Change the structure of modules' dependencies.

#### 2.3.1 Why to redesign the modules' dependencies.

- Spring provides different layered abstractions such as Spring integration, Spring Data, Spring Cloud Stream, etc. We should enable users to integrate our Azure services with these different abstractions.
- Below is an [example](https://viewer.diagrams.net/?highlight=0000ff&edit=_blank&layers=1&nav=1#R7Vxtb6O4Fv41lfZ%2ByAgwJsnHSdrOSjsjdW91tTOfVg44hC3BWSBtMr%2F%2B2mADxobQvBDaaVW1%2BAUDz%2BNzfM7xyw2Yr3dfYrRZfSMeDm8sw9vdgNsby7LAeEz%2FsZx9njOx7TzDjwMvzzLLjMfgJ%2BaZBs%2FdBh5OpIopIWEabORMl0QRdlMpD8UxeZGrLUkoP3WDfKxkPLooVHP%2FCrx0xb%2FCGpf5v%2BPAX4knm840L1kjUZl%2FSbJCHnmpZIG7GzCPCUnzq%2FVujkMGnsAlv%2B%2B%2BobR4sRhHaZcb3P0f2IeR%2F%2F3B%2Fva8Wf0e%2FGn%2BMwJW3swzCrf8i7%2BiCNMc3n6S7gUUyUuwDlkhmCUpilNOFn0smK1IHPwkUYpCmsMyROX7IAznJCRx1gZwbmcA0DecqW%2FPH%2FiM4xTvKln8a75gssZpvKdVeOnIFp2E9y1o8%2FRLyZRZ9KRVhSaH04d47%2FCLxovn%2FZf2JhT5IS7bt7o2LyqK5lGY4jhCKZ6RbeQlVZroReXbyqyMvFcQaU76JfJufje%2FvxSR9hj8CkTGZL79Ed9%2B%2Bfk0B7uHv%2F81vf%2BFI%2BAoRCoE%2BjHZblTg2ztGnY5CZ6KFaNdopcmRSTI1IBo6EE3DaiZJgu%2B1WJn20Vi1Y388VmKYszqCU%2B9h58NGgeYJLZ%2FQyA0DBkIdpph1aOxxoX9ZBSl%2B3CCXlb7QkZ1phnTNdIJJL5eyLri%2Fv506TqZNYvKEKyWes3AgK%2FFj5LHnVsqW2Y9Wg7STfVivVFCf9Im5OpgmmziI%2FBHtPfSdT0T9%2FDiJUmui9NZpn7iBJtzWOEmQT68GipwFTVXOexVzVQNy6NLdQDGzjamCmdMnZLAJsrhofnioQaD2tF51m2qWrAM3Zo3jmOq3GA8UOAfamqG4T%2BTGTf3NQykdj8l6TaJkoOiNxxpDBvSJnurXxBi5KRl0p5sC1Tju1xJxFGSw5%2BNHniRxuiI%2BiVB4V%2BbOZOzKOl8J2XDE%2FsFpuuf%2BItqmRMaTwhjvv7P7P0GR%2FMGbyxK3Oym15yl3Gz9nz30FKcJWT8g2dnEbFFzfU0%2FXx6005%2FUYTq0cxzhEafAsB4jOz%2BD4CAYlIM9Jp2PYEqGfzAOUZqkHHAfZAMEzr0atNSxqJ0Oi9lWSejUGwaAYFOp9GAyC6VgWTsN6U9JpD4tbNYb0IZ0HGITDYhC8WQY9lKyubwk5w6LTvjqdw6BlPCxa4NVpedtSNhkUnVZjWCybN1C4voSj3Tj%2F0uhoA9jN0QZncLT102sqbG93Ah1Mu867TlrwfJvzrvYAjIZdkH6vXP8oNRtNlbqMJZpNvtZuelB7gXFXo31Y4RLQGGUNohT7MX0FEl1GlbXrhe6qbCoLowgAVYVFdNJzq7ZWhtshvUQU9kyIOub1ENV%2FgjrR%2BXYX6RRTxAe1eTEj%2Bo5Gi%2BtHZ7uKjRjXDml%2BsTDzsOYflntoN06BuyHZeqMkjTFajxYBhTse8UUwvSktIfHdlxRMZDGzoSIFltnrMKDO%2FGrwHSiaZk1pjVU0tes1LoYmHFTk%2BMTo4okKZ9pV4QwrQAyvH4%2B6UOCjJz7hsFwHeMxcea%2Fxxb54GdYkm3jvVwzsAx2FoMaU1a55u9yw0%2BjLLQhJR5mHQUFEbrpFKXUfBoqjbX6C0njuaHyEca%2FAWq3AuiRaBv6W%2B8qbmLg4SQaL79g2DqFbOFv9oNu4MjhDlylSgfBQ7XnTMA6CerEuq%2F0Gtcc%2BEBIq8FUCEO4qCL2vaE%2B27N2osnCfRGoW44SOag8COLOW9Q0JS0QKWpjtYY1H9gh%2Bn0xj8oRTd9VokZ4e1jAnZwlrnMTQpcJE1X0bEclvevU0Q2ufOoz7a4M703cXO4LqktaaXZMPxty%2B6c%2BwOd6uadkSpttIcjnVdtIkjqxtzLq2OUuMTkhwxWjXDzRn93n5rQ8kYNu%2FSv1X4xDUJSJ3G%2FhtJ0lD64dWpOGzqzNBkxXasMvtOswrgBnrqYFL%2BUALHD6QJGBWFq2yIGlK1pUKn8PAZwUpqUkGHcPCIMLzYvd4DyPOuGYQ6ALpuiX2zqWkxlQjfO%2BcAlibazOvTUFxSMDQFdfl9ZEJaj6eXR8uGjSS2pbjHGwrV8QX026WGsO4sZyQGc7JBkUS486%2FW3YWxMzNzbDPDEx%2FgX6jb0x%2F6fMN7dV%2FbqxcLIwltfVGS7QOwn1%2B%2B5pEJMmsAqlKkrHOKhibXVG0oCa3n%2FWikfwGv1n2pHicxaIy4hqyh4vXpld%2B%2Fh%2FWzwOBFDGWmx12UaQEgjDDkObcsmvWOGQoQcrFobpmUVf01aOascpmcqKKEvaH0UUzOGFQUCbq5N%2FPKpTg0tTnrKppMYQzOwlm0GhwLioXWOf1GcaQIy7nZLhXn5ylfPGa9Tn66kdkgIgvEbeUny9%2FWqaDMs2eVcv0AyszsyTKNDpLuxR5nH1ITd2zwnXgeXmbuWOYbaFnBTkmGyZ6iXgkZO8Cbws45wKh7H0AmII5C9dK78z1vq5TVHfta3qG0mmKW4v%2BUmo2yPRKUXME2bJU0c6%2BLLDZigKRj5KywK%2B0VO%2BXWbLonNVMWWR4PUW2co1yI5gVgvihaD4UzYei%2BVA051I0lPFGXaOxXnNCJbsScS%2BAE6m6B5zEKoW5Ecvpo41T6ihx3UNTRoN78OoDcEbGJwDGpuw55KljrV5RhSyXCb6Mf60ujHznzl3dv9atau7XubPeiHN3MCrVnxdYi0pBwxGeXE9xqaLj%2FTpyo5zCVmBekRy7T8k5bevX%2BSWnXHcjrbopF%2BF0XjrVVeLEXsbq4o3Wkxz7X7zR%2Bt6%2FkPgYcqgJatYa9jrsAHVrkrDeFsJue7z9o2LRLRrNuez0Mgli%2Baw3PpuomWDsbvHpJrEuzJlTMxV0nOkU3jnObdFzplprCmfytrwP8gryNNOPY6dP8tSJ3SbyJKf5g8JysbtmA6Jp9sihkPYOHMrL3z9ILPwtzeFXpqURxIstGwPHHLBz8iLeA6dYaSy%2B03ZgWsM6dcN%2BT6cadSWr86YpMKzDG%2BD1968dWPPemYHpuYHVhySKmWOh5WBNe50vHqFnbHp1xo4nSSx5O7zV5%2BxsNojJDY94VzgqY93g7v8%3D) to demonstrate Spring's layered structure.
  ![image](https://user-images.githubusercontent.com/4465723/134799687-e49c28d4-bfcb-4fc3-887b-8a2ad48233b9.png)
- To integrate with Kafka, user may use `spring-kafka`,`spring-integration-kafka`,`spring-cloud-stream-binder-kafka`. It all depends on the programming model user want to use. 
- User should only care spring's abstraction, user don't need to care about interface in AzureSDKs, in order to achieve this goal, we need to redesign our modules' dependencies.

#### 2.3.2 The Expected structure for each projects

##### Expected structure for Spring Cloud Azure Starters.

- This is the expected structure about Spring Cloud Azure starters. 
- ![image](https://user-images.githubusercontent.com/4465723/138655493-234179de-ea50-4a69-9d23-c0aa5802a961.png)


##### Expected structure for Azure EventHubs.

- This is the expected structure for Azure EventHubs. Check this Spring-Cloud-Azure-Messaging-design for more info.
  ![mermaid-diagram-20211013135354](https://user-images.githubusercontent.com/63028776/137075027-a819bc3a-9e8f-4469-84c0-07bf3687f485.png)



##### Expected structure for Service Bus

- This is the expected structure for Service Bus. Check Spring-Cloud-Azure-Messaging-design for more info.
  ![mermaid-diagram-20211013135242](https://user-images.githubusercontent.com/63028776/137074908-abb66807-4a51-4b99-929d-1fee3eb8baf2.png)


## 3. Make the starter modules contain pom only

Remove all unrelated files in starters, to keep pom only in starters.

![image](https://user-images.githubusercontent.com/4465723/137656890-1d4a81f0-fdc7-4415-bc09-190bde00955c.png)


## Design each module based on Spring framework


| Design Doc   | Developer   |
|--|--|
| [Spring Cloud Azure Core design] | [saragluna](https://github.com/saragluna) |
| [Design for directory, module name and package path for Spring Cloud Azure messaging]|[yiliuTo](https://github.com/yiliuTo)|
| [Spring Cloud Azure auto configure design]| [chenrujun](https://github.com/chenrujun) |
| [Spring Cloud Azure Messaging design]|[yiliuTo](https://github.com/yiliuTo)|




## Success Criteria
- All Spring cloud azure 4.0 libraries release.
- All tests(IT/UT) pass with test Coverage > 90%
