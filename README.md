# Car Rental System

A Java-based Car Rental System with MySQL database integration and Swing GUI.

## Features

1. Add new cars to the system
2. Book cars for customers
3. View available cars
4. Generate rental bills
5. Track customer information
6. Calculate rental duration and total rent

## Prerequisites

1. Java Development Kit (JDK) 8 or higher
2. MySQL Server 5.7 or higher
3. MySQL Connector/J (JDBC driver)

## Setup Instructions

1. **Database Setup**
   - Install MySQL if not already installed
   - Open MySQL command line or workbench
   - Navigate to the `database` folder
   - Run the `car_rental_system.sql` script to create the database and tables

2. **Configure Database Connection**
   - Open `src/database/DatabaseConnection.java`
   - Update the following constants if needed:
     - URL (default: "jdbc:mysql://localhost:3306/car_rental_system")
     - USERNAME (default: "root")
     - PASSWORD (your MySQL password)

3. **Compile and Run**
   - Compile all Java files
   - Run the MainMenu class to start the application

## Project Structure

```
CarRentalSystem/
├── src/
│   ├── database/
│   │   └── DatabaseConnection.java
│   ├── gui/
│   │   ├── MainMenu.java
│   │   ├── AddCarFrame.java
│   │   ├── BookCarFrame.java
│   │   ├── ViewCarsFrame.java
│   │   └── GenerateBillFrame.java
│   └── model/
│       ├── Car.java
│       ├── Customer.java
│       └── Rental.java
├── database/
│   └── car_rental_system.sql
└── README.md
```

## Usage

1. **Add a New Car**
   - Click "Add New Car" button
   - Enter car details (name, type, rent per day)
   - Click Submit

2. **Book a Car**
   - Click "Book a Car" button
   - Select a car from the dropdown
   - Enter customer details
   - Enter rental and return dates
   - Click Submit

3. **View Cars**
   - Click "View Available Cars" button
   - See all cars in the system with their details

4. **Generate Bill**
   - Click "Generate Bill" button
   - Select a rental from the dropdown
   - Click Generate to see the bill details

## Dependencies

- MySQL Connector/J (JDBC driver)
- Java Swing (included in JDK)

## Note

Make sure to have MySQL running and properly configured before starting the application.
