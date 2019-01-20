# metrics-java

Utility library for working with [Metrics 2.0](http://metrics20.org/) in JVM languages

There are three modules:
- [main](https://github.com/ExpediaDotCom/metrics-java/tree/master/main) contains Java classes closely based on the [Metrics 2.0 specification](metrics20.org/spec/)
- [metrictank](https://github.com/ExpediaDotCom/metrics-java/tree/master/metrictank) contains serializers for reading and writing data in the [MetricTank Kafka-mdm formats](https://github.com/grafana/metrictank/blob/master/docs/inputs.md#kafka-mdm-recommended)
- [jackson](https://github.com/ExpediaDotCom/metrics-java/tree/master/jackson) contains classes for serialising metrics to and from JSON using the [Jackson library](https://github.com/FasterXML/jackson)

## Build

To build this library, one will need `jdk 1.8`

Run the following command to build the library 

```
./mvnw clean package
```
