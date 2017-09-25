#!/bin/bash

. /etc/default/eclipsescada

cd ~eclipsescada/"modbus"
exec screen -D -m -S "modbus" ./launcher
