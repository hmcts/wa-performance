# wa-performance
Performance tests for Work Allocation

To run locally:
- Performance test against the perftest environment: `./gradlew gatlingRun`

Flags:
- Debug (single-user mode): `-Ddebug=on e.g. ./gradlew gatlingRun -Ddebug=on`
- Run against AAT: `Denv=aat e.g. ./gradlew gatlingRun -Denv=aat`

Before running locally, update the client secret in src/gatling/resources/application.conf then run `git update-index --assume-unchanged src/gatling/resources/application.conf` to ensure the changes aren't pushed to github.

To make other configuration changes to the file, first run `git update-index --no-assume-unchanged src/gatling/resources/application.conf`, ensuring to remove the client secret before pushing to origin

## Useful info

* The UI simulation targets Work Allocation via XUI - this is being used for all WA performance testing

## Pre-test criteria:

* The file WA_TasksToCancel.csv consumes 324 rows per test - ensure the first 324 have been deleted from the prior test run before running another test
* The file IACCaseData.csv consumes 760 rows per test - as above, delete the first 760 before starting another test run
* The file PRLCaseData.csv consumes 108 rows per test - as above, delete the first 108 before starting another test run
* The file CivilJudicialCaseData.csv consumes 162 rows per test - as above, delete the first 162 before starting another test run
* The file FPLCaseData.csv consumes 335 rows per test - as above, delete the first 335 before starting another test run
* Ensure the Client Secrets are defined in application.conf