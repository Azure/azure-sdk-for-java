using TypeSpec.Reflection;

namespace Azure.ClientGenerator.Core;

/**
 * Changes the name of a method, parameter, property, or model generated in the client SDK
 * @param rename The rename you want applied to the object
 * @param scope The language scope you want this decorator to apply to. If not specified, will apply to all language emitters
 *
 * @example
 * ```typespec
 * @clientName("nameInClient")
 * op nameInService: void;
 * ```
 *
 * @example
 * ```typespec
 * @clientName("nameForJava", "java")
 * @clientName("name_for_python", "python")
 * @clientName("nameForCsharp", "csharp")
 * @clientName("nameForJavascript", "javascript")
 * op nameInService: void;
 * ```
 */
extern dec clientName(target: unknown, rename: valueof string, scope?: valueof string);

/**
 * Whether you want to generate an operation as a convenient operation.
 * @param value Whether to generate the operation as convenience method or not.
 * @param scope The language scope you want this decorator to apply to. If not specified, will apply to all language emitters
 *
 * @example
 * ```typespec
 * @convenientAPI(false)
 * op test: void;
 * ```
 */
extern dec convenientAPI(target: Operation, value?: valueof boolean, scope?: valueof string);

/**
 * Whether you want to generate an operation as a protocol operation.
 * @param value Whether to generate the operation as protocol or not.
 * @param scope The language scope you want this decorator to apply to. If not specified, will apply to all language emitters
 *
 * @example
 * ```typespec
 * @protocolAPI(false)
 * op test: void;
 * ```
 */
extern dec protocolAPI(target: Operation, value?: valueof boolean, scope?: valueof string);

/**
 * Create a ClientGenerator.Core client out of a namespace or interface
 * @param value Optional configuration for the service.
 * @param scope The language scope you want this decorator to apply to. If not specified, will apply to all language emitters
 *
 * @example Basic client setting
 * ```typespec
 * @client
 * namespace MyService {}
 * ```
 *
 * @example Setting with other service
 * ```typespec
 * namespace MyService {}
 *
 * @client({service: MyService})
 * interface MyInterface {}
 * ```
 *
 * @example Changing client name if you don't want <Interface/Namespace>Client
 * ```typespec
 * @client({client: MySpecialClient})
 * interface MyInterface {}
 * ```
 *
 * @example
 */
extern dec client(target: Namespace | Interface, value?: Model, scope?: valueof string);

/**
 * Create a ClientGenerator.Core operation group out of a namespace or interface
 * @param scope The language scope you want this decorator to apply to. If not specified, will apply to all language emitters
 *
 * @example
 * ```typespec
 * @operationGroup
 * interface MyInterface{}
 * ```
 */
extern dec operationGroup(target: Namespace | Interface, scope?: valueof string);

/**
 * DEPRECATED: Use `@usage` and `@access` decorator instead.
 *
 * Whether to exclude a model from generation for specific languages. By default we generate
 * all models that are included in operations.
 * @param scope The language scope you want this decorator to apply to. If not specified, will apply to all language emitters
 *
 * @example
 * ```typespec
 * @exclude("python")
 * model ModelToExclude {
 *   prop: string;
 * }
 * ```
 */
#deprecated "@exclude decorator is deprecated. Use `@usage` and `@access` decorator instead."
extern dec exclude(target: Model, scope?: valueof string);

/**
 * DEPRECATED: Use `@usage` and `@access` decorator instead.
 *
 * Whether to include a model in generation for specific languages. By default we generate
 * all models that are included in operations.
 * @param scope The language scope you want this decorator to apply to. If not specified, will apply to all language emitters
 *
 * @example
 * ```typespec
 * @include("python")
 * model ModelToInclude {
 *   prop: string;
 * }
 * ```
 */
#deprecated "@include decorator is deprecated. Use `@usage` and `@access` decorator instead."
extern dec include(target: Model, scope?: valueof string);

alias ClientFormat = "unixtime" | "iso8601" | "rfc1123" | "seconds";

/**
 * DEPRECATED: Use `@encode` decorator in `@typespec/compiler` instead.
 *
 * Can be used to explain the client type that the current TYPESPEC
 * type should map to.
 * @param value The client format to apply.
 * @example
 * ```typespec
 * model MyModel {
 *   @clientFormat("unixtime")
 *   created_at?: int64
 * }
 * ```
 */
