# Azure common test library for Java

Library containing common classes used to test Azure SDK client libraries.

* Create live or playback JUnit tests using [TestBase](./src/main/java/com/azure/common/test/TestBase.java) and
  leveraging [InterceptorManager](./src/main/java/com/azure/common/test/InterceptorManager.java) to keep track of
  network calls.
  ```java
  public class LiveSessionTests extends TestBase { }
  ```
* Record network calls using using pipeline policy, 
  [RecordNetworkCallPolicy](./src/main/java/com/azure/common/test/policy/RecordNetworkCallPolicy.java).
  ```java
  // All network calls are kept in the networkData variable.
  RecordedData networkData = new RecordedData();
  HttpPipeline pipeline = new HttpPipeline(new RecordNetworkCallPolicy(recordedData));
  ```
* Playback test session records with [PlaybackClient](./src/main/java/com/azure/common/test/http/PlaybackClient.java).
  ```java
  // Creates a HTTP client that plays back responses in recordedData.
  HttpClient playbackClient = new PlaybackClient(recordedData, null);
  ```

## Getting started

To use this package, add the following to your _pom.xml_.
```xml
  <dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-common-test</artifactId>
    <version>1.0.0-SNAPSHOT</version>
  </dependency>
```
