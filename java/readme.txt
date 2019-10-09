=== Copy Deps to Local ===
mvn install dependency:copy-dependencies

=== Build ===
mvn compile

=== Run ===
java -cp target\classes;target\dependency\* com.microsoft.storageperf.App
