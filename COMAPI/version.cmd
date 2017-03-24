SET "JAVA_HOME=C:\Program Files\Java\jdk1.8.0_74"
call PUSHD ".\COMAPI"
for /f "delims=" %%a in ('.\gradlew -q printFoundationVersion') do @set foundationVersion=%%a
for /f "delims=" %%b in ('.\gradlew -q printBuildNumber') do @set foundationBuildNumber=%%b

set foundationVersion=%foundationVersion:-SNAPSHOT=%
echo newVer = %foundationVersion%

powershell -Command "(gc .\foundation\src\main\java\com\comapi\BaseComapi.java) -replace 'VER_TO_REPLACE', '%foundationVersion%' | Out-File .\foundation\src\main\java\com\comapi\BaseComapi.java -encoding UTF8"
powershell -Command "(gc .\foundation\src\main\java\com\comapi\BaseComapi.java) -replace 'BUILD_NUM_TO_REPLACE', %foundationBuildNumber% | Out-File .\foundation\src\main\java\com\comapi\BaseComapi.java -encoding UTF8"

Powershell.exe -executionpolicy remotesigned -File .\removeBOM.ps1