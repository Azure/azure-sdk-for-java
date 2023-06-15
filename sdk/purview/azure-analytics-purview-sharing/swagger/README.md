## Generate autorest code

```yaml
input-file:
- https://github.com/Azure/azure-rest-api-specs/blob/main/specification/purview/data-plane/Azure.Analytics.Purview.Share/preview/2023-05-30-preview/share.json
output-folder: ../
java: true
regenerate-pom: false
data-plane: true
generate-tests: true
artifact-id: azure-analytics-purview-sharing
generate-samples: true
namespace: com.azure.analytics.purview.sharing
enable-sync-stack: false
service-versions:
- 2023-05-30-preview
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
      to: "SentShares_ListSentShares",
      from: "SentShares_GetInvitation",
      to: "SentShares_GetSentShareInvitation",
      from: "SentShares_CreateInvitation",
      to: "SentShares_CreateSentShareInvitation",
      from: "SentShares_DeleteInvitation",
      to: "SentShares_DeleteSentShareInvitation",
      from: "SentShares_ListInvitations",
      to: "SentShares_ListSentShareInvitations",
      from: "SentShares_NotifyUserInvitation",
      to: "SentShares_NotifyUserSentShareInvitation",
      from: "ReceivedShares_Get",
      to: "ReceivedShares_GetReceivedShare",
      from: "ReceivedShares_CreateOrReplace",
      to: "ReceivedShares_CreateOrReplaceReceivedShare",
      from: "ReceivedShares_Delete",
      to: "ReceivedShares_DeleteReceivedShare",
      from: "ReceivedShares_ListAttached",
      to: "ReceivedShares_ListAttachedReceivedShares",
      from: "ReceivedShares_ListDetached",
      to: "ReceivedShares_ListDetachedReceivedShares",
      from: "ShareResources_List",
      to: "ShareResources_ListShareResources"
    ]
```
