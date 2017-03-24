@echo off

SET "JAVA_HOME=C:\Program Files\Java\jdk1.8.0_74"
start cmd /c call adb devices -l | find "device product:" >nul
if errorlevel 1 (
    echo No connected devices
	start cmd /c call emulator -avd Nexus4_23 -wipe-data -no-skin -no-audio -no-window
) else (
    echo Found connected device
)

:loop
echo checking if device ready...

cmd /c call adb wait-for-device shell getprop init.svc.bootanim > tmpFile 
set /p var= < tmpFile 
del tmpFile 

ECHO var=%var%
if NOT "%var%"=="stopped" (
	echo waiting ...
	ping 127.0.0.1 -n 3 > nul
	goto loop
) else (
	ECHO emulator is ready
	call PUSHD ".\COMAPI"
	call .\gradlew.bat connectedAndroidTest -i
)

exit 0