#deprecated "@clientFormat decorator is deprecated. Use `@encode` decorator in `@typespec/compiler` instead."
extern dec clientFormat(target: ModelProperty, value: valueof ClientFormat);

/**
 * DEPRECATED: Use `@access` decorator instead.
 *
 * Whether to mark an operation as internal for specific languages,
 * meaning it should not be exposed to end SDK users
 * @param scope The language scope you want this decorator to apply to. If not specified, will apply to all language emitters
 *
 * @example
 * ```typespec
 * @internal("python")
 * op test: void;
 * ```
 */
#deprecated "@internal decorator is deprecated. Use `@access` decorator instead."
extern dec internal(target: Operation, scope?: valueof string);

/**
 * Usage value.
 */
enum Usage {
  /**
   * Used in request
   */
  input: 2,

  /**
   * Used in response
   */
  output: 4,
}

/**
 * Expand usage for models/enums.
 * A model/enum's default usage info is always calculated by the operations that use it.
 * You could use this decorator to expand the default usage info.
 * For example, with operation definition `op test(): OutputModel`,
 * the model `OutputModel` has default usage `Usage.output`.
 * After adding decorator `@@usage(OutputModel, Usage.input)`,
 * the final usage result for `OutputModel` is `Usage.input | Usage.output`.
 * The calculation of default usage info for models will be propagated to models' properties,
 * parent models, discriminated sub models.
 * But the expanded usage from `@usage` decorator will not be propagated.
 * If you want to do any customization for the usage of a model,
 * you need to take care of all related models/enums.
 * @param value The usage info you want to set for this model.
 * @param scope The language scope you want this decorator to apply to. If not specified, will apply to all language emitters
 *
 * @example Expand usage for model
 * ```typespec
 * op test(): OutputModel;
 *
 * // usage result for `OutputModel` is `Usage.input | Usage.output`
 * @usage(Usage.input)
 * model OutputModel {
 *   prop: string
 * }
 * ```
 *
 * @example Propagation of usage
 * ```typespec
 * // Usage.output
 * @discriminator("kind")
 * model Fish {
 *   age: int32;
 * }
 *
 * // Usage.input | Usage.output
 * @discriminator("sharktype")
 * @usage(Usage.input)
 * model Shark extends Fish {
 *   kind: "shark";
 *   origin: Origin;
 * }
 *
 * // Usage.output
 * model Salmon extends Fish {
 *   kind: "salmon";
 * }
 *
 * // Usage.output
 * model SawShark extends Shark {
 *   sharktype: "saw";
 * }
 *
 * // Usage.output
 * model Origin {
 *   country: string;
 *   city: string;
 *   manufacture: string;
 * }
 *
 * @get
 * op getModel(): Fish;
 * ```
 */
extern dec usage(target: Model | Enum | Union, value: EnumMember | Union, scope?: valueof string);

/**
 * Access value.
 */
enum Access {
  /**
   * Open to user
   */
  public: "public",

  /**
   * Hide from user
   */
  internal: "internal",
}

