package NtnuChatServer;

/**
 * Global count integer.
 */
public class GlobalCounter {
    private static int count = 0;
    
    /**
     * Return count and add +1 to count
     */
    public static int getNumber(){
        int returnValue = count;
        
        count++;
        
        return returnValue;
    }
}
