# wa-performance
Performance tests for Work Allocation

To run locally:
- Performance test against the perftest environment: `./gradlew gatlingRun`

Flags:
- Debug (single-user mode): `-Ddebug=on e.g. ./gradlew gatlingRun -Ddebug=on`
- Run against AAT: `Denv=aat e.g. ./gradlew gatlingRun -Denv=aat`

### Task creation latency

The task message simulation publishes case events at a configured hourly rate and reports how long the resulting tasks take to appear in Task Management:

`./gradlew gatlingRun -DsimulationClass=simulations.TaskMessageLatencySimulation -DeventsPerHour=10000 -DmessageDurationMinutes=60`

Use `-Ddebug=on` to publish exactly one event, or add `-DdebugEventCount=10` to publish ten events at once. Service Bus and Task Management database credentials must be supplied through `AZURE_SERVICE_BUS_CONNECTION_STRING`, `TASK_DB_USER`, and `TASK_DB_PASSWORD`. The test leases a pool of existing `WA/WaCaseType` case IDs associated with `endToEndTask`, because those tasks are created by tests that first create a real WA case in CCD. Concurrent events cannot be matched to the same task; the test stops before publishing if the database does not contain enough cases for the configured load.

The default event, new state and expected task type are `endToEndTask`, `TODO` and `endToEndTask`. Override them together when testing a different initiation rule. Messages are published with the Azure Service Bus SDK and the same application properties as the WA post-deployment tests, including `jurisdiction_id` and `message_context=wa-ft-<caseId>`; override the context prefix with `-DmessageContextPrefix=...` if an environment uses a different rule. The test calculates the same idempotency key as Case Event Handler from `EventInstanceId + expectedTaskType` and finds the task by indexed case ID plus that key in `additional_properties`. The key prevents another task for the same case from being mistaken for the measured task. Task-creation latency is measured from message publication until the matching task's UTC `created` timestamp in the Task Management database. This requires the performance-environment trigger to set `created` from `clock_timestamp()` when each test task is inserted. The standard Gatling report records this lifecycle as `Task_Creation_Latency`; publication failures and task timeouts are reported as failed requests. Debug output includes the topic, case ID, message ID, idempotency key and timestamps required to trace a single message through Service Bus, CEH and Task Management.

## Useful info

* The WA simulation targets Work Allocation via XUI - this is being used for all WA performance testing

## 🔗 Submodules

The XUI-Performance repo now utilises calls from the [common-performance](https://github.com/hmcts/common-performance) repo, using the XUIHelper file.

Please ensure that you run `git submodule update --init --recursive` to populate the common folder with the required folders & files for the first time. Then run `./gradlew clean build` to ensure any changes are picked up. (For further information, please refer to the [Setup Instructions](https://github.com/hmcts/common-performance/tree/master?tab=readme-ov-file#%EF%B8%8F-setup-instructions))

#### Also ensure that you run `git submodule update --recursive --remote`, followed by `./gradlew clean build` to check that you have the latest code from the common repo, before running or doing any updates to this repo.

#### Note - These steps will also be required when you clone or run on the VMs!