/**
 * Set explicit access for operations, models and enums.
 * When setting access for models,
 * the access info wll not be propagated to models' properties, base models or sub models.
 * When setting access for an operation,
 * it will influence the access info for models/enums that are used by this operation.
 * Models/enums that are used in any operations with `@access(Access.public)` will be implicitly set to access "public"
 * Models/enums that are only used in operations with `@access(Access.internal)` will be implicitly set to access "internal".
 * This influence will be propagated to models' properties, parent models, discriminated sub models.
 * But this influence will be override by `@usage` decorator on models/enums directly.
 * If an operation/model/enum has no `@access` decorator and is not influenced by any operation with `@access` decorator,
 * the access result is undefined.
 * @param value The access info you want to set for this model or operation.
 * @param scope The language scope you want this decorator to apply to. If not specified, will apply to all language emitters
 *
 * @example Set access
 * ```typespec
 * // Access.internal
 * @access(Access.internal)
 * model ModelToHide {
 *   prop: string;
 * }
 * // Access.internal
 * @access(Access.internal)
 * op test: void;
 * ```
 * @example Access propagation
 * ```typespec
 * // Access.internal
 * @discriminator("kind")
 * model Fish {
 *   age: int32;
 * }
 *
 * // Access.internal
 * @discriminator("sharktype")
 * model Shark extends Fish {
 *   kind: "shark";
 *   origin: Origin;
 * }
 *
 * // Access.internal
 * model Salmon extends Fish {
 *   kind: "salmon";
 * }
 *
 * // Access.internal
 * model SawShark extends Shark {
 *   sharktype: "saw";
 * }
 *
 * // Access.internal
 * model Origin {
 *   country: string;
 *   city: string;
 *   manufacture: string;
 * }
 *
 * // Access.internal
 * @get
 * @access(Access.internal)
 * op getModel(): Fish;
 * ```
 * @example Access influence from operation
 * ```typespec
 * // Access.internal
 * model Test1 {
 * }
 *
 * // Access.internal
 * @access(Access.internal)
 * @route("/func1")
 * op func1(
 *   @body body: Test1
 * ): void;
 *
 * // undefined
 * model Test2 {
 * }
 *
 * // undefined
 * @route("/func2")
 * op func2(
 *   @body body: Test2
 * ): void;
 *
 * // Access.public
 * model Test3 {
 * }
 *
 * // Access.public
 * @access(Access.public)
 * @route("/func3")
 * op func3(
 *   @body body: Test3
 * ): void;
 *
 * // undefined
 * model Test4 {
 * }
 *
 * // Access.internal
 * @access(Access.internal)
 * @route("/func4")
 * op func4(
 *   @body body: Test4
 * ): void;
 *
 * // undefined
 * @route("/func5")
 * op func5(
 *   @body body: Test4
 * ): void;
 *
 * // Access.public
 * model Test5 {
 * }
 *
 * // Access.internal
 * @access(Access.internal)
 * @route("/func6")
 * op func6(
 *   @body body: Test5
 * ): void;
 *
 * // undefined
 * @route("/func7")
 * op func7(
 *   @body body: Test5
 * ): void;
 *
 * // Access.public
 * @access(Access.public)
 * @route("/func8")
 * op func8(
 *   @body body: Test5
 * ): void;
 * ```
 */
extern dec access(
  target: Model | Operation | Enum | Union,
  value: EnumMember,
  scope?: valueof string
);

/**
 * Set whether a model property should be flattened or not.
 * @param scope The language scope you want this decorator to apply to. If not specified, will apply to all language emitters
 *
 * @example
 * ```typespec
 * model Foo {
 *    @flattenProperty
 *    prop: Bar;
 * }
 * model Bar {
 * }
 * ```
 */
#deprecated "@flattenProperty decorator is not recommended to use."
extern dec flattenProperty(target: ModelProperty, scope?: valueof string);

/**
 * Override the default client method generated by TCGC from your service definition
 * @param original: The original service definition
 * @param override: The override method definition that specifies the exact client method you want
 * @param scope The language scope you want this decorator to apply to. If not specified, will apply to all language emitters
 *
 * @example
 * ```typespec
 * // main.tsp
 * namespace MyService;
 *
 * model Params {
 *  foo: string;
 *  bar: string;
 * }
 * op myOperation(...Params): void; // by default, we generate the method signature as `op myOperation(foo: string, bar: string)`;
 *
 * // client.tsp
 * namespace MyCustomizations;
 *
 * @override(MyService.operation)
 * op myOperationCustomization(params: Params): void;
 *
 * // method signature is now `op myOperation(params: Params)`
 * ```
 *
 * @example
 * ```typespec
 * // main.tsp
 * namespace MyService;
 *
 * model Params {
 *  foo: string;
 *  bar: string;
 * }
 * op myOperation(...Params): void; // by default, we generate the method signature as `op myOperation(foo: string, bar: string)`;
 *
 * // client.tsp
 * namespace MyCustomizations;
 *
 * @override(MyService.operation, "csharp")
 * op myOperationCustomization(params: Params): void;
 *
 * // method signature is now `op myOperation(params: Params)` just for csharp
 * ```
 */
extern dec override(original: Operation, override: Operation, scope?: valueof string);
