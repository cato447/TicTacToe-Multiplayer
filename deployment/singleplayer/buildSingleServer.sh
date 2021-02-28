#!/bin/bash
cd /home/cato447/Code/ArcadeMachine/out/production/Server/
jar cmvf META-INF/MANIFEST.MF singleServer.jar *
scp singleServer.jar root@server:/root
rm singleServer.jar
cd /home/cato447/Code/ArcadeMachine/deployment/singleplayer/
./startSingleServer.sh