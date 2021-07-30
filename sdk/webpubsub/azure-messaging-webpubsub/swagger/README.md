## Generate autorest code

``` yaml
use: '@autorest/java@4.0.24'
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/webpubsub/data-plane/WebPubSub/preview/2021-05-01-preview/webpubsub.json
java: true
output-folder: ..
namespace: com.azure.messaging.webpubsub
generate-client-interfaces: false
sync-methods: all
client-side-validations: true
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
context-client-method-parameter: true
required-parameter-client-methods: false 
generate-client-as-impl: true
service-interface-as-public: true
use-iterable: true
```

### Customization

```java
import org.slf4j.Logger;

public class WebPubsubCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        customization.getPackage("com.azure.messaging.webpubsub.models")
            .getClass("ContentType")
            .rename("WebPubSubContentType");
    }
}
```
