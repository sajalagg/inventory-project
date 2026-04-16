# Cloud Kitchen Inventory Management System

A Java 17 + JavaFX + SQLite project for managing cloud-kitchen inventory, menu availability, customer ordering, and admin operations in one desktop application.

The system follows a layered architecture and demonstrates how inventory control, role-based access, and persistent data storage can be combined into a polished OOSD college project.

## Highlights

- layered backend with clear domain, repository, service, and UI boundaries
- SQLite persistence with schema initialization and seed data
- login-first flow with admin and customer portals
- customer self-registration from the welcome screen
- inventory-aware order processing with transactional stock deduction
- JavaFX desktop UI for both guest and admin workflows
- admin controls for adding/removing ingredients and dishes
- automatic out-of-stock handling when recipe requirements cannot be met

## User roles

### Customer portal

- login as a customer and browse the live premium menu
- place orders only for dishes that are currently serviceable
- view only personal order history
- no access to revenue, stock counts, ingredient-level operations, or global analytics

### Admin portal

- login as admin and view operational dashboard metrics
- monitor low-stock ingredients
- see all customer orders
- restock ingredients
- add new ingredients to the inventory
- remove ingredients from active use
- add new dishes with recipe mapping
- remove dishes from the live customer menu

## Project structure

- `src/main/java/com/cloudkitchen/model` domain models and computed availability objects
- `src/main/java/com/cloudkitchen/repository` JDBC repository layer
- `src/main/java/com/cloudkitchen/service` business logic and transaction handling
- `src/main/java/com/cloudkitchen/db` database bootstrap and schema initialization
- `src/main/java/com/cloudkitchen/ui` JavaFX application
- `src/main/resources/com/cloudkitchen/ui/app.css` UI styling
- `data/cloud_kitchen.db` SQLite database file created automatically on first run

## Core modules

- `model`
  Contains entities such as `Ingredient`, `Dish`, `RecipeItem`, `User`, `OrderRecord`, and availability/session models.

- `repository`
  Contains JDBC-based repository interfaces and SQLite implementations for users, ingredients, dishes, recipes, and orders.

- `service`
  Contains business logic for authentication, inventory operations, dish availability, and transactional order placement.

- `db`
  Handles connection setup, schema creation, seed data, and lightweight migration support.

- `ui`
  Contains the JavaFX application for login, registration, customer menu flow, and admin control panels.

## Run locally

1. Download runtime dependencies:

   `./scripts/setup_dependencies.sh`

2. Build and launch:

   `./scripts/run_app.sh`

The application creates `data/cloud_kitchen.db` automatically on first run.

## Demo login credentials

- Admin portal: `admin / admin123`
- Customer portal 1: `aarav / cust123`
- Customer portal 2: `diya / cust123`

Use the matching portal button on the login screen.

## New customer registration

New customer logins can be created directly from the login screen using:

- full name
- username
- password

Those accounts are stored in SQLite and can immediately use the customer portal.

## Features included

- login page with portal selection
- customer registration page
- admin can see all customers' order history
- customer can see only personal order history
- live ingredient inventory view
- low-stock watchlist
- dish availability based on recipe requirements
- order placement with stock deduction
- recent order history and revenue tracking
- restocking workflow for admins
- add/remove ingredient workflow for admins
- add/remove dish workflow for admins
- recipe-aware menu management
- persistent order history with customer linkage

## Dish availability logic

- every dish is mapped to required ingredients through recipe items
- a dish is available only when all required ingredients have sufficient stock
- if a required ingredient is missing, inactive, or insufficient, the dish becomes unavailable
- this happens automatically without manual admin toggling

## Verification

The project includes a smoke test that verifies:

- customer registration
- customer login
- order placement
- customer-specific order history
- admin-wide order visibility
- ingredient creation/removal
- dish creation/removal
- out-of-stock behavior caused by insufficient ingredient quantity

Run it with:

```zsh
mkdir -p build/classes
find build/classes -type f -delete
javac --class-path 'lib/*' -d build/classes $(find src/main/java -name '*.java')
java --class-path 'build/classes:src/main/resources:lib/*' com.cloudkitchen.app.SmokeTest
```

## Technology stack

- Java 17
- JavaFX
- SQLite
- JDBC

## Current scope

- desktop application only
- local SQLite persistence
- single-machine usage
- no networked multi-user concurrency

## Future improvements

- password hashing instead of plain-text demo passwords
- customer profile management
- dish editing instead of add/remove only
- ingredient editing and threshold updates
- analytics dashboards and reports
- invoice generation
- QR-based ordering or web/mobile extension
