#!/bin/bash

. /etc/default/eclipsescada

cd ~eclipsescada/"master"
exec screen -D -m -S "master" ./launcher
