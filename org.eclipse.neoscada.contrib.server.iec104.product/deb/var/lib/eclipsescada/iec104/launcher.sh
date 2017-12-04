#!/bin/bash

. /etc/default/eclipsescada

cd ~eclipsescada/"iec104"
exec screen -D -m -S "iec104" ./launcher
