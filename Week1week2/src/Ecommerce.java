import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Ecommerce {

    // ProductId -> StockCount
    private ConcurrentHashMap<String, AtomicInteger> stockMap;

    // ProductId -> Waiting list of userIds (FIFO)
    private ConcurrentHashMap<String, LinkedHashMap<Integer, Long>> waitingListMap;

    public Ecommerce() {
        stockMap = new ConcurrentHashMap<>();
        waitingListMap = new ConcurrentHashMap<>();
    }

    // Add product to inventory
    public void addProduct(String productId, int stock) {
        stockMap.put(productId, new AtomicInteger(stock));
        waitingListMap.put(productId, new LinkedHashMap<>());
    }

    // Check stock availability
    public int checkStock(String productId) {
        AtomicInteger stock = stockMap.get(productId);
        return stock != null ? stock.get() : 0;
    }

    // Purchase product
    public String purchaseItem(String productId, int userId) {
        AtomicInteger stock = stockMap.get(productId);
        if (stock == null) {
            return "Product not found";
        }

        // Atomically decrement stock
        while (true) {
            int currentStock = stock.get();
            if (currentStock > 0) {
                if (stock.compareAndSet(currentStock, currentStock - 1)) {
                    return "Success, " + (currentStock - 1) + " units remaining";
                }
            } else {
                // Add to waiting list
                LinkedHashMap<Integer, Long> waitingList = waitingListMap.get(productId);
                synchronized (waitingList) {
                    if (!waitingList.containsKey(userId)) {
                        waitingList.put(userId, System.currentTimeMillis());
                        return "Added to waiting list, position #" + waitingList.size();
                    } else {
                        return "Already in waiting list, position #" + new ArrayList<>(waitingList.keySet()).indexOf(userId) + 1;
                    }
                }
            }
        }
    }

    // Get waiting list for a product
    public List<Integer> getWaitingList(String productId) {
        LinkedHashMap<Integer, Long> waitingList = waitingListMap.get(productId);
        if (waitingList == null) return Collections.emptyList();
        synchronized (waitingList) {
            return new ArrayList<>(waitingList.keySet());
        }
    }

    // Demo
    public static void main(String[] args) throws InterruptedException {

        Ecommerce manager = new Ecommerce();
        manager.addProduct("IPHONE15_256GB", 5); // limited stock for demo

        // Simulate multiple users purchasing concurrently
        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (int userId = 101; userId <= 110; userId++) {
            final int uid = userId;
            executor.submit(() -> {
                String result = manager.purchaseItem("IPHONE15_256GB", uid);
                System.out.println("User " + uid + ": " + result);
            });
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        System.out.println("Remaining stock: " + manager.checkStock("IPHONE15_256GB"));
        System.out.println("Waiting list: " + manager.getWaitingList("IPHONE15_256GB"));
    }
}