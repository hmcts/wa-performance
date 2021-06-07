# wa-performance
Performance tests for Work Allocation

## Useful info

* The Master branch contains the pipeline tests run against AAT, whereby the required secrets are obtained from the vault
  * There should be no updates/changes made to the Master branch unless required for Pipeline fixes
* The Perftest branch contains performance tests to be run against Perftest, the required secrets will need to be added to the application.conf file locally