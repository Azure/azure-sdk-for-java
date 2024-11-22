# @azure-tools/typespec-client-generator-core

TypeSpec Data Plane Generation library

## Install

```bash
npm install @azure-tools/typespec-client-generator-core
```

## Decorators

### Azure.ClientGenerator.Core

- [`@access`](#@access)
- [`@client`](#@client)
- [`@clientInitialization`](#@clientinitialization)
- [`@clientName`](#@clientname)
- [`@clientNamespace`](#@clientnamespace)
- [`@convenientAPI`](#@convenientapi)
- [`@flattenProperty`](#@flattenproperty)
- [`@operationGroup`](#@operationgroup)
- [`@override`](#@override)
- [`@paramAlias`](#@paramalias)
- [`@protocolAPI`](#@protocolapi)
- [`@usage`](#@usage)
- [`@useSystemTextJsonConverter`](#@usesystemtextjsonconverter)

#### `@access`

Override access for operations, models and enums.
When setting access for namespaces,
the access info will be propagated to the models and operations defined in the namespace.
If the model has an access override, the model override takes precedence.
When setting access for an operation,
it will influence the access info for models/enums that are used by this operation.
Models/enums that are used in any operations with `@access(Access.public)` will be set to access "public"
Models/enums that are only used in operations with `@access(Access.internal)` will be set to access "internal".
The access info for models will be propagated to models' properties,
parent models, discriminated sub models.
The override access should not be narrow than the access calculated by operation,
and different override access should not conflict with each other,
otherwise a warning will be added to diagnostics list.

```typespec
@Azure.ClientGenerator.Core.access(value: EnumMember, scope?: valueof string)
```

##### Target

`Model | Operation | Enum | Union | Namespace`

##### Parameters

| Name  | Type             | Description                                                                                                   |
| ----- | ---------------- | ------------------------------------------------------------------------------------------------------------- |
| value | `EnumMember`     | The access info you want to set for this model or operation.                                                  |
| scope | `valueof string` | The language scope you want this decorator to apply to. If not specified, will apply to all language emitters |

##### Examples

###### Set access

```typespec
// Access.internal
@access(Access.internal)
model ModelToHide {
  prop: string;
}
// Access.internal
@access(Access.internal)
op test: void;
```

###### Access propagation

```typespec
// Access.internal
@discriminator("kind")
model Fish {
  age: int32;
}

// Access.internal
@discriminator("sharktype")
model Shark extends Fish {
  kind: "shark";
  origin: Origin;
}

// Access.internal
model Salmon extends Fish {
  kind: "salmon";
}

// Access.internal
model SawShark extends Shark {
  sharktype: "saw";
}

// Access.internal
model Origin {
  country: string;
  city: string;
  manufacture: string;
}

// Access.internal
@get
@access(Access.internal)
op getModel(): Fish;
```

###### Access influence from operation

```typespec
// Access.internal
model Test1 {}

// Access.internal
@access(Access.internal)
@route("/func1")
op func1(@body body: Test1): void;

// Access.public
model Test2 {}

// Access.public
@route("/func2")
op func2(@body body: Test2): void;

// Access.public
model Test3 {}

// Access.public
@access(Access.public)
@route("/func3")
op func3(@body body: Test3): void;

// Access.public
model Test4 {}

// Access.internal
@access(Access.internal)
@route("/func4")
op func4(@body body: Test4): void;

// Access.public
@route("/func5")
op func5(@body body: Test4): void;

// Access.public
model Test5 {}

// Access.internal
@access(Access.internal)
@route("/func6")
op func6(@body body: Test5): void;

// Access.public
@route("/func7")
op func7(@body body: Test5): void;

// Access.public
@access(Access.public)
@route("/func8")
op func8(@body body: Test5): void;
```

#### `@client`

Create a ClientGenerator.Core client out of a namespace or interface

```typespec
@Azure.ClientGenerator.Core.client(value?: Model, scope?: valueof string)
```

##### Target

`Namespace | Interface`

##### Parameters

| Name  | Type             | Description                                                                                                   |
| ----- | ---------------- | ------------------------------------------------------------------------------------------------------------- |
| value | `Model`          | Optional configuration for the service.                                                                       |
| scope | `valueof string` | The language scope you want this decorator to apply to. If not specified, will apply to all language emitters |

##### Examples

###### Basic client setting

```typespec
@client
namespace MyService {

}
```

###### Setting with other service

```typespec
namespace MyService {

}

@client({
  service: MyService,
})
interface MyInterface {}
```

###### Changing client name if you don't want <Interface/Namespace>Client

```typespec
@client({
  client: MySpecialClient,
})
interface MyInterface {}
```

#### `@clientInitialization`

Client parameters you would like to add to the client. By default, we apply endpoint, credential, and api-version parameters. If you add clientInitialization, we will append those to the default list of parameters.

```typespec
@Azure.ClientGenerator.Core.clientInitialization(options: Model, scope?: valueof string)
```

##### Target

`Namespace | Interface`

##### Parameters

| Name    | Type             | Description                                                                                                   |
| ------- | ---------------- | ------------------------------------------------------------------------------------------------------------- |
| options | `Model`          |                                                                                                               |
| scope   | `valueof string` | The language scope you want this decorator to apply to. If not specified, will apply to all language emitters |

##### Examples

```typespec
// main.tsp
namespace MyService;

op upload(blobName: string): void;
op download(blobName: string): void;

// client.tsp
namespace MyCustomizations;
model MyServiceClientOptions {
  blobName: string;
}

@@clientInitialization(MyService, MyServiceClientOptions)
// The generated client will have `blobName` on it. We will also
// elevate the existing `blobName` parameter to the client level.
```

#### `@clientName`

Changes the name of a method, parameter, property, or model generated in the client SDK

```typespec
@Azure.ClientGenerator.Core.clientName(rename: valueof string, scope?: valueof string)
```

##### Target

`unknown`

##### Parameters

| Name   | Type             | Description                                                                                                   |
| ------ | ---------------- | ------------------------------------------------------------------------------------------------------------- |
| rename | `valueof string` | The rename you want applied to the object                                                                     |
| scope  | `valueof string` | The language scope you want this decorator to apply to. If not specified, will apply to all language emitters |

##### Examples

```typespec
@clientName("nameInClient")
op nameInService: void;
```

```typespec
@clientName("nameForJava", "java")
@clientName("name_for_python", "python")
@clientName("nameForCsharp", "csharp")
@clientName("nameForJavascript", "javascript")
op nameInService: void;
```

#### `@clientNamespace`

Changes the namespace of a client, model, enum or union generated in the client SDK.
By default, the client namespace for them will follow the TypeSpec namespace.

```typespec
@Azure.ClientGenerator.Core.clientNamespace(rename: valueof string, scope?: valueof string)
```

##### Target

`Namespace | Interface | Model | Enum | Union`

##### Parameters

| Name   | Type             | Description                                                                                                   |
| ------ | ---------------- | ------------------------------------------------------------------------------------------------------------- |
| rename | `valueof string` | The rename you want applied to the object                                                                     |
| scope  | `valueof string` | The language scope you want this decorator to apply to. If not specified, will apply to all language emitters |

##### Examples

```typespec
@clientNamespace("ContosoClient")
namespace Contoso;
```

```typespec
@clientName("ContosoJava", "java")
@clientName("ContosoPython", "python")
@clientName("ContosoCSharp", "csharp")
@clientName("ContosoJavascript", "javascript")
namespace Contoso;
```

#### `@convenientAPI`

Whether you want to generate an operation as a convenient operation.

```typespec
@Azure.ClientGenerator.Core.convenientAPI(value?: valueof boolean, scope?: valueof string)
```

##### Target

`Operation`

##### Parameters

| Name  | Type              | Description                                                                                                   |
| ----- | ----------------- | ------------------------------------------------------------------------------------------------------------- |
| value | `valueof boolean` | Whether to generate the operation as convenience method or not.                                               |
| scope | `valueof string`  | The language scope you want this decorator to apply to. If not specified, will apply to all language emitters |

##### Examples

```typespec
@convenientAPI(false)
op test: void;
```

#### `@flattenProperty`

_Deprecated: @flattenProperty decorator is not recommended to use._

Set whether a model property should be flattened or not.

```typespec
@Azure.ClientGenerator.Core.flattenProperty(scope?: valueof string)
```

##### Target

`ModelProperty`

##### Parameters

| Name  | Type             | Description                                                                                                   |
| ----- | ---------------- | ------------------------------------------------------------------------------------------------------------- |
| scope | `valueof string` | The language scope you want this decorator to apply to. If not specified, will apply to all language emitters |

##### Examples

```typespec
model Foo {
  @flattenProperty
  prop: Bar;
}
model Bar {}
```

#### `@operationGroup`

Create a ClientGenerator.Core operation group out of a namespace or interface

```typespec
@Azure.ClientGenerator.Core.operationGroup(scope?: valueof string)
```

##### Target

`Namespace | Interface`

##### Parameters

| Name  | Type             | Description                                                                                                   |
| ----- | ---------------- | ------------------------------------------------------------------------------------------------------------- |
| scope | `valueof string` | The language scope you want this decorator to apply to. If not specified, will apply to all language emitters |

##### Examples

```typespec
@operationGroup
interface MyInterface {}
```

#### `@override`

Override the default client method generated by TCGC from your service definition

```typespec
@Azure.ClientGenerator.Core.override(override: Operation, scope?: valueof string)
```

##### Target

: The original service definition
`Operation`

##### Parameters

| Name     | Type             | Description                                                                                                   |
| -------- | ---------------- | ------------------------------------------------------------------------------------------------------------- |
| override | `Operation`      | : The override method definition that specifies the exact client method you want                              |
| scope    | `valueof string` | The language scope you want this decorator to apply to. If not specified, will apply to all language emitters |

##### Examples

```typespec
// main.tsp
namespace MyService;

model Params {
 foo: string;
 bar: string;
}
op myOperation(...Params): void; // by default, we generate the method signature as `op myOperation(foo: string, bar: string)`;

// client.tsp
namespace MyCustomizations;

@override(MyService.operation)
op myOperationCustomization(params: Params): void;

// method signature is now `op myOperation(params: Params)`
```

```typespec
// main.tsp
namespace MyService;

model Params {
 foo: string;
 bar: string;
}
op myOperation(...Params): void; // by default, we generate the method signature as `op myOperation(foo: string, bar: string)`;

// client.tsp
namespace MyCustomizations;

@override(MyService.operation, "csharp")
op myOperationCustomization(params: Params): void;

// method signature is now `op myOperation(params: Params)` just for csharp
```

#### `@paramAlias`

Alias the name of a client parameter to a different name. This permits you to have a different name for the parameter in client initialization then on individual methods and still refer to the same parameter.

```typespec
@Azure.ClientGenerator.Core.paramAlias(paramAlias: valueof string, scope?: valueof string)
```

##### Target

`ModelProperty`

##### Parameters

| Name       | Type             | Description                                                                                                   |
| ---------- | ---------------- | ------------------------------------------------------------------------------------------------------------- |
| paramAlias | `valueof string` |                                                                                                               |
| scope      | `valueof string` | The language scope you want this decorator to apply to. If not specified, will apply to all language emitters |

##### Examples

```typespec
// main.tsp
namespace MyService;

op upload(blobName: string): void;

// client.tsp
namespace MyCustomizations;
model MyServiceClientOptions {
  blob: string;
}

@@clientInitialization(MyService, MyServiceClientOptions)
@@paramAlias(MyServiceClientOptions.blob, "blobName")

// The generated client will have `blobName` on it. We will also
// elevate the existing `blob` parameter to the client level.
```

#### `@protocolAPI`

Whether you want to generate an operation as a protocol operation.

```typespec
@Azure.ClientGenerator.Core.protocolAPI(value?: valueof boolean, scope?: valueof string)
```

##### Target

`Operation`

##### Parameters

| Name  | Type              | Description                                                                                                   |
| ----- | ----------------- | ------------------------------------------------------------------------------------------------------------- |
| value | `valueof boolean` | Whether to generate the operation as protocol or not.                                                         |
| scope | `valueof string`  | The language scope you want this decorator to apply to. If not specified, will apply to all language emitters |

##### Examples

```typespec
@protocolAPI(false)
op test: void;
```

#### `@usage`

Override usage for models/enums.
A model/enum's default usage info is always calculated by the operations that use it.
You could use this decorator to override the default usage info.
When setting usage for namespaces,
the usage info will be propagated to the models defined in the namespace.
If the model has an usage override, the model override takes precedence.
For example, with operation definition `op test(): OutputModel`,
the model `OutputModel` has default usage `Usage.output`.
After adding decorator `@@usage(OutputModel, Usage.input | Usage.output)`,
the final usage result for `OutputModel` is `Usage.input | Usage.output`.
The usage info for models will be propagated to models' properties,
parent models, discriminated sub models.
The override usage should not be narrow than the usage calculated by operation,
and different override usage should not conflict with each other,
otherwise a warning will be added to diagnostics list.

```typespec
@Azure.ClientGenerator.Core.usage(value: EnumMember | Union, scope?: valueof string)
```

##### Target

`Model | Enum | Union | Namespace`

##### Parameters

| Name  | Type                  | Description                                                                                                   |
| ----- | --------------------- | ------------------------------------------------------------------------------------------------------------- |
| value | `EnumMember \| Union` | The usage info you want to set for this model.                                                                |
| scope | `valueof string`      | The language scope you want this decorator to apply to. If not specified, will apply to all language emitters |

##### Examples

###### Expand usage for model

```typespec
op test(): OutputModel;

// usage result for `OutputModel` is `Usage.input | Usage.output`
@usage(Usage.input)
model OutputModel {
  prop: string;
}
```

###### Propagation of usage

```typespec
// Usage.output
@discriminator("kind")
model Fish {
  age: int32;
}

// Usage.input | Usage.output
@discriminator("sharktype")
@usage(Usage.input)
model Shark extends Fish {
  kind: "shark";
  origin: Origin;
}

// Usage.output
model Salmon extends Fish {
  kind: "salmon";
}

// Usage.output
model SawShark extends Shark {
  sharktype: "saw";
}

// Usage.output
model Origin {
  country: string;
  city: string;
  manufacture: string;
}

@get
op getModel(): Fish;
```

#### `@useSystemTextJsonConverter`

Whether a model needs the custom JSON converter, this is only used for backward compatibility for csharp.

```typespec
@Azure.ClientGenerator.Core.useSystemTextJsonConverter(scope?: valueof string)
```

##### Target

`Model`

##### Parameters

| Name  | Type             | Description                                                                                                   |
| ----- | ---------------- | ------------------------------------------------------------------------------------------------------------- |
| scope | `valueof string` | The language scope you want this decorator to apply to. If not specified, will apply to all language emitters |

##### Examples

```typespec
@useSystemTextJsonConverter
model MyModel {
  prop: string;
}
```
