# AGENTS.md

This file provides guidance for AI agents (e.g., GitHub Copilot, Model Context Protocol servers, or other LLM-based assistants) interacting with the Azure SDK for Java repository.

## Repository Purpose

The Azure SDK for Java provides developers with libraries for accessing Azure services. The SDK is organized into client libraries, with each library corresponding to an Azure service. The repository follows a specific structure:

- **Client Libraries**: Located in `/sdk` directory, containing individual service clients
- **Data plane Libraries**: Libraries with Maven group `com.azure`
- **Management Libraries**: Libraries with Maven group `com.azure.resourcemanager`
- **Spring Libraries**: Libraries with Maven group `com.azure.spring`

## Repository Structure

```
/sdk/                    # Individual service client libraries
/eng/                    # Engineering system files and automation
/doc/                    # Documentation
/samples/                # Sample code and tutorials
/.github/                # GitHub-specific configuration
  /copilot-instructions.md  # Detailed Copilot-specific instructions
```

## Agent Capabilities

### Supported Actions

AI agents interacting with this repository can assist with:

1. **Code Generation and Modification**
   - Creating new client libraries following Azure SDK design guidelines
   - Adding features to existing libraries
   - Writing unit and integration tests
   - Refactoring code while maintaining backward compatibility

2. **Documentation**
   - Writing and updating README files
   - Creating JavaDoc comments for public APIs
   - Updating CHANGELOG files
   - Creating code samples and snippets

3. **Issue Triage and PR Review**
   - Analyzing and categorizing issues
   - Suggesting labels and assignees
   - Reviewing pull requests for adherence to guidelines
   - Identifying potential breaking changes

4. **Build and Test Automation**
   - Running Maven builds
   - Executing unit tests
   - Analyzing test failures
   - Checking code style with Checkstyle and SpotBugs

5. **SDK Release Support**
   - Checking package release readiness
   - Updating version numbers following semantic versioning
   - Verifying API review status

### Boundaries and Limitations

Agents should **NOT**:

- Disable or modify Checkstyle or SpotBugs rules to resolve linting issues
- Re-record tests as a fix to failing tests
- Make breaking changes to GA'd APIs without explicit approval
- Turn off security checks or introduce security vulnerabilities
- Suggest third-party alternatives to Azure SDK packages in official samples

## Key Workflows

### Building the SDK

```bash
# Build all modules
mvn clean install -DskipTests

# Build a specific module
mvn -f sdk/{service}/pom.xml clean install -DskipTests

# Run tests for a specific module
mvn -f sdk/{service}/pom.xml test
```

### Running Code Quality Checks

```bash
# Run Checkstyle
mvn checkstyle:check

# Run SpotBugs
mvn spotbugs:check

# Run both style checks
mvn checkstyle:check spotbugs:check
```

### Testing

```bash
# Run unit tests
mvn test

# Run live tests (requires Azure resources and environment variables)
mvn -Dmaven.wagon.http.pool=false --batch-mode --fail-at-end test

# See eng/common/TestResources/New-TestResources.ps1 for setting up test resources
```

### Versioning

- Version files are located in `/eng/versioning/`
- Follow semantic versioning: `major.minor.patch[-beta.N]`
- Use the `update_versions.py` script for version updates
- See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed versioning guidelines

### SDK Generation

Many client libraries are generated from OpenAPI specifications using AutoRest:

```bash
# Typical codegen command
autorest --version=3.9.7 --java --use=@autorest/java@4.1.59 \
  --input-file=<spec-file> --namespace=<namespace> --output-folder=<output>
```

## Design Guidelines

All contributions must follow the [Azure SDK Design Guidelines for Java](https://azure.github.io/azure-sdk/java_introduction.html).

### Core Principles

- **Idiomatic**: Use natural Java patterns (try-with-resources, Streams, Optional)
- **Consistent**: Follow consistent naming and design patterns across all libraries
- **Approachable**: Provide clear documentation and intuitive APIs
- **Diagnosable**: Include comprehensive logging and telemetry
- **Dependable**: Ensure reliability through robust error handling and retries

### API Design Requirements

- Service client classes with "Client" suffix
- Fluent builder patterns for client instantiation
- Synchronous and asynchronous clients (async suffixed with "AsyncClient")
- Options bags for additional parameters (e.g., `<MethodName>Options`)
- Standard verbs: create, upsert, set, get, list, delete
- Comprehensive JavaDoc for all public APIs

### Java Compatibility

- **Baseline**: Java 8
- **Testing**: Up to latest Java LTS (currently Java 21)
- **Best Practices**: Use modern Java features where appropriate

## CI/CD Pipeline

The repository uses Azure Pipelines for continuous integration:

- Each service has a `ci.yml` file defining its build pipeline
- PRs must pass all CI checks before merging
- Daily builds publish to the dev feed at [Azure Artifacts](https://dev.azure.com/azure-sdk/public/_packaging?_a=feed&feed=azure-sdk-for-java)

## Security and Compliance

- Never commit secrets or credentials
- Use Azure Identity for authentication in samples
- Follow secure coding practices
- Report security issues to <secure@microsoft.com>
- See [SECURITY.md](SECURITY.md) for more information

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed contribution guidelines, including:

- Pull request requirements
- Code review process
- Testing requirements
- Documentation standards
- Version management

## Agent-Specific Instructions

For detailed GitHub Copilot-specific instructions, including behavior guidelines, data sources, and SDK-specific patterns, see [.github/copilot-instructions.md](.github/copilot-instructions.md).

## Resources

### Documentation

- [Azure SDK for Java Documentation](https://aka.ms/java-docs)
- [Azure for Java Developers](https://docs.microsoft.com/java/azure/)
- [Azure SDK Design Guidelines](https://azure.github.io/azure-sdk/)
- [Java Guidelines](https://azure.github.io/azure-sdk/java_introduction.html)
- [Building the SDK](https://github.com/Azure/azure-sdk-for-java/wiki/Building)

### Repository Links

- [Latest Releases](https://azure.github.io/azure-sdk/releases/latest/java.html)
- [GitHub Issues](https://github.com/Azure/azure-sdk-for-java/issues)
- [Stack Overflow](https://stackoverflow.com/questions/tagged/azure-java-sdk)
- [Support](SUPPORT.md)

### External References

- [Maven](https://maven.apache.org/)
- [JUnit 5](https://junit.org/junit5/)
- [Semantic Versioning](https://semver.org/)
- [Azure Artifacts Dev Feed](https://dev.azure.com/azure-sdk/public/_packaging?_a=feed&feed=azure-sdk-for-java)

## Feedback and Questions

- For issues or feature requests, file a [GitHub Issue](https://github.com/Azure/azure-sdk-for-java/issues/new/choose)
- For questions, use [Stack Overflow with the `azure-java-sdk` tag](https://stackoverflow.com/questions/tagged/azure-java-sdk)
- For contributions, see [CONTRIBUTING.md](CONTRIBUTING.md)

---

This file aligns with the [AGENTS.md standard](https://agents.md) and provides a comprehensive guide for AI agents interacting with the Azure SDK for Java repository.
