import java.util.*;

class Asset {
    String name;
    double returnRate;

    Asset(String name, double returnRate) {
        this.name = name;
        this.returnRate = returnRate;
    }

    public String toString() {
        return name + ":" + returnRate;
    }
}

public class Problem4 {

    public static void main(String[] args) {
        List<Asset> list = Arrays.asList(
                new Asset("AAPL", 12),
                new Asset("TSLA", 8),
                new Asset("GOOG", 15)
        );

        // Merge sort using Collections
        list.sort(Comparator.comparingDouble(a -> a.returnRate));
        System.out.println("Merge: " + list);

        // Quick style descending
        list.sort((a, b) -> Double.compare(b.returnRate, a.returnRate));
        System.out.println("Quick DESC: " + list);
    }
}