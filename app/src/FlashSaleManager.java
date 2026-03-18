import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.*;

public class FlashSaleManager {
    private final ConcurrentHashMap<String, AtomicInteger> stock = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<Integer>> waitingList = new ConcurrentHashMap<>();

    public void addProduct(String productId, int initialStock) {
        stock.put(productId, new AtomicInteger(initialStock));
        waitingList.put(productId, new ConcurrentLinkedQueue<>());
    }

    public String checkStock(String productId) {
        AtomicInteger currentStock = stock.get(productId);
        if (currentStock == null) return "Product not found";
        return currentStock.get() + " units available";
    }

    public String purchaseItem(String productId, int userId) {
        AtomicInteger currentStock = stock.get(productId);
        if (currentStock == null) return "Product not found";

        int remaining = currentStock.getAndUpdate(val -> val > 0 ? val - 1 : val);
        if (remaining > 0) {
            return "Success, " + (remaining - 1) + " units remaining";
        } else {
            waitingList.get(productId).add(userId);
            return "Added to waiting list, position #" + waitingList.get(productId).size();
        }
    }

    public void restock(String productId, int additionalStock) {
        AtomicInteger currentStock = stock.get(productId);
        if (currentStock == null) return;

        currentStock.addAndGet(additionalStock);

        Queue<Integer> queue = waitingList.get(productId);
        while (!queue.isEmpty() && currentStock.get() > 0) {
            int userId = queue.poll();
            currentStock.decrementAndGet();
            System.out.println("User " + userId + " from waiting list got the item. Remaining stock: " + currentStock.get());
        }
    }

    public static void main(String[] args) throws InterruptedException {
        FlashSaleManager manager = new FlashSaleManager();
        manager.addProduct("IPHONE15_256GB", 100);

        ExecutorService executor = Executors.newFixedThreadPool(50);
        for (int i = 1; i <= 200; i++) {
            final int userId = i;
            executor.submit(() -> {
                String result = manager.purchaseItem("IPHONE15_256GB", userId);
                System.out.println("User " + userId + ": " + result);
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        System.out.println(manager.checkStock("IPHONE15_256GB"));
        manager.restock("IPHONE15_256GB", 10);
    }
}