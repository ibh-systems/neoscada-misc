Eclipse NeoSCADA MISC
=====================

Status & Health monitoring
--------------------------

A small tool which provides a status & health HTTP endpoint to monitor your 
NeoSCADA services.

There are actually several elements to it:

* JMX monitoring
* some threshold monitoring for NeoSCADA items
* some threshold monitoring for IEC104 signals

At the moment there is a debian distribution available, which provides start
scripts for both upstart and systemd.

It will use the `eclipsescada` user, though if it doesn't exist, it will create it.

The configuration is a json file located at `/var/lib/eclipsescada/status/config.json`

The logback configuration is located at `/var/lib/eclipsescada/status/logback.xml`

The default logfile is put at: `/var/log/eclipsescada/app.status.log`

The configuration provides the ability to monitor several server at once, and merge 
the status of those into one.

The standard port is 8080.

It will return http status 404 for anything which is not found in the configuration. 
If the status is OK (or WARNING) the http status will be 200. If the state is 
CRITICAL, then the http status code is 500.

The configuration:

    {"<node>" : {
        "<hostname>" : {
            "neoscada" : [
                {"name": "master", 
                 "daUrl" : "da:ngp://localhost:2101", 
                 "jmxPort" : 9001, 
                 "items": [
                    {"tag": "statistics.sessions.total", "ll": 1.0},
                    {"tag": "a.sample.item", 
                     "ll": 0.0
                     "l": 1.0,
                     "h": 100.0,
                     "hh": 500.0,
                     "checkTime": true,
                     "timeDelta": 900000.0,
                     "checkTime": true,
                     "checkToggle": false
                     }
                ]}
            ],
            "iec104" : [
                {"name": "a-104-server", 
                 "port" : 2404,
                 "items": [
                    {"tag": "0.1.2.3.4", "ll": 0.0, "l": 10.0},
                    ...
            ]
        }
    }

So for all SCADA/IEC tags, the levels can be configured individually. For JMX the
alarm levels are fix for now:

* free Heap Memory < 5% -> WARNING
* free Heap Memory < 0.5% -> CRITICAL
* LoadAverage > 0.95 -> WARNING
* LoadAverage > 2 -> CRITICAL

Also there is no option for JMX monitoring other then the NeoSCADA instances yet.

The configuration won't be read automatically, so if the configuration changes, then
the service has to be restarted.

**WARNING**: This service is not tested thoroughly yet, so use it on your own risk.
