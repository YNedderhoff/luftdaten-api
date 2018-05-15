# luftdaten-api

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/de987f0cfb5548bda1bf70bdb9774254)](https://app.codacy.com/app/YNedderhoff/luftdaten-api?utm_source=github.com&utm_medium=referral&utm_content=YNedderhoff/luftdaten-api&utm_campaign=badger)

An API on top of InfluxDB holding data from the [lufdaten.info](http://luftdaten.info) [particulate matter sensor](https://github.com/opendata-stuttgart/sensors-software) exposing several endpoints, some of which were intended to be used with [Brandwatch Vizia](https://www.brandwatch.com/vizia/).

## Endpoints

* `/ping`

check if this api is running

* `/pingInflux`

check if the connection to InfluxDB is working correctly

* `/temperature.json`, `/humidity.json`, `/pm.json`

Data of the last 3 hours in JSON stream format, e.g.

```json
{
   "series":[
      {
         "id":"temperature",
         "name":"feinstaub",
         "colour":"#FF4500",
         "axis":[
            "time",
            "Temperature (°C)"
         ],
         "values":[
            [
               "2017-11-25T15:00Z",
               9.25
            ],
            [
               "2017-11-25T16:00Z",
               8.091666666666667
            ],
            ...
            [
               "2017-11-28T15:00Z",
               7.75
            ]
         ]
      }
   ]
}
```

* `/temperature/last`, `/humidity/last`, `/pm1/last`, `/pm2/last` 

The last value for the requested measurement

```json
{
   "date":"2017-11-28T15:07Z",
   "label":"Temperature (°C)",
   "value":7.7
}

```

* `/lastMeasurements`

The last values of all measurements

```json
[
   {
      "date":"2017-11-28T15:00Z",
      "label":"Temperature (°C)",
      "value":7.75
   },
   {
      "date":"2017-11-28T15:00Z",
      "label":"Humidity (%)",
      "value":63
   },
   {
      "date":"2017-11-28T15:00Z",
      "label":"PM10 (µm)",
      "value":0.9
   },
   {
      "date":"2017-11-28T15:00Z",
      "label":"PM2.5 (µm)",
      "value":0.5
   }
]
```

## Build & Run

It is assumed that the InfluxDB instance holding the data is running. The settings have to be set in `application.properties` file in `src/main/resources` (you might need to create it), e.g.
 
```
spring.influxdb.url: <ip or url of InfluxDB instance>:<port of InfluxDB instance>
spring.influxdb.username: <username>
spring.influxdb.password: <password>
spring.influxdb.database: <database name>
```
 
Build luftdaten-api and run with Docker

```
mvn clean package
docker build -t luftdaten-api .
docker run -it --rm -p 8080:8080 luftdaten-api
```

The endpoints will be exposed at `localhost:8080`