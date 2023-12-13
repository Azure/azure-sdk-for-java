REM
REM A Windows script to run all Java samples in the folder.
REM
echo on

REM Optional cleanup 
rmdir /s /q target & del /s /q *.class

REM Copy dependencies
call mvn clean dependency:copy-dependencies

REM Compile all samples
javac *.java -cp ".;target\dependency\*"

REM Run all samples, one after the other
echo "===> SampleAnalyzeAllImageFile"
java -cp ".;target\dependency\*" SampleAnalyzeAllImageFile
echo "===> SampleCaptionImageFile"
java -cp ".;target\dependency\*" SampleCaptionImageFile
echo "===> SampleCaptionImageFileAsync"
java -cp ".;target\dependency\*" SampleCaptionImageFileAsync
echo "===> SampleCaptionImageUrl"
java -cp ".;target\dependency\*" SampleCaptionImageUrl
echo "===> SampleDenseCaptionsImageFile"
java -cp ".;target\dependency\*" SampleDenseCaptionsImageFile
echo "===> SampleObjectsImageFile"
java -cp ".;target\dependency\*" SampleObjectsImageFile
echo "===> SampleOcrImageFile"
java -cp ".;target\dependency\*" SampleOcrImageFile
echo "===> SampleOcrImageUrl"
java -cp ".;target\dependency\*" SampleOcrImageUrl
echo "===> SamplePeopleImageFile"
java -cp ".;target\dependency\*" SamplePeopleImageFile
echo "===> SampleSmartCropsImageFile"
java -cp ".;target\dependency\*" SampleSmartCropsImageFile
echo "===> SampleTagsImageFile"
java -cp ".;target\dependency\*" SampleTagsImageFile
