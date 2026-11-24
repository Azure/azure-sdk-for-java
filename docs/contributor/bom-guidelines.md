# BOM Guidelines

---

## What Is the Azure SDK BOM?

A **Bill of Materials (BOM)** (`azure-sdk-bom`) is a customer-facing POM that declares a curated set of
Azure SDK libraries that are known to have compatible dependency trees. Consumers import the BOM to avoid
managing individual version numbers.

The BOM is located at: `sdk/boms/azure-sdk-bom/`

---

## Using the BOM (Consumer Instructions)

Add the BOM to your `<dependencyManagement>` section:

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.azure</groupId>
      <artifactId>azure-sdk-bom</artifactId>
      <version>1.2.x</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

Then declare individual libraries without specifying versions:

```xml
<dependencies>
  <dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-storage-blob</artifactId>
  </dependency>
  <dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
  </dependency>
</dependencies>
```

---

## Release Cadence

The BOM release cadence is independent of individual library releases.
A BOM release is triggered when one or more contained libraries release a new version.

---

## BOM Versioning

Changes to the BOM version are driven by the severity of changes in contained libraries:

| Change in contained library | BOM version bump |
|-----------------------------|-----------------|
| Major version release | Minor bump (e.g. `1.0.x` → `1.1.0`) |
| Minor or patch release | Patch bump (e.g. `1.0.0` → `1.0.1`) |

---

## Validation

Before a BOM release candidate can ship it must pass the **dependency checker**:

| Result | Colour | Meaning |
|--------|--------|---------|
| Success | Green | All contained libraries share the same version of every common dependency |
| Warning | Orange | Common dependency exists but is not at its latest released version |
| Error | Red | Two or more libraries conflict on the version of a shared dependency |

**A BOM release candidate may not contain any red (Error) entries.**

Run the dependency check locally during the `package` phase:

```bash
mvn package -f sdk/boms/azure-sdk-bom/pom.xml -DskipTests
```

---

## Adding a Library to the BOM

1. Verify the library is GA (not beta-only).
2. Add a `<dependency>` entry in `sdk/boms/azure-sdk-bom/pom.xml` under `<dependencyManagement>`.
3. Run the dependency checker and resolve all errors before opening a PR.
4. Follow the standard version-update tag convention (see [Versioning](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/versioning.md)).

---

## See Also

- [Versioning](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/versioning.md)
- [Release Checklist](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/release-checklist.md)
- [azure-sdk-bom source](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/boms/)
