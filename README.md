# ServerStatsDB
Simple server stats (currently playercount and ticks) database logger for [RedstoneWorld](http://redstoneworld.de/)

## Builds
http://ci.minebench.de/job/ServerStatsDB/

## Config

``` yaml
# Period in seconds between running the stats collector
period: 60
# MySQL config:
storage:
    type: mysql
    host: 127.0.0.1
    port: 3306
    user: minecraft
    pass: password
    database: database
    table: serverstatsdb_log
```

## License

```
Copyright 2016 Max Lee (https://github.com/Phoenix616/)

This program is free software: you can redistribute it and/or modify
it under the terms of the Mozilla Public License as published by
the Mozilla Foundation, version 2.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
Mozilla Public License v2.0 for more details.

You should have received a copy of the Mozilla Public License v2.0
along with this program. If not, see <http://mozilla.org/MPL/2.0/>.
```
