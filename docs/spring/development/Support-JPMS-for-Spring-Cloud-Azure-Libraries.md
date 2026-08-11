- [What is JPMS](#what-is-jpms)
- [What is a module](#what-is-a-module)
- [How to create a Java module](#how-to-create-a-java-module)
  - [Add module descriptor](#add-module-descriptor)
  - [Automatic module](#automatic-module)
- [Useful tools](#useful-tools)
  - [Listing the JDK's modules](#listing-the-jdks-modules)
  - [Listing the modules for a specific module](#listing-the-modules-for-a-specific-module)
- [How to make a Spring Cloud Azure library a module](#how-to-make-a-spring-cloud-azure-library-a-module)
  - [For Spring Cloud Azure library](#for-spring-cloud-azure-library)
  - [For SDK level library](#for-sdk-level-library)
  - [How to test](#how-to-test)
- [References](#references)

## What is JPMS

JPMS is Java Platform Module System. 


Until Java 9, Java's top-level code organization element had been the package. Starting with Java 9 that changed: above the package now is the module. The module collects related packages together. 

The JPMS is a code-level structure, so it doesn't change the fact that we package Java into JAR files.

## What is a module

Modularity adds a higher level of aggregation above packages. The key new language element is the module - a uniquely named, reusable group of related packages, as well as resources (such as images and XML files) and a module descriptor specifying

- the module's name
- the module's dependencies (that is, other modules this module depends on)
- the package it explicitly makes available to other modules (all other packages in the modules are implicitly unavailable to other modules)
- the services it offers
- the services it consumes
- to what other modules it allows reflection

## How to create a Java module

### Add module descriptor
As mentioned, a module must provide a module descriptor. A mdodule descriptor is defined in a file named `module-info.java`.

One can refer to the `module-info.java` of [azure-cosmos](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-cosmos/src/main/java/module-info.java#L5-L76) for example.

Here are some commonly used module directives:

- **requires**: A requires module directive specifies that this module depends on another module—this relationship is called a module dependency. Each module must explicitly state its dependencies. When module A requires module B, module A is said to read module B and module B is read by module A. To specify a dependency on another module, use requires

- **requires transitive**: To specify a dependency on another module and to ensure that other modules reading your module also read that dependency—known as implied readability—use requires transitive

- **requires static**: represents the concept of optional dependence such a module is mandatory at compilation and optional at runtime.

- **exports and exports…to**: An exports module directive specifies one of the module’s packages whose public types (and their nested public and protected types) should be accessible to code in all other modules. An exports…to directive enables you to specify in a comma-separated list precisely which module’s or modules’ code can access the exported package—this is known as a qualified export. 

More syntax about module-info.java can see https://www.oracle.com/corporate/features/understanding-java-9-modules.html. 

### Automatic module
If a project, which is modular itself but has a dependency which is not a module yet, how will the JPMS handle that?

It automatically creates a module:
- Name: 
  + If the JAR defines the `Automatic-Module-Name` header in the manifest file, it will be the module's name. 
  + Otherwise the JAR name is the module's name.

- Requires:
  + An automatic module read all other modules.
- Exports/Opens
  + An automatic module exports all packages and also opens them for deep reflection.


## Useful tools

### Listing the JDK's modules

To list the JDK's set of modules, which includes the standard modules that implement the Java Language SE Specification (names starting with java), JavaFX modules (names starting with JavaFX), JDK-specific modules (names starting with jdk). 

A version string follows each module name - `@9` indicates that the module belongs to Java 9. 

```
java --list-modules

```

### Listing the modules for a specific module

This command can be used to verify whether a module is valid. 
```
java -p ~/.m2/repository/com/azure/azure-core/1.32.0/azure-core-1.32.0.jar --list-modules
``` 

## How to make a Spring Cloud Azure library a module


### For Spring Cloud Azure library
Our current plan is the same as the Spring's team, they don't support full JPMS yet. https://github.com/spring-projects/spring-framework/issues/18079. 

So we only need to provide `Automatic Module Name` for a Spring Cloud Azure library.

Check this for example https://github.com/Azure/azure-sdk-for-java/blob/d96d0333112997b0ee5aa03aacfad2d0b0e7d1d4/sdk/spring/spring-cloud-azure-starter/pom.xml#L106-L115. 

### For SDK level library

But if you are developing a SDK level library, we should add the `module-info.java` module descriptor for it. For example `azure-identity-providers-*` libraries. 

Check this for example https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/src/main/java/module-info.java. 


### How to test
1. Create a new application with java version greater than 9, for example Java 11.
2. Add a `module-info.java` file for this application
3. Add the module name you want to test in the file, such as `requires com.azure.identity.providers.mysql` 
4. Then run the application, if successful proves OK or you need to check again.

BTW, if you do not use codes from the tested module, this test method won't work.
 

## References
- https://www.oracle.com/corporate/features/understanding-java-9-modules.html
- https://stackoverflow.com/questions/46741907/what-is-an-automatic-module#:~:text=The%20module%20name%20of%20an,JAR%20file%20by%20the%20ModuleFinder%20.
- https://github.com/tfesenko/Java-Modules-JPMS-CheatSheet/blob/master/README.md
