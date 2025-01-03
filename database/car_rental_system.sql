-- Create the database
CREATE DATABASE IF NOT EXISTS car_rental_system;
USE car_rental_system;

-- Create cars table
CREATE TABLE IF NOT EXISTS cars (
    car_id INT PRIMARY KEY AUTO_INCREMENT,
    car_name VARCHAR(100) NOT NULL,
    car_type VARCHAR(50) NOT NULL,
    rent_per_day DECIMAL(10, 2) NOT NULL,
    is_available BOOLEAN DEFAULT true,
    status VARCHAR(20) DEFAULT 'ACTIVE'
);

-- Create customers table
CREATE TABLE IF NOT EXISTS customers (
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL
);

-- Create rentals table
CREATE TABLE IF NOT EXISTS rentals (
    rental_id INT PRIMARY KEY AUTO_INCREMENT,
    car_id INT NOT NULL,
    customer_id INT NOT NULL,
    rental_date DATE NOT NULL,
    return_date DATE NOT NULL,
    total_amount DECIMAL(10, 2),
    FOREIGN KEY (car_id) REFERENCES cars(car_id),
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

-- Create rental_history table for logging
CREATE TABLE IF NOT EXISTS rental_history (
    history_id INT PRIMARY KEY AUTO_INCREMENT,
    rental_id INT,
    car_id INT,
    customer_id INT,
    rental_date DATE,
    return_date DATE,
    total_amount DECIMAL(10, 2),
    action_type VARCHAR(20),
    action_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create stored procedure for calculating rental amount
DELIMITER //
CREATE PROCEDURE CalculateRentalAmount(
    IN p_rental_id INT
)
BEGIN
    DECLARE v_rent_per_day DECIMAL(10, 2);
    DECLARE v_days INT;
    
    -- Calculate the number of days and total amount
    UPDATE rentals r
    JOIN cars c ON r.car_id = c.car_id
    SET r.total_amount = (
        DATEDIFF(r.return_date, r.rental_date) * c.rent_per_day
    )
    WHERE r.rental_id = p_rental_id;
END //
DELIMITER ;

-- Create trigger for rental history logging
DELIMITER //
CREATE TRIGGER after_rental_insert
AFTER INSERT ON rentals
FOR EACH ROW
BEGIN
    INSERT INTO rental_history (
        rental_id, car_id, customer_id, 
        rental_date, return_date, total_amount, 
        action_type
    )
    VALUES (
        NEW.rental_id, NEW.car_id, NEW.customer_id,
        NEW.rental_date, NEW.return_date, NEW.total_amount,
        'INSERT'
    );
END //
DELIMITER ;

-- Create stored procedure for removing a car
DELIMITER //
CREATE PROCEDURE RemoveCar(
    IN p_car_id INT,
    OUT p_success BOOLEAN,
    OUT p_message VARCHAR(100)
)
BEGIN
    DECLARE v_rental_count INT;
    
    -- Check if car has any active rentals
    SELECT COUNT(*) INTO v_rental_count
    FROM rentals
    WHERE car_id = p_car_id
    AND return_date >= CURRENT_DATE;
    
    IF v_rental_count > 0 THEN
        SET p_success = FALSE;
        SET p_message = 'Cannot remove car: Active rentals exist';
    ELSE
        DELETE FROM cars WHERE car_id = p_car_id;
        SET p_success = TRUE;
        SET p_message = 'Car removed successfully';
    END IF;
END //
DELIMITER ;

-- Create stored procedure to get active cars
DELIMITER //
CREATE PROCEDURE GetActiveCars()
BEGIN
    SELECT * FROM cars WHERE status = 'ACTIVE';
END //
DELIMITER ;
