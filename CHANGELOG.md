# Changelog

## [0.11.7](https://github.com/statisticsnorway/rawdata-converter/tree/0.11.7) (2021-06-30)

[Full Changelog](https://github.com/statisticsnorway/rawdata-converter/compare/0.11.6...0.11.7)

**Implemented enhancements:**

- Pseudonymization report [\#23](https://github.com/statisticsnorway/rawdata-converter/issues/23)
- Option to specify non-encrypted rawdata message items [\#22](https://github.com/statisticsnorway/rawdata-converter/issues/22)
- Support filtering and ordering of job execution summaries [\#21](https://github.com/statisticsnorway/rawdata-converter/issues/21)

## [0.11.6](https://github.com/statisticsnorway/rawdata-converter/tree/0.11.6) (2021-06-06)

[Full Changelog](https://github.com/statisticsnorway/rawdata-converter/compare/0.11.5...0.11.6)

## [0.11.5](https://github.com/statisticsnorway/rawdata-converter/tree/0.11.5) (2021-05-29)

[Full Changelog](https://github.com/statisticsnorway/rawdata-converter/compare/0.11.4...0.11.5)

## [0.11.4](https://github.com/statisticsnorway/rawdata-converter/tree/0.11.4) (2021-05-29)

[Full Changelog](https://github.com/statisticsnorway/rawdata-converter/compare/0.11.3...0.11.4)

## [0.11.3](https://github.com/statisticsnorway/rawdata-converter/tree/0.11.3) (2021-05-27)

[Full Changelog](https://github.com/statisticsnorway/rawdata-converter/compare/0.11.2...0.11.3)

**Fixed bugs:**

- Rawdata client provider GCS if metadata could not be resolved [\#20](https://github.com/statisticsnorway/rawdata-converter/issues/20)

## [0.11.2](https://github.com/statisticsnorway/rawdata-converter/tree/0.11.2) (2021-05-26)

[Full Changelog](https://github.com/statisticsnorway/rawdata-converter/compare/0.11.1...0.11.2)

## [0.11.1](https://github.com/statisticsnorway/rawdata-converter/tree/0.11.1) (2021-05-20)

[Full Changelog](https://github.com/statisticsnorway/rawdata-converter/compare/0.11.0...0.11.1)

**Closed issues:**

- Add option to ignore conversion errors [\#19](https://github.com/statisticsnorway/rawdata-converter/issues/19)

## [0.11.0](https://github.com/statisticsnorway/rawdata-converter/tree/0.11.0) (2021-04-15)

[Full Changelog](https://github.com/statisticsnorway/rawdata-converter/compare/0.10.6...0.11.0)

**Fixed bugs:**

- Retrieving secrets without an explicit version should default to using "latest" as version [\#17](https://github.com/statisticsnorway/rawdata-converter/issues/17)

## [0.10.6](https://github.com/statisticsnorway/rawdata-converter/tree/0.10.6) (2021-03-11)

[Full Changelog](https://github.com/statisticsnorway/rawdata-converter/compare/0.10.5...0.10.6)

## [0.10.5](https://github.com/statisticsnorway/rawdata-converter/tree/0.10.5) (2021-03-11)

[Full Changelog](https://github.com/statisticsnorway/rawdata-converter/compare/0.10.4...0.10.5)

**Merged pull requests:**

- Allow use of topic metadata instead of samples in V2. [\#16](https://github.com/statisticsnorway/rawdata-converter/pull/16) ([kimcs](https://github.com/kimcs))

## [0.10.4](https://github.com/statisticsnorway/rawdata-converter/tree/0.10.4) (2021-03-11)

[Full Changelog](https://github.com/statisticsnorway/rawdata-converter/compare/0.10.3...0.10.4)

**Implemented enhancements:**

- Upgrade to Micronaut 2.4.x [\#15](https://github.com/statisticsnorway/rawdata-converter/issues/15)

## [0.10.3](https://github.com/statisticsnorway/rawdata-converter/tree/0.10.3) (2021-03-11)

[Full Changelog](https://github.com/statisticsnorway/rawdata-converter/compare/0.10.2...0.10.3)

**Implemented enhancements:**

- Upgrade to Micronaut 2.3.x [\#4](https://github.com/statisticsnorway/rawdata-converter/issues/4)

**Merged pull requests:**

- Use secret service for pseudo and client credentials [\#14](https://github.com/statisticsnorway/rawdata-converter/pull/14) ([kschulst](https://github.com/kschulst))
- Add SecretService [\#13](https://github.com/statisticsnorway/rawdata-converter/pull/13) ([kschulst](https://github.com/kschulst))

## [0.10.2](https://github.com/statisticsnorway/rawdata-converter/tree/0.10.2) (2021-03-03)

[Full Changelog](https://github.com/statisticsnorway/rawdata-converter/compare/0.10.1...0.10.2)

## [0.10.1](https://github.com/statisticsnorway/rawdata-converter/tree/0.10.1) (2021-03-03)

[Full Changelog](https://github.com/statisticsnorway/rawdata-converter/compare/0.10.0...0.10.1)

## [0.10.0](https://github.com/statisticsnorway/rawdata-converter/tree/0.10.0) (2021-03-03)

[Full Changelog](https://github.com/statisticsnorway/rawdata-converter/compare/0.9.5...0.10.0)

**Implemented enhancements:**

- Use GCP Secret Manager for secrets [\#3](https://github.com/statisticsnorway/rawdata-converter/issues/3)

**Merged pull requests:**

- Use GCP Secret Manager for secrets [\#12](https://github.com/statisticsnorway/rawdata-converter/pull/12) ([kschulst](https://github.com/kschulst))

## [0.9.5](https://github.com/statisticsnorway/rawdata-converter/tree/0.9.5) (2021-02-18)

[Full Changelog](https://github.com/statisticsnorway/rawdata-converter/compare/0.9.4...0.9.5)

**Implemented enhancements:**

- Include reference to rawadata-source and topic in logs [\#7](https://github.com/statisticsnorway/rawdata-converter/issues/7)

**Fixed bugs:**

- Duplicates after using intial-position=LAST [\#10](https://github.com/statisticsnorway/rawdata-converter/issues/10)

**Merged pull requests:**

- Don't include actual initial position if initialPosition=LAST [\#11](https://github.com/statisticsnorway/rawdata-converter/pull/11) ([kschulst](https://github.com/kschulst))

## [0.9.4](https://github.com/statisticsnorway/rawdata-converter/tree/0.9.4) (2021-02-17)

[Full Changelog](https://github.com/statisticsnorway/rawdata-converter/compare/0.9.3...0.9.4)

**Fixed bugs:**

- JWT for accessing dapla services  is expired [\#2](https://github.com/statisticsnorway/rawdata-converter/issues/2)

**Merged pull requests:**

- Fix jwt expired bug [\#5](https://github.com/statisticsnorway/rawdata-converter/pull/5) ([kschulst](https://github.com/kschulst))

## [0.9.3](https://github.com/statisticsnorway/rawdata-converter/tree/0.9.3) (2021-02-16)

[Full Changelog](https://github.com/statisticsnorway/rawdata-converter/compare/0.9.2...0.9.3)

**Merged pull requests:**

- Relax converter job logging [\#1](https://github.com/statisticsnorway/rawdata-converter/pull/1) ([kschulst](https://github.com/kschulst))

## [0.9.2](https://github.com/statisticsnorway/rawdata-converter/tree/0.9.2) (2021-02-16)

[Full Changelog](https://github.com/statisticsnorway/rawdata-converter/compare/0.9.1...0.9.2)



\* *This Changelog was automatically generated by [github_changelog_generator](https://github.com/github-changelog-generator/github-changelog-generator)*
