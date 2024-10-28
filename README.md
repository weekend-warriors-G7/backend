
# Weekend Warriors Backend

This repository contains the backend for the Weekend Warriors project, built with Java Spring Boot and Dockerized for easy deployment. The Makefile provided automates key tasks, including cleaning, building, and running the Docker container.

## Prerequisites

- **Java**: Ensure Java is installed for building the project.
- **Maven**: Required for building the Java project. The Makefile uses Maven Wrapper (`mvnw`) to handle this.
- **Docker**: Docker must be installed to build and run the containerized application.

## Makefile Targets

The Makefile automates the following tasks:

### `make clean`

Cleans up any previous build files.

```bash
make clean
```

This command:
- Runs the Maven `clean` command to remove previous build files.

### `make build`

Cleans and then builds the project.

```bash
make build
```

This command:
- Runs `make clean`.
- Compiles the project using Maven, producing a `.jar` file in the `target` directory.

### `make docker_build`

Builds the Docker image for the backend.

```bash
make docker_build
```

This command:
- Runs `make build` to ensure the project is compiled.
- Builds a Docker image named `gabjea/weekend-warriors-backend:latest`.

### `make up_local`

Stops any existing container named `backend`, removes it, and then builds and runs the Docker container.

```bash
make up_local
```

This command:
- Runs `make down_local` to stop any running container named `backend`.
- Builds the Docker image if needed (`make docker_build`).
- Starts a new container in detached mode (`-d`) with the name `backend`, exposing it on port `8080`.

### `make down_local`

Stops and removes the running `backend` container, if it exists.

```bash
make down_local
```

This command:
- Runs `make docker_stop` to stop a running container named `backend`.
- Removes the `backend` container.

### `make docker_stop`

Stops the running `backend` container, if it exists.

```bash
make docker_stop
```

This command:
- Attempts to stop a running container named `backend`, ignoring errors if it’s not running.

## Workflow

To clean, build, and run the backend service with Docker, run the following commands in order:

1. **Run the container**:
   ```bash
   make up_local
   ```

2. **Stop and remove the container**:
   ```bash
   make down_local
   ```

## Additional Notes

- The Docker container is set to run in detached mode (`-d`), so it will run in the background.
- The `docker_stop` and `down_local` targets use `|| true` to ignore errors, which helps avoid issues when the container isn’t running or doesn’t exist.

This Makefile helps automate the process of cleaning, building, Dockerizing, and running the backend application, making development and deployment more streamlined.
