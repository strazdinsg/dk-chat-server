package NtnuChatServer;

/**
 * Global count integer.
 */
public class GlobalCounter {
    private static int count = 1;

    /**
     * Return count and add +1 to count
     *
     * @return
     */
    public static int getNumber() {
        int returnValue = count;

        count++;

        return returnValue;
    }
}
