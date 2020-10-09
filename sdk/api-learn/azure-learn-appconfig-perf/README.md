# Build
```
> mvn install -f eng\code-quality-reports\pom.xml

> mvn install -f common\perf-test-core\pom.xml -Dgpg.skip -Drevapi.skip -DskipTests -Dcheckstyle.skip -Dspotbugs.skip

> mvn install -f sdk\api-learn\pom.xml -Dgpg.skip -Drevapi.skip -DskipTests -Dcheckstyle.skip -Dspotbugs.skip
```

# Run
```
> java -jar sdk\api-learn\azure-learn-appconfig-perf\target\azure-learn-appconfig-perf-1.0.0-beta.1-jar-with-dependencies.jar getconfigurationsetting --size 10

=== Options ===
{
  "count" : 10,
  "duration" : 10,
  "host" : null,
  "insecure" : false,
  "iterations" : 1,
  "noCleanup" : true,
  "parallel" : 1,
  "port" : -1,
  "size" : 10,
  "sync" : false,
  "warmup" : 10
}

=== Setup ===
.

=== Warmup ===
Current         Total
44              44
54              98
54              152
49              201
51              252
53              305
55              360
49              409
55              464
56              520
1               521

=== Results ===
Completed 521 operations in a weighted-average of 9.98s (52.20 ops/s, 0.019 s/op)

=== Test ===
Current         Total
51              51
53              104
56              160
52              212
55              267
51              318
57              375
53              428
54              482
55              537
0               537

=== Results ===
Completed 537 operations in a weighted-average of 9.99s (53.74 ops/s, 0.019 s/op)

=== Cleanup ===

``` 
