# A PowerShell script to run all Java samples in the folder.
# The script does not have any arguments.

# Optional cleanup
Remove-Item -ErrorAction SilentlyContinue -Recurse -Force target
Remove-Item -ErrorAction SilentlyContinue -Recurse -Force *.class

# Copy dependencies
mvn clean dependency:copy-dependencies

# Compile all samples
javac *.java -cp ".;target\dependency\*"

# Run all samples, one after the other
Write-Host "===> SampleAnalyzeAllImageFile"
Start-Process -NoNewWindow -Wait java -ArgumentList "-cp", ".;target\dependency\*", "SampleAnalyzeAllImageFile"

Write-Host "===> SampleCaptionImageFile"
Start-Process -NoNewWindow -Wait java -ArgumentList "-cp", ".;target\dependency\*", "SampleCaptionImageFile"

Write-Host "===> SampleCaptionImageFileAsync"
Start-Process -NoNewWindow -Wait java -ArgumentList "-cp", ".;target\dependency\*", "SampleCaptionImageFileAsync"

Write-Host "===> SampleCaptionImageUrl"
Start-Process -NoNewWindow -Wait java -ArgumentList "-cp", ".;target\dependency\*", "SampleCaptionImageUrl"

Write-Host "===> SampleDenseCaptionsImageFile"
Start-Process -NoNewWindow -Wait java -ArgumentList "-cp", ".;target\dependency\*", "SampleDenseCaptionsImageFile"

Write-Host "===> SampleObjectsImageFile"
Start-Process -NoNewWindow -Wait java -ArgumentList "-cp", ".;target\dependency\*", "SampleObjectsImageFile"

Write-Host "===> SampleOcrImageFile"
Start-Process -NoNewWindow -Wait java -ArgumentList "-cp", ".;target\dependency\*", "SampleOcrImageFile"

Write-Host "===> SampleOcrImageUrl"
Start-Process -NoNewWindow -Wait java -ArgumentList "-cp", ".;target\dependency\*", "SampleOcrImageUrl"

Write-Host "===> SampleOcrImageUrlAsync"
Start-Process -NoNewWindow -Wait java -ArgumentList "-cp", ".;target\dependency\*", "SampleOcrImageUrlAsync"

Write-Host "===> SamplePeopleImageFile"
Start-Process -NoNewWindow -Wait java -ArgumentList "-cp", ".;target\dependency\*", "SamplePeopleImageFile"

Write-Host "===> SampleSmartCropsImageFile"
Start-Process -NoNewWindow -Wait java -ArgumentList "-cp", ".;target\dependency\*", "SampleSmartCropsImageFile"

Write-Host "===> SampleTagsImageFile"
Start-Process -NoNewWindow -Wait java -ArgumentList "-cp", ".;target\dependency\*", "SampleTagsImageFile"
