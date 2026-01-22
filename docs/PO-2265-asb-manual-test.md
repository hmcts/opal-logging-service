# PO-2265 Manual Azure Service Bus Test

## Purpose
Validate that a PDPO message published to Azure Service Bus is consumed by the logging service and persisted to the database.

## 1. Prerequisites
- Access to the Azure Service Bus namespace and PDPL queue.
- Access to the logging service database (or the test-support search endpoint).

## 2. Environment Variables
### Logging Service (consumer)
Set these before starting `opal-logging-service` (defaults point at the local emulator; consumer is enabled by default):

- `LOGGING_PDPL_CONSUMER_ENABLED=true` (optional; defaults to `true`)
- `SERVICEBUS_CONNECTION_STRING=<Azure Service Bus connection string>`
- `SERVICEBUS_LOGGING_PDPL_QUEUE_NAME=<queue name>` (e.g. `logging-pdpl`)
- `SERVICEBUS_LOGGING_PDPL_PROTOCOL=amqps` (optional; defaults to emulator-friendly `amqp`)

If your database settings are not already configured, set these as well:

- `OPAL_LOGGING_DB_HOST`
- `OPAL_LOGGING_DB_PORT`
- `OPAL_LOGGING_DB_NAME`
- `OPAL_LOGGING_DB_USERNAME`
- `OPAL_LOGGING_DB_PASSWORD`

### Logging Service Connectivity Test (publisher)
Set these before running the manual test in this repository (reuse the same queue settings):

- `LOGGING_PDPL_ASB_TEST_ENABLED=true`
- `SERVICEBUS_CONNECTION_STRING=<Azure Service Bus connection string>`
- `SERVICEBUS_LOGGING_PDPL_QUEUE_NAME=<queue name>` (e.g. `logging-pdpl`)

## 3. Enqueue a PDPO Message
### Run the connectivity test
From `opal-logging-service`:

```bash
LOGGING_PDPL_ASB_TEST_ENABLED=true ./gradlew integration \
  --tests uk.gov.hmcts.opal.logging.config.PdplQueueConnectivityIntegrationTest
```

This sends a PDPO payload with a `PDPL-IT-<uuid>` business identifier.

### Optional: confirm the message is on the queue
Use Azure Service Bus “peek” to confirm the message is waiting before starting the service.

## 4. Start the Logging Service
From `opal-logging-service`:

```bash
./gradlew bootRun
```

Confirm the service starts without JMS errors in the logs and consumes the queued message.

## 5. Verify the Message Was Persisted
### Option A - Database query

```sql
SELECT l.pdpo_log_id, i.business_identifier, l.created_at
FROM pdpo_log l
JOIN pdpo_identifiers i ON l.pdpo_identifiers_id = i.pdpo_identifiers_id
WHERE i.business_identifier LIKE 'PDPL-IT-%'
ORDER BY l.pdpo_log_id DESC
LIMIT 1;
```

### Option B - Test-support endpoint
If `OPAL_LOGGING_TEST_SUPPORT_ENABLED=true` is set, use the test-support search endpoint to confirm the log is present.

## 6. Troubleshooting
- If no record appears, check the logging service logs for JSON parsing or DB errors.
- If the message remains in the queue, ensure `LOGGING_PDPL_CONSUMER_ENABLED=true` and the connection string is valid.
- If the message moves to the dead-letter queue, check the delivery count and the error logs for the first failure reason.
