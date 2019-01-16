# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## Unreleased
### Changed
- Metrictank interval and org_id are now stored as metatags instead of tags. This makes it possible to use tags with those names, however metrics serialised with earlier versions of metrics-java will not be compatible with this release.

## 0.6.2 2019-01-08
### Changed
- MessagePackSerializer deserialisation validation is now closer to the MetricTank go implementation behaviour. As a consqeuence if the unit, value, timestamp, or tags are missing a default empty or zero value will be supplied.

## 0.6.1 2018-10-18
This version number was partially released and should not be used

## 0.6.0 2018-11-02
### Added
- MetricPointSerializer for reading and writing the Metrictank Kafka-mdm MetricPoint format

### Changed
- MessagePackSerializer now reads the Metrictank Kafka-mdm MetricData format, the previous implementation was not compatible with Metrictank

## 0.5.0 2018-10-03
### Added
- Jackson MetricsJavaModule to allow existing ObjectMappers to serialize MetricData

## 0.4.0 2018-09-21
### Added
- Deserializer for [MetricPoint](https://github.com/raintank/schema/blob/master/metricpoint.go) and MetricData from Kafka-mdm

## 0.3.0 2018-09-11
### Added
- Added source and javadoc to the build

## 0.2.0 2018-09-07
### Fixed
- Creating a metric definition from a key now supplies default values for the required tags

## 0.1.0 2018-09-06
### Added
- Initial implementation of [MetricData](https://github.com/raintank/schema/blob/faebde8e89e024d82c8c7b3bd9c8cd5f794b3b38/metric.go#L33)
