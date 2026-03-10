import java.util.*;

public class SocialMediaUserName {

    // username -> userId
    private HashMap<String, Integer> users;

    // username -> attempt frequency
    private HashMap<String, Integer> attempts;

    public SocialMediaUserName () {
        users = new HashMap<>();
        attempts = new HashMap<>();
    }

    // Register an existing user
    public void registerUser(String username, int userId) {
        users.put(username, userId);
    }

    // Check if username is available
    public boolean checkAvailability(String username) {
        // track attempts
        attempts.put(username, attempts.getOrDefault(username, 0) + 1);

        // O(1) lookup
        return !users.containsKey(username);
    }

    // Suggest alternative usernames
    public List<String> suggestAlternatives(String username) {
        List<String> suggestions = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            String suggestion = username + i;
            if (!users.containsKey(suggestion)) {
                suggestions.add(suggestion);
            }
        }

        String modified = username.replace("_", ".");
        if (!users.containsKey(modified)) {
            suggestions.add(modified);
        }

        return suggestions;
    }

    // Find most attempted username
    public String getMostAttempted() {
        String mostAttempted = "";
        int max = 0;

        for (Map.Entry<String, Integer> entry : attempts.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                mostAttempted = entry.getKey();
            }
        }

        return mostAttempted + " (" + max + " attempts)";
    }

    public static void main(String[] args) {

        SocialMediaUserName checker = new SocialMediaUserName();

        // Existing users
        checker.registerUser("john_doe", 101);
        checker.registerUser("admin", 102);

        System.out.println("john_doe available? " + checker.checkAvailability("john_doe"));
        System.out.println("jane_smith available? " + checker.checkAvailability("jane_smith"));

        System.out.println("Suggestions for john_doe: " + checker.suggestAlternatives("john_doe"));

        // simulate attempts
        for (int i = 0; i < 5; i++) {
            checker.checkAvailability("admin");
        }

        System.out.println("Most attempted username: " + checker.getMostAttempted());
    }
}