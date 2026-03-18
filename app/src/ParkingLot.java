import java.util.*;

class ParkingSpot {
    String licensePlate;
    long entryTime;
    boolean occupied;

    ParkingSpot() {
        this.licensePlate = null;
        this.entryTime = 0;
        this.occupied = false;
    }
}

public class ParkingLot {
    private final ParkingSpot[] spots;
    private final int capacity;
    private int occupiedCount = 0;
    private int totalProbes = 0;
    private int totalParkings = 0;
    private final Map<Integer, Integer> hourlyOccupancy = new HashMap<>();

    public ParkingLot(int capacity) {
        this.capacity = capacity;
        this.spots = new ParkingSpot[capacity];
        for (int i = 0; i < capacity; i++) {
            spots[i] = new ParkingSpot();
        }
    }

    private int hash(String licensePlate) {
        return Math.abs(licensePlate.hashCode()) % capacity;
    }

    public String parkVehicle(String licensePlate) {
        int preferred = hash(licensePlate);
        int probes = 0;
        for (int i = 0; i < capacity; i++) {
            int idx = (preferred + i) % capacity;
            if (!spots[idx].occupied) {
                spots[idx].licensePlate = licensePlate;
                spots[idx].entryTime = System.currentTimeMillis();
                spots[idx].occupied = true;
                occupiedCount++;
                totalProbes += probes;
                totalParkings++;
                updateHourlyStats();
                return "Assigned spot #" + idx + " (" + probes + " probes)";
            }
            probes++;
        }
        return "Parking Lot Full";
    }

    public String exitVehicle(String licensePlate) {
        for (int i = 0; i < capacity; i++) {
            if (spots[i].occupied && spots[i].licensePlate.equals(licensePlate)) {
                long durationMs = System.currentTimeMillis() - spots[i].entryTime;
                double hours = durationMs / (1000.0 * 60 * 60);
                double fee = hours * 5.0; // $5 per hour
                spots[i].occupied = false;
                spots[i].licensePlate = null;
                occupiedCount--;
                return "Spot #" + i + " freed, Duration: " + String.format("%.2f", hours) + "h, Fee: $" + String.format("%.2f", fee);
            }
        }
        return "Vehicle not found";
    }

    private void updateHourlyStats() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        hourlyOccupancy.put(hour, hourlyOccupancy.getOrDefault(hour, 0) + 1);
    }

    public String getStatistics() {
        double occupancyRate = (occupiedCount * 100.0) / capacity;
        double avgProbes = totalParkings == 0 ? 0 : (totalProbes * 1.0 / totalParkings);

        int peakHour = -1, max = 0;
        for (Map.Entry<Integer, Integer> entry : hourlyOccupancy.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                peakHour = entry.getKey();
            }
        }

        return "Occupancy: " + String.format("%.1f", occupancyRate) + "%, Avg Probes: " +
                String.format("%.2f", avgProbes) + ", Peak Hour: " + peakHour + ":00";
    }

    public static void main(String[] args) throws InterruptedException {
        ParkingLot lot = new ParkingLot(500);

        System.out.println(lot.parkVehicle("ABC-1234"));
        System.out.println(lot.parkVehicle("ABC-1235"));
        System.out.println(lot.parkVehicle("XYZ-9999"));

        Thread.sleep(2000); // simulate time passing
        System.out.println(lot.exitVehicle("ABC-1234"));

        System.out.println(lot.getStatistics());
    }
}