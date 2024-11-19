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
- [`@clientFormat`](#@clientformat)
- [`@clientName`](#@clientname)
- [`@convenientAPI`](#@convenientapi)
- [`@exclude`](#@exclude)
- [`@flattenProperty`](#@flattenproperty)
- [`@include`](#@include)
- [`@internal`](#@internal)
- [`@operationGroup`](#@operationgroup)
- [`@override`](#@override)
- [`@protocolAPI`](#@protocolapi)
- [`@usage`](#@usage)

#### `@access`

Set explicit access for operations, models and enums.
When setting access for models,
the access info wll not be propagated to models' properties, base models or sub models.
When setting access for an operation,
it will influence the access info for models/enums that are used by this operation.
Models/enums that are used in any operations with `@access(Access.public)` will be implicitly set to access "public"
Models/enums that are only used in operations with `@access(Access.internal)` will be implicitly set to access "internal".
This influence will be propagated to models' properties, parent models, discriminated sub models.
But this influence will be override by `@usage` decorator on models/enums directly.
If an operation/model/enum has no `@access` decorator and is not influenced by any operation with `@access` decorator,
the access result is undefined.

```typespec
@Azure.ClientGenerator.Core.access(value: EnumMember, scope?: valueof string)
```

##### Target

`Model | Operation | Enum | Union`

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

// undefined
model Test2 {}

// undefined
@route("/func2")
op func2(@body body: Test2): void;

// Access.public
model Test3 {}

// Access.public
@access(Access.public)
@route("/func3")
op func3(@body body: Test3): void;

// undefined
model Test4 {}

// Access.internal
@access(Access.internal)
@route("/func4")
op func4(@body body: Test4): void;

// undefined
@route("/func5")
op func5(@body body: Test4): void;

// Access.public
model Test5 {}

// Access.internal
@access(Access.internal)
@route("/func6")
op func6(@body body: Test5): void;

// undefined
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

#### `@clientFormat`

_Deprecated: @clientFormat decorator is deprecated. Use `@encode` decorator in `@typespec/compiler` instead._

DEPRECATED: Use `@encode` decorator in `@typespec/compiler` instead.

Can be used to explain the client type that the current TYPESPEC
type should map to.

```typespec
@Azure.ClientGenerator.Core.clientFormat(value: valueof "unixtime" | "iso8601" | "rfc1123" | "seconds")
```

##### Target

`ModelProperty`

##### Parameters

| Name  | Type                                                        | Description                 |
| ----- | ----------------------------------------------------------- | --------------------------- |
| value | `valueof "unixtime" \| "iso8601" \| "rfc1123" \| "seconds"` | The client format to apply. |

##### Examples

```typespec
model MyModel {
  @clientFormat("unixtime")
  created_at?: int64;
}
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

#### `@exclude`

_Deprecated: @exclude decorator is deprecated. Use `@usage` and `@access` decorator instead._

DEPRECATED: Use `@usage` and `@access` decorator instead.

Whether to exclude a model from generation for specific languages. By default we generate
all models that are included in operations.

```typespec
@Azure.ClientGenerator.Core.exclude(scope?: valueof string)
```

##### Target

`Model`

##### Parameters

| Name  | Type             | Description                                                                                                   |
| ----- | ---------------- | ------------------------------------------------------------------------------------------------------------- |
| scope | `valueof string` | The language scope you want this decorator to apply to. If not specified, will apply to all language emitters |

##### Examples

```typespec
@exclude("python")
model ModelToExclude {
  prop: string;
}
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

#### `@include`

_Deprecated: @include decorator is deprecated. Use `@usage` and `@access` decorator instead._

DEPRECATED: Use `@usage` and `@access` decorator instead.

Whether to include a model in generation for specific languages. By default we generate
all models that are included in operations.

```typespec
@Azure.ClientGenerator.Core.include(scope?: valueof string)
```

##### Target

`Model`

##### Parameters

| Name  | Type             | Description                                                                                                   |
| ----- | ---------------- | ------------------------------------------------------------------------------------------------------------- |
| scope | `valueof string` | The language scope you want this decorator to apply to. If not specified, will apply to all language emitters |

##### Examples

```typespec
@include("python")
model ModelToInclude {
  prop: string;
}
```

#### `@internal`

_Deprecated: @internal decorator is deprecated. Use `@access` decorator instead._

DEPRECATED: Use `@access` decorator instead.

Whether to mark an operation as internal for specific languages,
meaning it should not be exposed to end SDK users

```typespec
@Azure.ClientGenerator.Core.internal(scope?: valueof string)
```

##### Target

`Operation`

##### Parameters

| Name  | Type             | Description                                                                                                   |
| ----- | ---------------- | ------------------------------------------------------------------------------------------------------------- |
| scope | `valueof string` | The language scope you want this decorator to apply to. If not specified, will apply to all language emitters |

##### Examples

```typespec
@internal("python")
op test: void;
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

Expand usage for models/enums.
A model/enum's default usage info is always calculated by the operations that use it.
You could use this decorator to expand the default usage info.
For example, with operation definition `op test(): OutputModel`,
the model `OutputModel` has default usage `Usage.output`.
After adding decorator `@@usage(OutputModel, Usage.input)`,
the final usage result for `OutputModel` is `Usage.input | Usage.output`.
The calculation of default usage info for models will be propagated to models' properties,
parent models, discriminated sub models.
But the expanded usage from `@usage` decorator will not be propagated.
If you want to do any customization for the usage of a model,
you need to take care of all related models/enums.

```typespec
@Azure.ClientGenerator.Core.usage(value: EnumMember | Union, scope?: valueof string)
```

##### Target

`Model | Enum | Union`

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
