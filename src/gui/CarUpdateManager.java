package gui;

import java.util.ArrayList;
import java.util.List;

public class CarUpdateManager {
    private static CarUpdateManager instance;
    private List<CarUpdateListener> listeners;

    private CarUpdateManager() {
        listeners = new ArrayList<>();
    }

    public static CarUpdateManager getInstance() {
        if (instance == null) {
            instance = new CarUpdateManager();
        }
        return instance;
    }

    public void addListener(CarUpdateListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(CarUpdateListener listener) {
        listeners.remove(listener);
    }

    public void notifyCarUpdate() {
        for (CarUpdateListener listener : listeners) {
            listener.onCarUpdate();
        }
    }
}
