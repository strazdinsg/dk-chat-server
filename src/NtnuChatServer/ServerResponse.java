package NtnuChatServer;

/**
 * The servers response messages.
 */
public class ServerResponse {
    public static String MSG_ERR_PRIVMSG_UNAUTHORIZED = "msgerr unauthorized";
    public static String MSG_ERR_PRIVMSG_RECIPIENT = "msgerr incorrect recipient";
    public static String MSG_ERR = "cmderr command not supported";
    public static String MSG = "msg %s %s";
    public static String MSG_LOGIN_OK = "loginok";
    public static String MSG_LOGIN_ERR = "loginerr %s";
    public static String MSG_USERS = "users %s";
    public static String MSG_PRIVMSG = "privmsg %s %s";
    public static String MSG_SUPPORTED = "supported login msg privmsg users help";
}
