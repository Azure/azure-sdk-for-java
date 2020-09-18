Write-Host "Starting swagger overwrites for Azure.Communication.Chat"
$swaggerFilePath="swagger.json"
$replacementMapping=@{
    'AddChatThreadMembersRequest'='AddChatThreadMembersOptions'
    'CreateChatThreadRequest'='CreateChatThreadOptions'
    'SendChatMessageRequest'='SendChatMessageOptions'
    'UpdateChatMessageRequest'='UpdateChatMessageOptions'
    'UpdateChatThreadRequest'='UpdateChatThreadOptions'}
$fileContent=Get-Content $swaggerFilePath
Foreach ($key in $replacementMapping.Keys) {
    $fileContent = $fileContent.Replace($key, $replacementMapping.$key)
}
Set-Content -Path $swaggerFilePath -Value $fileContent
Write-Host "Completed swagger overwrites for Azure.Communication.Chat"
