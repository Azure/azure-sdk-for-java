# Azure JSON shared library for Java

[![Build Documentation](https://img.shields.io/badge/documentation-published-blue.svg)](https://azure.github.io/azure-sdk-for-java)

Azure JSON provides shared primitives, abstractions, and helpers for JSON.

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.

### Include the package

#### Include direct dependency

If you want to take dependency on a particular version of the library that is not present in the BOM,
add the direct dependency to your project as follows.

[//]: # ({x-version-update-start;com.azure:azure-json;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-json</artifactId>
  <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts

### JsonSerializable

`JsonSerializable` is used to define how an object is JSON serialized and deserialized using stream-style serialization
where the object itself manages the logic for how it's handled with JSON. The interface provides an instance-based
`toJson` API which handles writing the object to a `JsonWriter` and a static `fromJson` API which implementations must
override to define how an object is created by reading from a `JsonReader`.

### JsonToken

`JsonToken` is a basic enum that indicates the current state in a JSON stream.

### JsonReader

`JsonReader` provides both basic, reading primitive and boxed primitive types, and convenience, reading arrays, maps,
and objects, APIs for reading JSON. `JsonReader` is provided to allow for any underlying JSON parser to implement it,
such as Jackson or GSON, as long as the implementation passes the tests provided by this package's test-jar 
(`JsonReaderContractTests`).

`JsonReader` doesn't progress forward in the JSON stream until `nextToken` is called, meaning that `JsonReader.getInt`
could be called indefinitely returning the same integer without error until `nextToken` progresses the JSON stream 
forward.

`JsonReader` allows for type conversion between JSON types, such as trying to convert a JSON string to a number or vice
versa, and for commonly used non-standard JSON values, such as `NaN`, `INF`, `-INF`, `Infinity`, and `-Infinity`.

`JsonReader` doesn't take ownership of the JSON input source and therefore won't close any resources if the JSON is 
provided using an `InputStream` or `Reader`.

#### Nesting Limits

To prevent `StackOverflowError`s `JsonReader`'s generic `readUntyped` API tracks how deeply nested the object being read
is, if the nesting passes the threshold of `1000` an `IllegalStateException` will be thrown in an attempt to prevent
the stack from overflowing.

### JsonWriter

`JsonWriter` provides both basic, writing primitives and boxed primitive types, and convenience, writing arrays, maps,
and objects, APIs for writing JSON. `JsonWriter` is provided to allow for any underlying JSON writer to implement it,
such as Jackson or GSON, as long as the implementation passes the tests provided by this package's test-jar
(`JsonWriterContractTests`).

`JsonWriter` allows for commonly used non-standard JSON values, such as `NaN`, `INF`, `-INF`, `Infinity`, and 
`-Infinity`, to be written using `writeNumberField` or `writeRawValue`.

`JsonWriter` won't write null `byte[]`, `Boolean`, `Number`, or `String` values when written as a field, 
`writeBinaryField`, `writeBooleanField`, `writeNumberField`, or `writeStringField`, if a null field needs to be written
use `writeNullField`.

`JsonWriter` must be periodically flushed to ensure content written to it is flushed to the underlying container type,
generally an `OutputStream` or `Writer`. Failing to flush may result in content being lost. Closing the `JsonWriter`
will also flush content, so it's best practice to use `JsonWriter` in a try-with-resources block where the `JsonWriter` 
will be closed once it's finished being used.

`JsonWriter` doesn't take ownership of the JSON output source and therefore won't close any resources if the JSON is
being written to an `OutputSteam` or `Writer`.

#### JSON State Management

To ensure that the JSON being written is valid `JsonWriter` will maintain the state of the JSON using `JsonWriteContext`
and on each attempt to write it will validate whether the operation is valid. The implementation of `JsonWriter` must
ensure this is tracked correctly, for example when nothing has been written the JSON state must be `ROOT` and `ROOT`
doesn't allow for JSON field names to be written.

### JsonProvider

`JsonProvider` is a service provider interface which allows for `JsonReader`s and `JsonWriter`s to be created using
implementations found on the classpath. `JsonProvider` can also create the default implementations which are provided
by this package if an implementation isn't found on the classpath.

#### JsonOptions

`JsonOptions` contains configurations that are respected by all implementations of `JsonReader`s and `JsonWriter`s. At
this time there is only one configuration for determining whether non-numeric numbers, `NaN`, `INF`, `-INF`, `Infinity`, 
and `-Infinity` are supported in JSON reading and writing with a default setting of `true`, that non-numeric numbers 
are allowed.

## Examples

### JsonSerializable

```java jsonserializablesample-basic
public class JsonSerializableExample implements JsonSerializable<JsonSerializableExample> {
    private int anInt;
    private boolean aBoolean;
    private String aString;
    private Double aNullableDecimal;

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();

        jsonWriter.writeIntField("anInt", anInt);
        jsonWriter.writeBooleanField("aBoolean", aBoolean);
        jsonWriter.writeStringField("aString", aString);
        // writeNumberField doesn't write the field if the value is null, if a null field is explicitly needed
        // null checking and using writeNullField should be used.
        jsonWriter.writeNumberField("aNullableDecimal", aNullableDecimal);

        // Example of null checking:
        // if (aNullableDecimal == null) {
        //     jsonWriter.writeNullField("aNullableDecimal");
        // } else {
        //     jsonWriter.writeNumberField("aNullableDecimal", aNullableDecimal);
        // }

        return jsonWriter.writeEndObject();
    }

    public JsonSerializableExample fromJson(JsonReader jsonReader) throws IOException {
        // readObject is a convenience method on JsonReader which prepares the JSON for being read as an object.
        // If the current token isn't initialized it will begin reading the JSON stream, then if the current token
        // is still null or JsonToken.NULL null will be returned without calling the reader function. If the
        // current token isn't a valid object state an exception will be thrown, and if it is a valid object state
        // the reader function will be called.
        return jsonReader.readObject(reader -> {
            // Since this class has no constructor reading to fields can be done inline.
            // If the class had a constructor with arguments the recommendation is using local variables to track
            // all field values.
            JsonSerializableExample result = new JsonSerializableExample();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                if ("anInt".equals(fieldName)) {
                    result.anInt = reader.getInt();
                } else if ("aBoolean".equals(fieldName)) {
                    result.aBoolean = reader.getBoolean();
                } else if ("aString".equals(fieldName)) {
                    result.aString = reader.getString();
                } else if ("aNullableDecimal".equals(fieldName)) {
                    // getNullable returns null if the current token is JsonToken.NULL, if the current token isn't
                    // JsonToken.NULL it passes the reader to the ReadValueCallback.
                    result.aNullableDecimal = reader.getNullable(JsonReader::getDouble);
                } else {
                    // Skip children when the field is unknown.
                    // If the current token isn't an array or object this is a no-op, otherwise is skips the entire
                    // sub-array/sub-object.
                    reader.skipChildren();
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

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcore%2Fazure-json%2FREADME.png)
