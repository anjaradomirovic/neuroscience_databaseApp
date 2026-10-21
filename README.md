# Neuroscience Research Database App

A desktop application for managing the daily work of a neuroscience research facility: laboratories, equipment, resources, experiments, their executions and scheduled sessions. Built with JavaFX on top of a relational MySQL database, with much of the business logic implemented in the database itself.

> The user interface is in Serbian.

![Sessions calendar view](screenshots/sessions.png)
*Sessions calendar with filtering by laboratory and experiment.*

## Features

- **Sessions:** calendar view with filtering by laboratory and experiment, scheduling and editing sessions
- **Laboratories:** create, edit and delete laboratories
- **Resources:** per-laboratory stock levels, replenishing and consuming stock, and a list of laboratories waiting on missing resources
- **Equipment:** equipment grouped by type, status changes and registration of new equipment with calibration data
- **Experiments:** catalogue with required resources and equipment, designers, theoretical frameworks and ethics approval status
- **Executions:** tracking each run of an experiment from planned to finished (successful or unsuccessful)
- **Statistics:** success rates per experiment and completed executions per laboratory
- **Authentication:** sign-up and log-in screens

## Tech Stack

Java 20 · JavaFX 20 · MySQL (JDBC) · Maven

## Database

The application uses a MySQL schema named `neuronauka`, with:

- **Tables:** experiments, executions, sessions, laboratories, resources, equipment, researchers, designers and theoretical frameworks
- **Stored procedures:** stock updates, equipment management and resource replenishment
- **Functions:** experiment success rate and completed executions per laboratory
- **Views:** laboratories waiting for resources

Multi-table operations, such as creating an experiment together with its resources, equipment and designers, run inside a single transaction with rollback on failure.

> **Note:** The database is currently unavailable, so the application cannot be run end to end at the moment.

## Author

**Anja Radomirović**: [GitHub](https://github.com/anjaradomirovic)
