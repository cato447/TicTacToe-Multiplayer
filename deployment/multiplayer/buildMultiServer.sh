#!/bin/bash
cd /home/cato447/Code/ArcadeMachine/out/production/Server/
jar cmvf META-INF/multiServerManifest.MF multiServer.jar *
scp multiServer.jar root@server:/root
rm multiServer.jar
cd /home/cato447/Code/ArcadeMachine/deployment/multiplayer/
./startMultiServer.sh