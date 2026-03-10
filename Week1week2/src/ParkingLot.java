import java.util.*;

public class ParkingLot {

    // Parking spot status
    enum Status {
        EMPTY,
        OCCUPIED,
        DELETED
    }

    // Parking Spot structure
    static class ParkingSpot {
        String licensePlate;
        long entryTime;
        Status status;

        ParkingSpot() {
            status = Status.EMPTY;
        }
    }

    private ParkingSpot[] table;
    private int capacity = 500;
    private int size = 0;
    private int totalProbes = 0;
    private Map<Integer, Integer> hourlyTraffic = new HashMap<>();

    public ParkingLot() {
        table = new ParkingSpot[capacity];
        for (int i = 0; i < capacity; i++) {
            table[i] = new ParkingSpot();
        }
    }

    // Custom hash function
    private int hash(String licensePlate) {
        return Math.abs(licensePlate.hashCode()) % capacity;
    }

    // Linear probing
    public void parkVehicle(String licensePlate) {

        int index = hash(licensePlate);
        int probes = 0;

        while (table[index].status == Status.OCCUPIED) {
            index = (index + 1) % capacity;
            probes++;
        }

        table[index].licensePlate = licensePlate;
        table[index].entryTime = System.currentTimeMillis();
        table[index].status = Status.OCCUPIED;

        size++;
        totalProbes += probes;

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        hourlyTraffic.put(hour, hourlyTraffic.getOrDefault(hour, 0) + 1);

        System.out.println("Assigned spot #" + index + " (" + probes + " probes)");
    }

    // Find vehicle
    private int findVehicle(String licensePlate) {

        int index = hash(licensePlate);
        int start = index;

        while (table[index].status != Status.EMPTY) {

            if (table[index].status == Status.OCCUPIED &&
                    table[index].licensePlate.equals(licensePlate)) {
                return index;
            }

            index = (index + 1) % capacity;

            if (index == start) break;
        }

        return -1;
    }

    // Exit vehicle
    public void exitVehicle(String licensePlate) {

        int index = findVehicle(licensePlate);

        if (index == -1) {
            System.out.println("Vehicle not found");
            return;
        }

        long exitTime = System.currentTimeMillis();
        long durationMillis = exitTime - table[index].entryTime;

        double hours = durationMillis / (1000.0 * 60 * 60);

        double fee = hours * 5.5; // $5.5 per hour

        table[index].status = Status.DELETED;
        table[index].licensePlate = null;

        size--;

        System.out.printf(
                "Spot #%d freed, Duration: %.2f hours, Fee: $%.2f\n",
                index, hours, fee
        );
    }

    // Find nearest available spot from entrance
    public int findNearestSpot() {

        for (int i = 0; i < capacity; i++) {
            if (table[i].status != Status.OCCUPIED) {
                return i;
            }
        }

        return -1;
    }

    // Generate statistics
    public void getStatistics() {

        double occupancy = (size * 100.0) / capacity;

        double avgProbes = size == 0 ? 0 : (double) totalProbes / size;

        int peakHour = -1;
        int maxTraffic = 0;

        for (int hour : hourlyTraffic.keySet()) {
            if (hourlyTraffic.get(hour) > maxTraffic) {
                maxTraffic = hourlyTraffic.get(hour);
                peakHour = hour;
            }
        }

        System.out.println("Occupancy: " + String.format("%.2f", occupancy) + "%");
        System.out.println("Avg Probes: " + String.format("%.2f", avgProbes));

        if (peakHour != -1) {
            System.out.println("Peak Hour: " + peakHour + ":00 - " + (peakHour + 1) + ":00");
        }
    }

    // Demo
    public static void main(String[] args) {

        ParkingLot lot = new ParkingLot();

        lot.parkVehicle("ABC-1234");
        lot.parkVehicle("ABC-1235");
        lot.parkVehicle("XYZ-9999");

        lot.exitVehicle("ABC-1234");

        int nearest = lot.findNearestSpot();
        System.out.println("Nearest available spot: #" + nearest);

        lot.getStatistics();
    }
}
