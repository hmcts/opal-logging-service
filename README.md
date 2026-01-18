# opal-logging-service

## Building and deploying the application

### Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install gradle.

To build the project execute the following command:

```bash
  ./gradlew build
```

### Running the application

Create the image of the application by executing the following command:

```bash
  ./gradlew assemble
```

Note: Docker Compose V2 is highly recommended for building and running the application.
In the Compose V2 old `docker-compose` command is replaced with `docker compose`.

Create docker image:

```bash
  docker compose build
```

Run the distribution (created in `build/install/opal-logging-service` directory)
by executing the following command:

```bash
  docker compose up
```

This will start the API container exposing the application's port
(set to `4065` in this template app).

In order to test if the application is up, you can call its health endpoint:

```bash
  curl http://localhost:4065/health
```

You should get a response similar to this:

```
  {"status":"UP","diskSpace":{"status":"UP","total":249644974080,"free":137188298752,"threshold":10485760}}
```

### Alternative script to run application

To skip all the setting up and building, just execute the following command:

```bash
./bin/run-in-docker.sh
```

For more information:

```bash
./bin/run-in-docker.sh -h
```

Script includes bare minimum environment variables necessary to start api instance. Whenever any variable is changed or any other script regarding docker image/container build, the suggested way to ensure all is cleaned up properly is by this command:

```bash
docker compose rm
```

It clears stopped containers correctly. Might consider removing clutter of images too, especially the ones fiddled with:

```bash
docker images

docker image rm <image-id>
```

There is no need to remove postgres and java or similar core images.

## Environment variables

### Database (local run)
When running the service locally (outside Docker), use the host-mapped database port from
`docker-compose.yml`:

- `OPAL_LOGGING_DB_HOST=localhost`
- `OPAL_LOGGING_DB_PORT=5436`
- `OPAL_LOGGING_DB_NAME=opal-logging-db`
- `OPAL_LOGGING_DB_USERNAME=opal-logging`
- `OPAL_LOGGING_DB_PASSWORD=opal-logging`

### PDPL queue consumer
Enable the Azure Service Bus consumer and provide the queue connection details:

- `LOGGING_PDPL_CONSUMER_ENABLED=true`
- `LOGGING_PDPL_CONNECTION_STRING=<Azure Service Bus connection string>`
- `LOGGING_PDPL_QUEUE=logging-pdpl`

### PDPL queue processing notes
The listener uses `Session.CLIENT_ACKNOWLEDGE`, so acknowledgements are only sent after the
`@JmsListener` completes successfully. Let exceptions propagate (do not swallow them) so
failed messages are redelivered.

### PDPL queue manual publisher (developer testing)
Use the manual publisher to enqueue a PDPO message to any Azure Service Bus queue:

- `LOGGING_PDPL_ASB_TEST_ENABLED=true`
- `LOGGING_PDPL_CONNECTION_STRING=<Azure Service Bus connection string>`
- `LOGGING_PDPL_QUEUE=<queue name>`

Run it from the repo root:

```bash
LOGGING_PDPL_ASB_TEST_ENABLED=true ./gradlew integration \
  --tests uk.gov.hmcts.opal.logging.config.PdplQueueConnectivityIntegrationTest
```

For the full manual flow, see `docs/PO-2265-asb-manual-test.md`.

### Test-support endpoints (optional)
Enable the test-support REST endpoints (disabled by default):

- `OPAL_LOGGING_TEST_SUPPORT_ENABLED=true`

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
