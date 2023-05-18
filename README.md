# wa-performance
Performance tests for Work Allocation

## Useful info

* The UI simulation targets Work Allocation via XUI - this is being used for all WA performance testing

## Pre-test criteria:

* The file WA_TasksToCancel.csv consumes 324 rows per test - ensure the first 324 have been deleted from the prior test run before running another test
* The file IACCaseData.csv consumes 760 rows per test - as above, delete the first 760 before starting another test run
* The file PRLCaseData.csv consumes 108 rows per test - as above, delete the first 108 before starting another test run
* The file CivilJudicialCaseData.csv consumes 162 rows per test - as above, delete the first 162 before starting another test run
* Ensure the CCD Gateway Client Secret is defined in application.conf
