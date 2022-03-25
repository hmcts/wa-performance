# wa-performance
Performance tests for Work Allocation

## Useful info

* The Master branch contains the pipeline tests run against AAT, whereby the required secrets are obtained from the vault
  * There should be no updates/changes made to the Master branch unless required for Pipeline fixes
* The Perftest branch contains performance tests to be run against Perftest, the required secrets will need to be added to the application.conf file locally

## Perftest Details

* There are 2 simulations available in the Perftest branch, located in the simulations folder
  * The UI simulation targets Work Allocation via XUI - this is being used for R2 performance testing

## Pre-test criteria:

* The file WA_TasksToCancel.csv consumes 40 rows per test - ensure the first 40 have been deleted from the prior test run before running another test
* The file WA_R2Cases.csv consumes 600 rows per test - as above, delete the first 600 before starting another test run
* Ensure the CCD Gateway Client Secret is defined in application.conf
