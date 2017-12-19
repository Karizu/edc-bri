package id.co.bri.brizzi.handler;

/**
 * Created by indra on 3/16/2016.
 */
public class AsyncMessageSender {

    public static final int cLoggingStaticHookObject = 0;
    public final static byte cEndMessageByte = -0x01;
    public static String cModuleNameSpace = "async-message-sender";
    //    public static String cThreadCountConfigKey = cModuleNameSpace + SystemConfig.getSeparatorString() + "maxthreads";
    public static String cQueueSizeKey = "request-queue";

    public AsyncMessageSender() {
//        SystemLog.getSingleton().log(this, LogType.TRACE, "init async message sender " + this);
    }

    private static void logTrace(String pMessage) {
//        SystemLog.getSingleton().log(cLoggingStaticHookObject, LogType.TRACE, pMessage);
    }


}
