// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

def markdownFile = new File(basedir, "target/api-review/api.md")
def metadataFile = new File(basedir, "target/api-review/api.metadata.yml")
assert markdownFile.isFile()
assert metadataFile.isFile()
def bytes = markdownFile.bytes
def markdown = new String(bytes, "UTF-8")
assert markdown.contains("Widget")
assert !markdown.contains("\r")
assert markdown.endsWith("\n") && !markdown.endsWith("\n\n")
def hash = java.security.MessageDigest.getInstance("SHA-256").digest(bytes).encodeHex().toString()
assert metadataFile.getText("UTF-8") == "apiHash: ${hash}\npackageVersion: '1.0.0'\npackageRelativePath: 'sdk/test/azure-widget'\n"

def pluginPoms = basedir.parentFile.parentFile.listFiles().findAll {
    it.name.startsWith("api-review-maven-plugin-") && it.name.endsWith(".pom")
}
assert pluginPoms.size() == 1
def pluginJar = new File(pluginPoms[0].parentFile, pluginPoms[0].name.replaceFirst(/\.pom$/, ".jar"))
assert pluginJar.isFile()
def standalone = new File(basedir, "target/standalone")
java.nio.file.Files.createDirectories(standalone.toPath())
def tool = new File(standalone, "tool.jar")
def sources = new File(standalone, "azure-widget-1.0.0-sources.jar")
java.nio.file.Files.copy(pluginJar.toPath(), tool.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING)
java.nio.file.Files.copy(new File(basedir, "target/custom-output-sources.jar").toPath(), sources.toPath(),
    java.nio.file.StandardCopyOption.REPLACE_EXISTING)
def home = new File(standalone, "home")
java.nio.file.Files.createDirectories(home.toPath())
def destination = new File(standalone, "bundle")
def javaExecutable = new File(System.getProperty("java.home"),
    "bin/" + (System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java"))
def processBuilder = new ProcessBuilder(javaExecutable.absolutePath, "-Duser.home=" + home.absolutePath,
    "-Dhttp.proxyHost=127.0.0.1", "-Dhttp.proxyPort=1", "-Dhttps.proxyHost=127.0.0.1", "-Dhttps.proxyPort=1",
    "-jar", tool.absolutePath, "--sources-jar", sources.absolutePath,
    "--package-relative-path", "sdk/test/azure-widget", "--output-directory", destination.absolutePath)
processBuilder.directory(standalone).redirectErrorStream(true).redirectOutput(new File(standalone, "run.log"))
processBuilder.environment().remove("CLASSPATH")
def process = processBuilder.start()
if (!process.waitFor(30, java.util.concurrent.TimeUnit.SECONDS)) {
    process.destroyForcibly()
    throw new AssertionError("Standalone generation timed out.")
}
assert process.exitValue() == 0 : new File(standalone, "run.log").getText("UTF-8")
assert !new File(standalone, "pom.xml").exists()
assert !new File(home, ".m2").exists()
assert java.util.Arrays.equals(bytes, new File(destination, "api.md").bytes)
assert metadataFile.getText("UTF-8") == new File(destination, "api.metadata.yml").getText("UTF-8")
return true
