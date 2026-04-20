# Getting Started – Contributor Environment Setup

> **See also**: [CONTRIBUTING.md](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/CONTRIBUTING.md)

---

## Prerequisites Checklist

- [ ] JDK installed (21 recommended, 8 minimum)
- [ ] Maven installed and on `PATH`
- [ ] Git with long-path support enabled (Windows)
- [ ] IDE configured (IntelliJ IDEA recommended)
- [ ] Pre-commit hook installed

---

## 1. Java

The libraries are built and tested against **JDK 8, 11, 17, and 21**.
Install the latest LTS release (JDK 21) for local development.

**Download links:**
- [OpenJDK 8 (Temurin)](https://adoptium.net/temurin/releases/?package=jdk&version=8)
- [OpenJDK 11 (Temurin)](https://adoptium.net/temurin/releases/?package=jdk&version=11)
- [OpenJDK 17 (Temurin)](https://adoptium.net/temurin/releases/?package=jdk&version=17)
- [OpenJDK 21 (Temurin)](https://adoptium.net/temurin/releases/?package=jdk&version=21)

**After installation:**

```bash
# Set JAVA_HOME (example – adjust path for your OS)
export JAVA_HOME=/path/to/jdk
export PATH=$JAVA_HOME/bin:$PATH

# Verify
java -version
```

---

## 2. Maven

1. [Download Maven](https://maven.apache.org/download.cgi) and unzip to a folder.
2. Set environment variables:
   ```
   MAVEN_HOME = <unzip location>
   PATH       = %MAVEN_HOME%\bin (Windows) or $MAVEN_HOME/bin (Unix)
   ```
3. Verify: `mvn -version`

**Windows Dev Drive tip:** If using Windows Dev Drive, point Maven's local cache there for faster builds:
```
MAVEN_OPTS=-Dmaven.repo.local=D:\maven
```

### Maven Artifact Feed Authentication

This repo routes Maven through an Azure Artifacts feed.

**External contributors** — if you hit `401 Unauthorized`, either:
- Submit the PR and let CI resolve it, or
- Add the following to `~/.m2/settings.xml` to re-enable Maven Central:

```xml
<settings>
  <profiles>
    <profile>
      <id>external-contributor</id>
      <repositories>
        <repository>
          <id>central</id>
          <url>https://repo.maven.apache.org/maven2</url>
          <releases><enabled>true</enabled></releases>
          <snapshots><enabled>false</enabled></snapshots>
        </repository>
      </repositories>
    </profile>
  </profiles>
  <activeProfiles>
    <activeProfile>external-contributor</activeProfile>
  </activeProfiles>
</settings>
```

**Internal contributors (Microsoft)** — set up the Maven Credential Provider:

```bash
# Run from outside the azure-sdk-for-java directory
mvn dependency:get \
  "-Dartifact=com.microsoft.azure:artifacts-maven-credprovider:3.1" \
  "-DremoteRepositories=central::::https://pkgs.dev.azure.com/artifacts-public/PublicTools/_packaging/AzureArtifacts/maven/v1"
```

Then add to `.mvn/extensions.xml`:
```xml
<extensions xmlns="http://maven.apache.org/EXTENSIONS/1.1.0"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://maven.apache.org/EXTENSIONS/1.1.0 https://maven.apache.org/xsd/core-extensions-1.0.0.xsd">
  <extension>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>artifacts-maven-credprovider</artifactId>
    <version>3.1</version>
  </extension>
</extensions>
```

Troubleshooting 401 errors:
1. Ensure [access to Azure SDK Partners](https://aka.ms/azsdk/access) has been granted.
2. Verify Azure CLI login: `az account show`
3. Re-authenticate: `az login`

---

## 3. Git – Long Path Support (Windows)

Some filenames in the repo exceed the Windows 260-character limit.

```
# Registry (run as Administrator)
REG ADD HKLM\SYSTEM\CurrentControlSet\Control\FileSystem /v LongPathsEnabled /t REG_DWORD /d 1

# Git global setting
git config --global core.longpaths true
```

---

## 4. IDE

The preferred IDE is **[IntelliJ IDEA](https://www.jetbrains.com/idea/)** — the free Community edition is sufficient.

When opening the project, import the root `pom.xml` as a Maven project.

---

## 5. Pre-commit Hook

Copy the pre-commit hook before making your first commit:

```bash
cp eng/scripts/pre-commit .git/hooks/pre-commit
```

This runs formatting and basic validations locally before each commit.

---

## 6. Install Build Tooling

Many build commands depend on the code-quality tooling. Install it once:

```bash
mvn install -f eng/code-quality-reports/pom.xml
```

---

## Next Steps

- [Building the SDK](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/building.md)
- [Running unit tests](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/unit-testing.md)
- [Running live tests](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/live-testing.md)
- [Submitting a PR](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/CONTRIBUTING.md#pull-requests)
