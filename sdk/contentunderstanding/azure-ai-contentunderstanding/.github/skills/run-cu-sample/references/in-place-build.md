# In-Place Build Process for Content Understanding SDK

## Overview

The in-place build process allows you to compile and run Content Understanding SDK samples without installing the SDK package to your local Maven repository. This is useful during SDK development when you want to test changes quickly without going through the full Maven install cycle.

## Why In-Place Build?

### Traditional Maven Workflow
```
mvn install  # Installs package to ~/.m2/repository
mvn exec:java -Dexec.mainClass="..."  # Runs sample
```

**Drawbacks:**
- Requires full install cycle
- Slower iteration during development
- Installs to local repository even for testing

### In-Place Build Workflow
```
mvn compile -DskipTests  # Compiles to target/classes
# Build classpath and compile samples
# Run directly with java -cp
```

**Advantages:**
- Faster iteration
- No local repository pollution
- Direct control over classpath
- Works well for sample development

## Build Steps Explained

### Step 1: Compile Main SDK Code
```bash
mvn compile -DskipTests
```

**What it does:**
- Compiles `src/main/java/**/*.java` to `target/classes/`
- Processes resources from `src/main/resources/`
- Skips test compilation and execution
- Creates the compiled SDK classes needed by samples

**Output:** `target/classes/` directory with compiled SDK classes

### Step 2: Build Classpath
```bash
mvn dependency:build-classpath -Dmdep.outputFile=target/classpath.txt -q
```

**What it does:**
- Resolves all project dependencies (including transitive)
- Builds a classpath string with all JAR file paths
- Writes the classpath to `target/classpath.txt`
- Uses `-q` (quiet) to reduce output

**Output:** `target/classpath.txt` file containing colon-separated (Linux/Mac) or semicolon-separated (Windows) JAR paths

**Example content:**
```
/home/user/.m2/repository/com/azure/azure-core/1.57.1/azure-core-1.57.1.jar:/home/user/.m2/repository/com/azure/azure-core-http-netty/1.16.3/azure-core-http-netty-1.16.3.jar:...
```

### Step 3: Set Classpath Environment Variable
```bash
CLASSPATH=$(cat target/classpath.txt):target/classes
```

**What it does:**
- Reads the classpath from the file
- Appends `target/classes` (the compiled SDK) to the classpath
- Sets the `CLASSPATH` environment variable

**Why `target/classes` is needed:**
- The classpath file only contains dependency JARs
- We need the locally compiled SDK classes in the classpath
- Order matters: dependencies first, then our classes

### Step 4: Compile Samples
```bash
javac -cp "$CLASSPATH" --release 8 -d target/classes \
    src/samples/java/com/azure/ai/contentunderstanding/samples/*.java
```

**What it does:**
- Compiles all sample Java files
- Uses `-cp "$CLASSPATH"` to find SDK classes and dependencies
- Uses `--release 8` for Java 8 compatibility
- Outputs `.class` files to `target/classes/` (same as main SDK)

**Why direct `javac`:**
- Maven's `compile` phase doesn't compile samples by default
- Samples are typically in a separate source set
- Direct compilation gives us control

### Step 5: Run Sample
```bash
java -cp "$CLASSPATH" com.azure.ai.contentunderstanding.samples.Sample02_AnalyzeUrlAsync
```

**What it does:**
- Runs the sample's `main()` method
- Uses the classpath to find all required classes
- Executes in the current JVM

## Classpath Structure

The final classpath contains:

1. **Dependency JARs** (from `target/classpath.txt`):
   - `azure-core-*.jar`
   - `azure-core-http-netty-*.jar`
   - `azure-identity-*.jar`
   - All transitive dependencies

2. **Compiled SDK Classes** (`target/classes`):
   - `com/azure/ai/contentunderstanding/**/*.class`
   - Main SDK implementation

3. **Compiled Sample Classes** (`target/classes`):
   - `com/azure/ai/contentunderstanding/samples/*.class`
   - Sample code

## Environment Variables

Samples typically require environment variables:

- `CONTENTUNDERSTANDING_ENDPOINT` - Service endpoint URL
- `CONTENTUNDERSTANDING_KEY` - API key (optional, if using key auth)
- Other configuration as needed

Load from `.env` file:
```bash
set -a
source .env
set +a
```

## Comparison with Maven Exec Plugin

### Maven Exec Plugin Approach
```bash
mvn compile exec:java -Dexec.mainClass="com.azure.ai.contentunderstanding.samples.Sample02_AnalyzeUrlAsync"
```

**Pros:**
- Simpler command
- Maven handles classpath automatically

**Cons:**
- Still requires compile phase
- Less control over classpath
- May not work well with samples in separate source set

### In-Place Build Approach
```bash
# Build once
mvn compile -DskipTests
mvn dependency:build-classpath -Dmdep.outputFile=target/classpath.txt -q
CLASSPATH=$(cat target/classpath.txt):target/classes
javac -cp "$CLASSPATH" --release 8 -d target/classes src/samples/java/.../*.java

# Run multiple times
java -cp "$CLASSPATH" com.azure.ai.contentunderstanding.samples.Sample02_AnalyzeUrlAsync
```

**Pros:**
- Full control over compilation and execution
- Can run multiple times without rebuilding
- Works with any sample structure
- Faster iteration

**Cons:**
- More steps initially
- Manual classpath management

## Troubleshooting

### Classpath Issues
- **Problem**: `ClassNotFoundException` or `NoClassDefFoundError`
- **Solution**: Verify `target/classpath.txt` exists and includes all dependencies
- **Check**: Ensure `target/classes` is in the classpath

### Compilation Errors
- **Problem**: `javac` can't find SDK classes
- **Solution**: Ensure main SDK is compiled (`mvn compile -DskipTests`)
- **Check**: Verify `target/classes` contains compiled SDK classes

### Sample Not Found
- **Problem**: `ClassNotFoundException` for sample class
- **Solution**: Ensure samples are compiled to `target/classes`
- **Check**: Verify sample class file exists in expected package structure

### Environment Variables
- **Problem**: Sample fails with missing configuration
- **Solution**: Load `.env` file or set environment variables
- **Check**: Verify required variables are set: `echo $CONTENTUNDERSTANDING_ENDPOINT`

## Best Practices

1. **Build classpath once**: Reuse `target/classpath.txt` unless dependencies change
2. **Compile samples separately**: Only recompile samples when sample code changes
3. **Use scripts**: Encapsulate the process in scripts for repeatability
4. **Check prerequisites**: Verify environment variables before running
5. **Clean when needed**: Run `mvn clean` if you encounter stale class files

## Integration with IDEs

Most IDEs can be configured to use this approach:

- **IntelliJ IDEA**: Configure run configuration with custom classpath
- **VS Code**: Use Java extension with custom classpath settings
- **Eclipse**: Set up classpath in run configuration

However, the script-based approach is often simpler and more portable.
