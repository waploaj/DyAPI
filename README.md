# Dynamic API Handler Framework

![License](https://img.shields.io/badge/license-MIT-blue.svg)

## Overview

This project implements a **Dynamic API Handler Framework** that allows the configuration of APIs via database entries, dynamically handling request parameters, and invoking corresponding Controller (`ctl`) and Business Object (`bo`) classes and methods as defined in an XML configuration. The design avoids hardcoding APIs and allows for dynamic mapping and invocation based on API configuration stored in database tables.

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Folder Structure](#folder-structure)
- [Database Tables](#database-tables)
- [Key Components](#key-components)
- [API Flow](#api-flow)
- [Debugging](#debugging)
- [Setup and Configuration](#setup-and-configuration)
- [Usage](#usage)
- [License](#license)

---

## Architecture Overview

This framework provides a decoupled structure for handling API requests. The API logic is defined in the database and loaded dynamically during runtime. The architecture consists of:

- **procctlcfg**: Contains the mapping of API codes to Controller and BO classes/methods.
- **procctlmpg**: Maintains information on request parameters (e.g., mandatory fields, validation).
- **lookup.xml**: Maintains the mapping of API codes to BO classes and methods.
- **ApiHandler**: A servlet that handles the incoming API request, fetching configuration from the database, validating parameters, and invoking the correct controller and BO classes.

### Design Pattern: Dynamic API Invocation with XML Configuration

The design follows a **dynamic routing pattern** where the controller and BO logic are dynamically determined at runtime based on the database and XML configuration.

---

## Folder Structure

├── src │ ├── main │ │ ├── java │ │ │ ├── com.yourcompany.yourapp │ │ │ │ ├── ctl │ │ │ │ ├── bo │ │ │ │ ├── service │ │ │ │ ├── utility │ │ ├── resources │ │ │ ├── lookup.xml


- **ctl/**: Contains all Controller classes (e.g., `BalanceCtl`).
- **bo/**: Contains Business Object classes that implement core business logic.
- **service/**: Contains the API handling logic (`ApiHandler.java`).
- **utility/**: Contains utility classes (e.g., `UtilityFunctions`, `ApiDebug`).
- **resources/**: Contains the `lookup.xml` configuration file for mapping API codes to BO methods.

---

## Database Tables

### procctlcfg

This table maps API codes to the corresponding Controller (`ctl`) and Business Object (`bo`) classes.

| api_code   | className                            | methodName    |
|------------|--------------------------------------|---------------|
| 10001      | com.yourcompany.yourapp.ctl.BalanceCtl | processBalance|
| 10002      | com.yourcompany.yourapp.ctl.PaymentCtl | processPayment|

### procctlmpg

This table maintains information about request parameters.

| api_code | paramName  | isMandatory | validationMethod  |
|----------|------------|-------------|-------------------|
| 10001    | accountId  | Y           | validateAccountId |
| 10002    | amount     | Y           | validateAmount    |

---

## Key Components

### ApiHandler Class

The `ApiHandler` class is responsible for handling all API requests. It:

1. Extracts the API code from the URL.
2. Fetches API configuration (class name and method) from the database using `UtilityFunctions.fetchApiConfig()`.
3. Validates request parameters based on `procctlmpg` configuration.
4. Invokes the correct Controller class dynamically.
5. Passes the validated request parameters to the BO classes, invoking methods as defined in `lookup.xml`.

### UtilityFunctions Class

The `UtilityFunctions` class provides helper functions for:

- Fetching API configuration from the database.
- Extracting and validating request parameters.
- Dynamically invoking methods using reflection based on the configuration in `lookup.xml`.

### ApiDebug Class

The `ApiDebug` class is a custom logger that supports different log levels (`DEBUG`, `INFO`, `WARN`, `ERROR`) and can log either to the console or to a file. This class can be used for logging debug information across the application.

Usage:
```java
ApiDebug.log("This is a debug message");
ApiDebug.error("This is an error message");
```

# API Flow

### API Request
- Incoming requests will contain an `api_code` to identify which controller (`ctl`) and BO or Interface methods to invoke.

### Parameter Extraction
- Parameters will be extracted and validated based on the `procctlmpg` table.

### Interface Layer Invocation
- The correct class and method in the Interface Layer are fetched from the `procctlcfg` table and invoked using reflection.

### Data Processing
- Data collected from channel devices (e.g., sensor data) will be processed via the Interface Layer.

### Core Interaction
- The Interface Layer will handle requests to/from the core, ensuring no direct communication between the core and channel devices.

### Response Generation
- A response is generated based on the processed data and sent back to the caller.

---

# Device-Specific Integrations

### iOS Devices
- **Integration**: Swift will be used to communicate with iOS devices, leveraging native APIs for accessing sensors such as GPS and accelerometers.
- **Data Collection**: Data will be collected at predefined intervals and validated through the Interface Layer.
- **Efficiency**: The Interface Layer will manage device sleep and awake times for optimal power usage during data collection.

### Android Devices
- **Integration**: Kotlin and Java will be used to communicate with Android devices, accessing sensors and collecting data at configurable intervals.
- **Proximity Detection**: Android devices will synchronize through the Interface Layer to ensure the most accurate and relevant data is collected.
- **Sensor Management**: Sensors will have configurable time intervals for data collection, minimizing resource consumption.

---

# Interface Layer

The Interface Layer acts as middleware between the core system and channel devices. Its main purpose is to decouple direct communication between the core system and channels like iOS and Android, ensuring that requests are properly routed and responses are handled efficiently.

### Key Functions:
- **Request Mediation**: All API requests from channel devices pass through the Interface Layer before reaching the core system.
- **Response Handling**: The Interface Layer manages responses from the core and delivers them back to the respective channel.
- **Core Decoupling**: By preventing direct interaction between channels and core systems, the Interface Layer ensures clean separation and flexibility.

### Example Flow:
1. An API request is received from an Android device.
2. The request is passed to the Interface


