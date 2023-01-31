# Azure XML shared library for Java

[![Build Documentation](https://img.shields.io/badge/documentation-published-blue.svg)](https://azure.github.io/azure-sdk-for-java)

Azure XML provides shared primitives, abstractions, and helpers for XML.

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.

### Include the package

#### Include direct dependency

If you want to take dependency on a particular version of the library that is not present in the BOM,
add the direct dependency to your project as follows.

[//]: # ({x-version-update-start;com.azure:azure-xml;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-xml</artifactId>
  <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts

### XmlSerializable

`XmlSerializable` is used to define how an object is XML serialized and deserialized using stream-style serialization
where the object itself manages the logic for how it's handled with XML. The interface provides an instance-based
`toXml` API which handles writing the object to an `XmlWriter` and a static `fromXml` API which implementations must
override to define how an object is created by reading from an `XmlReader`.

### XmlToken

`XmlToken` is a basic enum that indicates the current state in an XML stream.

### XmlReader

`XmlReader` provides both basic, reading primitive and boxed primitive types, and convenience, reading an object, APIs
for reading XML. `XmlReader` is provided to allow for any underlying XML parser to implement it, such as Woodstox or
XMLStreamReader, as long as the implementation passes the tests provided by this package's test-jar 
(`XmlReaderContractTests`).

`XmlReader` is a simplified XML parser where it only supports reading element-by-element with the ability to retrieve
the namespace and attributes for that element. `XmlReader` doesn't progress forward in the XML stream until 
`nextElement` is called, meaning that `XmlReader.getIntElement` could be called indefinitely returning the same integer
without error until `nextElement` progresses the XML stream forward.

`XmlReader` doesn't take ownership of the XML input source and therefore won't close any resources if the XML is
provided using an `InputStream` or `Reader`.

### XmlWriter

`XmlWriter` provides basic APIs for writing XML. `XmlWriter` is provided to allow for any underlying XML writer to
implement it, such as Woodstox or XMLStreamWriter, as long as the implementation passes the tests provided by this
package's test-jar (`XmlWriterContractTests`).

`XmlWriter` must be periodically flushed to ensure content written to it is flushed to the underlying container type,
generally an `OutputStream` or `Writer`. Failing to flush may result in content being lost. Closing the `XmlWriter` will
also flush content, so it's best practice to use `XmlWriter` in a try-with-resources block where the `XmlWriter` will
be closed once it's finished being used.

`XmlWriter` doesn't take ownership of the XML output source and therefore won't close any resources if the XML is being
written to an `OutputStream` or `Writer`.

### XmlProvider

`XmlProvider` is a service provider interface which allows for `XmlReader`s and `XmlWriter`s to be created using
implementations found on the classpath. `XmlProvider` can also create the default implementations which are provided by
this package if an implementation isn't found on the classpath.

## Examples

### XmlSerializable

```java xmlserializablesample-basic
public class XmlSerializableExample implements XmlSerializable<XmlSerializableExample> {
    private boolean aBooleanAttribute;
    private Double aNullableDecimalAttribute;
    private int anIntElement;
    private String aStringElement;

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter) throws XMLStreamException {
        xmlWriter.writeStartElement("example");

        // Writing attributes must happen first so that they are written to the object start element.
        xmlWriter.writeBooleanAttribute("aBooleanAttribute", aBooleanAttribute);
        xmlWriter.writeNumberAttribute("aNullableDecimalAttribute", aNullableDecimalAttribute);

        xmlWriter.writeIntElement("anIntElement", anIntElement);
        xmlWriter.writeStringElement("aStringElement", aStringElement);

        return xmlWriter.writeEndElement();
    }

    public XmlSerializableExample fromXml(XmlReader xmlReader) throws XMLStreamException {
        // readObject is a convenience method on XmlReader which prepares the XML for being read as an object.
        // If the current token isn't an XmlToken.START_ELEMENT the next token element will be iterated to, if it's
        // still not an XmlToken.START_ELEMENT after iterating to the next element an exception will be thrown. If
        // the next element is an XmlToken.START_ELEMENT it will validate that the XML element matches the name
        // expected, if the name doesn't match an exception will be thrown. If the element name matches the reader
        // function will be called.
        return xmlReader.readObject("example", reader -> {
            // Since this class has no constructor reading to fields can be done inline.
            // If the class had a constructor with arguments the recommendation is using local variables to track
            // all field values.

            XmlSerializableExample result = new XmlSerializableExample();

            // Reading attributes must happen first so that the XmlReader is looking at the object start element.
            result.aBooleanAttribute = reader.getBooleanAttribute(null, "aBooleanAttribute");
            result.aNullableDecimalAttribute = reader.getNullableAttribute(null, "aNullableDecimalAttribute",
                Double::parseDouble);

            while (reader.nextElement() != XmlToken.END_ELEMENT) {
                QName elementName = reader.getElementName();

                // Since this object doesn't use namespaces we can work with the local part directly.
                // If it had namespaces the full QName would need to be inspected.
                String localPart = elementName.getLocalPart();
                if ("anIntElement".equals(localPart)) {
                    result.anIntElement = reader.getIntElement();
                } else if ("aStringElement".equals(localPart)) {
                    // getStringElement coalesces XML text and XML CData into a single string without needing to
                    // manage state.
                    result.aStringElement = reader.getStringElement();
                } else {
                    // Skip element when the element is unknown.
                    reader.skipElement();
                }
            }

            return result;
        });
    }
}
```

## Next steps

Get started with Azure libraries that are [built using Azure Core](https://azure.github.io/azure-sdk/releases/latest/#java).

## Troubleshooting

If you encounter any bugs, please file issues via [GitHub Issues](https://github.com/Azure/azure-sdk-for-java/issues/new/choose)
or checkout [StackOverflow for Azure Java SDK](https://stackoverflow.com/questions/tagged/azure-java-sdk).

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

<!-- links -->
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcore%2Fazure-xml%2FREADME.png)
