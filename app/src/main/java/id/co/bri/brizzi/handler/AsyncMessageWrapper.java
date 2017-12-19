package id.co.bri.brizzi.handler;

import java.net.InetSocketAddress;

/**
 * Created by indra on 3/16/2016.
 */
public class AsyncMessageWrapper {

    public static final int cLoggingStaticHookObject = 0;
    private InetSocketAddress mDestination;
    private Object mMessage;

    public AsyncMessageWrapper(InetSocketAddress pDestination, Object pMessage) {
        this.mDestination = pDestination;
        this.mMessage = pMessage;
    }

    public AsyncMessageWrapper(String pDestIP, int pDestPort, Object pMessage) {
        this.mDestination = new InetSocketAddress(pDestIP, pDestPort);
        this.mMessage = pMessage;
    }

    public InetSocketAddress getDestination() {
        return mDestination;
    }

    public Object getMessage() {
        return mMessage;
    }

    public String getMessageStream() {
        return mMessage.toString();
    }


    @Override
    public String toString() {
        return "[" + mMessage + "] to [" + mDestination + "]";
    }
}
