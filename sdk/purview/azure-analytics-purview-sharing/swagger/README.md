## Generate autorest code

```yaml
input-file:
- https://github.com/Azure/azure-rest-api-specs/blob/12cbd66750c4dfe302e9eca42b1fc727f8c02700/specification/purview/data-plane/Azure.Analytics.Purview.Share/preview/2023-02-15-preview/share.json
output-folder: ../
java: true
regenerate-pom: false
data-plane: true
generate-tests: true
artifact-id: azure-analytics-purview-sharing
generate-samples: true
namespace: com.azure.analytics.purview.sharing
service-versions:
- 2023-02-15-preview
directive:
  rename-operation:
    [
      from: "SentShares_Get",
      to: "SentShares_GetSentShare",
      from: "SentShares_CreateOrReplace",
      to: "SentShares_CreateOrReplaceSentShare",
      from: "SentShares_Delete",
      to: "SentShares_DeleteSentShare",
      from: "SentShares_List",
      to: "SentShares_GetAllSentShares",
      from: "SentShares_GetInvitation",
      to: "SentShares_GetSentShareInvitation",
      from: "SentShares_CreateInvitation",
      to: "SentShares_CreateSentShareInvitation",
      from: "SentShares_DeleteInvitation",
      to: "SentShares_DeleteSentShareInvitation",
      from: "SentShares_ListInvitations",
      to: "SentShares_GetAllSentShareInvitations",
      from: "SentShares_NotifyUserInvitation",
      to: "SentShares_NotifyUserSentShareInvitation",
      from: "ReceivedShares_Get",
      to: "ReceivedShares_GetReceivedShare",
      from: "ReceivedShares_CreateOrReplace",
      to: "ReceivedShares_CreateOrReplaceReceivedShare",
      from: "ReceivedShares_Delete",
      to: "ReceivedShares_DeleteReceivedShare",
      from: "ReceivedShares_ListAttached",
      to: "ReceivedShares_GetAllAttachedReceivedShares",
      from: "ReceivedShares_ListDetached",
      to: "ReceivedShares_GetAllDetachedReceivedShares",
    ]
```
```
