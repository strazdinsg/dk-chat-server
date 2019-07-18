package NtnuChatServer;

/**
 * The servers response messages.
 */
public class ServerResponse {
    public static String MSG_ERR_PRIVMSG_UNAUTHORIZED = "msgerr unauthorized\n";
    public static String MSG_ERR_PRIVMSG_RECIPIENT = "msgerr incorrect recipient\n";
    public static String MSG_ERR = "cmderr command not supported\n";
    public static String MSG = "msg %s %s\n";
    public static String MSG_LOGIN_OK = "loginok\n";
    public static String MSG_LOGIN_ERR = "loginerr %s\n";
    public static String MSG_USERS = "users %s\n";
    public static String MSG_PRIVMSG = "privmsg %s %s\n";
    public static String MSG_SUPPORTED = "msg privmsg users help\n";
}
