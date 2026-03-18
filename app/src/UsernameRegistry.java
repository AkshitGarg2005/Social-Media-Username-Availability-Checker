import java.util.*;

public class UsernameRegistry {
    // HashMap for instant O(1) lookup of usernames
    private Map<String, Integer> usernameMap;
    // HashMap for tracking attempted username frequency
    private Map<String, Integer> attemptFrequency;

    public UsernameRegistry() {
        usernameMap = new HashMap<>();
        attemptFrequency = new HashMap<>();
    }

    // Register a username with a userId
    public boolean register(String username, int userId) {
        if (usernameMap.containsKey(username)) {
            return false; // already taken
        }
        usernameMap.put(username, userId);
        return true;
    }

    // Check availability in O(1)
    public boolean checkAvailability(String username) {
        // Track attempts
        attemptFrequency.put(username, attemptFrequency.getOrDefault(username, 0) + 1);
        return !usernameMap.containsKey(username);
    }

    // Suggest alternatives if taken
    public List<String> suggestAlternatives(String username) {
        List<String> suggestions = new ArrayList<>();
        if (usernameMap.containsKey(username)) {
            // Append numbers
            for (int i = 1; i <= 3; i++) {
                String alt = username + i;
                if (!usernameMap.containsKey(alt)) {
                    suggestions.add(alt);
                }
            }
            // Replace underscore with dot
            String modified = username.replace("_", ".");
            if (!usernameMap.containsKey(modified)) {
                suggestions.add(modified);
            }
        }
        return suggestions;
    }

    // Get most attempted username
    public String getMostAttempted() {
        String mostAttempted = null;
        int maxAttempts = 0;
        for (Map.Entry<String, Integer> entry : attemptFrequency.entrySet()) {
            if (entry.getValue() > maxAttempts) {
                maxAttempts = entry.getValue();
                mostAttempted = entry.getKey();
            }
        }
        return mostAttempted + " (" + maxAttempts + " attempts)";
    }

    // Demo
    public static void main(String[] args) {
        UsernameRegistry registry = new UsernameRegistry();

        // Register some usernames
        registry.register("john_doe", 1);
        registry.register("admin", 2);

        // Check availability
        System.out.println("checkAvailability(\"john_doe\") → " + registry.checkAvailability("john_doe"));
        System.out.println("checkAvailability(\"jane_smith\") → " + registry.checkAvailability("jane_smith"));

        // Suggest alternatives
        System.out.println("suggestAlternatives(\"john_doe\") → " + registry.suggestAlternatives("john_doe"));

        // Track attempts
        registry.checkAvailability("admin");
        registry.checkAvailability("admin");
        registry.checkAvailability("admin");

        System.out.println("getMostAttempted() → " + registry.getMostAttempted());
    }
}