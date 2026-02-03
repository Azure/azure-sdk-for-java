## Platform Matrix FAQs

### When to use the `IsLatestNonLtsJdk` flag in the platform-matrix file.
If the latest version of Java is a non-LTS version, then the `IsLatestNonLtsJdk` flag should be set to `true`. This will tell our test agents to manually download said non-LTS Java version. Here's an example of what an entry of platform-matrix using the `IsLatestNonLtsJdk` flag would look like:

```json
{
  "Agent": {
    "ubuntu-24.04": { "OSVmImage": "ubuntu-24.04", "Pool": "azsdk-pool" }
  },
  "JavaTestVersion": "1.20",
  "AZURE_TEST_HTTP_CLIENTS": "netty",
  "TestFromSource": false,
  "RunAggregateReports": false,
  "TestGoals": "surefire:test",
  "TestOptions": "",
  "IsLatestNonLtsJdk": true
}
```
