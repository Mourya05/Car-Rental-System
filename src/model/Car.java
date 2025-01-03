package model;

public class Car {
    private int carId;
    private String carName;
    private String carType;
    private double rentPerDay;
    private boolean isAvailable;

    public Car(int carId, String carName, String carType, double rentPerDay, boolean isAvailable) {
        this.carId = carId;
        this.carName = carName;
        this.carType = carType;
        this.rentPerDay = rentPerDay;
        this.isAvailable = isAvailable;
    }

    // Getters and Setters
    public int getCarId() { return carId; }
    public void setCarId(int carId) { this.carId = carId; }
    
    public String getCarName() { return carName; }
    public void setCarName(String carName) { this.carName = carName; }
    
    public String getCarType() { return carType; }
    public void setCarType(String carType) { this.carType = carType; }
    
    public double getRentPerDay() { return rentPerDay; }
    public void setRentPerDay(double rentPerDay) { this.rentPerDay = rentPerDay; }
    
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
}
