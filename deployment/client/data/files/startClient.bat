@echo off
IF exist javafx-sdk-11.0.2 ( goto launch ) ELSE ( goto create_enviorment && goto launch)

:create_enviorment
powershell -command "Expand-Archive -Force 'openjfx-11.0.2_windows-x64_bin-sdk.zip' 'library'
cd library
move javafx-sdk-11.0.2 ../
cd ../
rmdir library

:launch
set /p playerName="Gebe deinen Spielernamen ein: " 
java -jar --module-path javafx-sdk-11.0.2\lib --add-modules javafx.controls TicTacToe_Client.jar %playerName%