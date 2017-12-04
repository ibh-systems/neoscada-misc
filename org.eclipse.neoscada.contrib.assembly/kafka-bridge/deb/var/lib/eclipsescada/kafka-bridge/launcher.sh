#!/bin/bash

. /etc/default/eclipsescada

cd ~eclipsescada/"kafka-bridge"

exec screen -D -m -S "kafka-bridge" ./launcher
