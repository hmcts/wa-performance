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

* The WA simulation targets Work Allocation via XUI - this is being used for all WA performance testing

## 🔗 Submodules

The WA-Performance repo now utilises calls from the [common-performance](https://github.com/hmcts/common-performance) repo, using the XUIHelper file.

Please ensure that you run `git submodule update --init --recursive` to populate the common folder with the required folders & files for the first time. Then run `./gradlew clean build` to ensure any changes are picked up. (For further information, please refer to the [Setup Instructions](https://github.com/hmcts/common-performance/tree/master?tab=readme-ov-file#%EF%B8%8F-setup-instructions))


#### Also ensure that you run `git submodule update --recursive --remote`, followed by `./gradlew build` to check that you have the latest code from the common repo, before running or doing any updates to this repo.

#### Note - These steps will also be required when you clone or run on the VMs!