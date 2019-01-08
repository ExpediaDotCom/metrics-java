# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## Unreleased

## 0.6.1 2019-01-08
### Changed
- MessagePackSerializer will now add an empty unit if the unit tag is missing. This is to match the behaviour of the MetricTank go code.

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
