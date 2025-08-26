# Sample Validation in Azure SDK for Java CI Pipelines

This document explains how code samples are validated in the Azure SDK for Java CI pipelines.

## Overview

The Azure SDK for Java employs a multi-layered approach to validate samples in CI pipelines, ensuring they are correct, compilable, and follow proper metadata standards.

## Validation Mechanisms

### 1. Sample Metadata Validation

**Location**: `eng/common/pipelines/templates/steps/verify-samples.yml`

**Process**:
- Scans all markdown files (*.md) in the service directories
- Executes `eng/common/scripts/Test-SampleMetadata.ps1` script
- Validates frontmatter metadata against approved product slugs

**What it checks**:
- Product slugs in markdown frontmatter must be valid according to Microsoft's taxonomy
- Validates against the comprehensive list in `Test-SampleMetadata.ps1` (450+ approved slugs)
- Supports parent product slugs when `-AllowParentProducts` flag is used

**Execution in CI**:
```yaml
# Called from CI job template
- template: /eng/common/pipelines/templates/steps/verify-samples.yml
  parameters:
    ServiceDirectories: $(PRServiceDirectories)
```

### 2. Sample Compilation Validation

**Location**: Maven build configuration in parent POMs

**Process**:
- Samples are located in `src/samples/java` directories within each SDK module
- During the `generate-test-sources` phase, the `build-helper-maven-plugin` adds sample sources to the test classpath
- Samples are compiled during the `test-compile` phase along with regular tests

**Configuration** (from `sdk/parents/azure-sdk-parent/pom.xml`):
```xml
<plugin>
  <groupId>org.codehaus.mojo</groupId>
  <artifactId>build-helper-maven-plugin</artifactId>
  <executions>
    <execution>
      <id>compile-samples-source</id>
      <phase>generate-test-sources</phase>
      <goals>
        <goal>add-test-source</goal>
      </goals>
      <configuration>
        <sources>
          <source>src/samples/java</source>
        </sources>
      </configuration>
    </execution>
  </executions>
</plugin>
```

**What it validates**:
- Ensures all sample Java code compiles successfully
- Validates that samples have correct imports and dependencies
- Checks that samples follow Java syntax and can be built
- Sample classes are compiled into `target/test-classes/` directory

### 3. Code Quality Validation

**Linting and Analysis**:
- **Checkstyle**: Sample code is included in Checkstyle analysis by default (`checkstyle.includeTestSourceDirectory=true`)
- **SpotBugs**: Sample analysis is controlled by `spotbugs.includeTests` property (default: false)
- **Javadoc**: Samples are excluded from Javadoc generation (package `*.samples` is excluded)

**Configuration details**:
```xml
<!-- Samples are included in Checkstyle by default -->
<checkstyle.includeTestSourceDirectory>true</checkstyle.includeTestSourceDirectory>

<!-- SpotBugs analysis on samples can be enabled -->
<spotbugs.includeTests>false</spotbugs.includeTests>

<!-- Samples excluded from Javadoc -->
<excludePackageNames>*.samples:</excludePackageNames>
```

## CI Pipeline Flow

### 1. Build Stage
1. **Project List Generation**: Determines which modules have changed
2. **Sample Metadata Validation**: Runs `verify-samples.yml` step
3. **Build and Package**: Compiles main code with `mvn deploy`
4. **Test Compilation**: Compiles tests and samples with `test-compile` goal

### 2. Test Stage
1. **Test Execution**: Runs unit tests (samples are compiled but not executed as tests)
2. **Code Coverage**: Generates coverage reports
3. **Linting**: Runs Checkstyle on all code including samples

## Sample Structure

### Typical Sample Directory Structure
```
sdk/[service]/[module]/
├── src/
│   ├── main/java/          # Main SDK code
│   ├── test/java/          # Unit tests
│   └── samples/
│       ├── README.md       # Sample documentation
│       └── java/           # Sample Java code
│           └── com/azure/[service]/
│               ├── SampleClass1.java
│               └── SampleClass2.java
```

### Sample Code Requirements
- Must be compilable Java code
- Should demonstrate real SDK usage patterns
- Must have proper imports and dependencies
- Follow the same code quality standards as main SDK code

## Key Files and Locations

| Component | Location | Purpose |
|-----------|----------|---------|
| Sample Metadata Validation | `eng/common/pipelines/templates/steps/verify-samples.yml` | Validates sample markdown metadata |
| Metadata Validation Script | `eng/common/scripts/Test-SampleMetadata.ps1` | PowerShell script that checks product slugs |
| Build Configuration | `sdk/parents/azure-sdk-parent/pom.xml` | Maven configuration for sample compilation |
| CI Job Template | `eng/pipelines/templates/jobs/ci.yml` | Main CI job that orchestrates validation |
| Service CI Files | `sdk/[service]/ci.yml` | Service-specific CI configuration |

## Testing Sample Validation Locally

### To validate sample metadata:
```bash
# From repository root
Get-ChildItem sdk/[service] -Filter *.md -Recurse | eng/common/scripts/Test-SampleMetadata.ps1 -AllowParentProducts
```

### To compile samples:
```bash
# From a service module directory
mvn test-compile
# Check that sample classes are generated in target/test-classes/
```

### To run full validation:
```bash
# From repository root
mvn clean install -pl sdk/[service]/[module] -am
```

## Common Issues and Solutions

### Sample Compilation Failures
- **Issue**: Sample doesn't compile
- **Solution**: Check imports, dependencies, and Java syntax
- **Verification**: Run `mvn test-compile` in the module directory

### Metadata Validation Failures
- **Issue**: Invalid product slug in markdown frontmatter
- **Solution**: Use only approved slugs from the taxonomy list
- **Verification**: Run the PowerShell script locally

### Missing Dependencies
- **Issue**: Sample references classes not available in test classpath
- **Solution**: Ensure all required dependencies are declared in the module's pom.xml

## References

- [Azure SDK Design Guidelines](https://azure.github.io/azure-sdk/java_introduction.html)
- [Microsoft Taxonomy](https://taxonomy.learn.microsoft.com/TaxonomyServiceAdminPage/#/taxonomy/)
- [Contributing Guide](../CONTRIBUTING.md)