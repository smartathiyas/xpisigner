@echo off
setlocal
SET PATH=%~dp0;%PATH%
java -Dappdata="%APPDATA%" -Dxpi.mode=jss -jar  %~dp0\xpi.jar %1 %2 %3 %4 %5 %6
endlocal