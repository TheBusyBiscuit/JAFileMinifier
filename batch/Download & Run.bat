@echo off

if not exist "%cd%/JAFileMinifier" (
	mkdir "%cd%/JAFileMinifier"
)

if not exist "%cd%/JAFileMinifier/JAFileMinifier.jar" (
	bitsadmin.exe /transfer "Jar Download" https://github.com/TheBusyBiscuit/JAFileMinifier/raw/master/dist/JAFileMinifier.jar "%cd%/JAFileMinifier/JAFileMinifier.jar"
)

set /p mode=Mode? (manual/auto)
java -jar JAFileMinifier/JAFileMinifier.jar %mode%

echo.
echo Press any key to exit

pause > nul
exit