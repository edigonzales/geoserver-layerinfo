# geoserver-layerinfo

```
./gradlew clean build -x test
```

```
cp -v build/libs/geoserver-layerinfo-0.0.LOCALBUILD.jar ../docker-geoserver/jars
```

## todo
- Testcontainer für Tests
- Besteht Gefahr wegen Sql-Injection? -> x und y müssen Zahlen sein!
- Logging: Das loglevel kann man programmatisch setzen. -> Aus Property oder Env Var auslesen.
