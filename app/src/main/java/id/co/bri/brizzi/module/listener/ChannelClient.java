
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.bri.brizzi.module.listener;

/**
 * @author Ahmad
 */

import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class ChannelClient implements Runnable {
    // The host:port combination to connect to
    private InetAddress hostAddress;
    private int port;

    // The selector we'll be monitoring
    private Selector selector;

    // The channel on which we'll accept connections
    private SocketChannel channel;

    private ByteBuffer readBuffer = ByteBuffer.allocate(8192);

    // A list of ChangeRequest instances
    private List changeRequests = new LinkedList();

    // Maps a SocketChannel to a list of ByteBuffer instances
    private Map pendingData = new HashMap();
    private Boolean running = false;
    private int CONNTIMEOUT = 60000;
    private byte[] reply;
    private int stage;

    public ChannelClient(InetAddress hostAddress, int port) throws IOException {
        this.hostAddress = hostAddress;
        this.port = port;
        this.selector = this.initSelector();
        this.stage = 0;
    }

    public int checkStage() {
        return stage;
    }

    public byte[] getReply() {
        return reply;
    }

    private Selector initSelector() throws IOException {
        // Create a new selector
        Selector socketSelector = Selector.open();

        // Create a new non-blocking socket channel
        this.channel = SocketChannel.open();
        configureChannel(channel);

        // Connect the socket channel to the specified address and port
        InetSocketAddress isa = new InetSocketAddress(this.hostAddress, this.port);
//        Log.d("SOCKET_C", "Server Address : " + this.hostAddress + ":" + String.valueOf(this.port));
        channel.configureBlocking(true);
        try {
            channel.socket().connect(isa, CONNTIMEOUT);
        } catch (Exception e) {
            if (channel!=null) {
                if (channel.socket()!=null) {
                    channel.socket().close();
                }
                channel.close();
            }
            running = false;
            e.printStackTrace();
            return null;
        }
        channel.configureBlocking(false);
        socketSelector.wakeup();
        // Register the socket channel, indicating an interest in
        channel.register(socketSelector, SelectionKey.OP_CONNECT);
        synchronized (this.changeRequests) {
            // Indicate we want the interest ops set changed
            this.changeRequests.add(new ChangeRequest(channel, ChangeRequest.REGISTER, SelectionKey.OP_CONNECT));
//            this.changeRequests.add(new ChangeRequest(channel, ChangeRequest.CHANGEOPS, SelectionKey.OP_READ));
        }
        synchronized (this.changeRequests) {
            // Indicate we want the interest ops set changed
//            this.changeRequests.add(new ChangeRequest(channel, ChangeRequest.REGISTER, SelectionKey.OP_CONNECT));
            this.changeRequests.add(new ChangeRequest(channel, ChangeRequest.CHANGEOPS, SelectionKey.OP_READ));
        }

        return socketSelector;
    }

    private void configureChannel(SocketChannel channel) throws IOException {
        channel.configureBlocking(false);
        channel.socket().setSendBufferSize(0x100000); // 1Mb
        channel.socket().setReceiveBufferSize(0x100000); // 1Mb
        channel.socket().setKeepAlive(true);
        channel.socket().setReuseAddress(true);
        channel.socket().setSoLinger(false, 0);
        channel.socket().setSoTimeout(60);
        channel.socket().setTcpNoDelay(true);
    }

    public void send(SocketChannel socket, byte[] data) {
        reply = null;
        stage = 1;
        synchronized (this.changeRequests) {
            // Indicate we want the interest ops set changed
            this.changeRequests.add(new ChangeRequest(socket, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));

            // And queue the data we want written
            synchronized (this.pendingData) {
                List queue = (List) this.pendingData.get(socket);
                if (queue == null) {
                    queue = new ArrayList();
                    this.pendingData.put(socket, queue);
                }
                queue.add(ByteBuffer.wrap(data));
            }
        }

        // Finally, wake up our selecting thread so it can make the required changes
        this.selector.wakeup();
    }

    public boolean isConnected() {
        return running;
    }

    public Selector getSelector() {
        return selector;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            try {
                // Process any pending changes
                synchronized (this.changeRequests) {
                    Iterator changes = this.changeRequests.iterator();
                    while (changes.hasNext()) {
                        ChangeRequest change = (ChangeRequest) changes.next();
                        switch (change.type) {
                            case ChangeRequest.CHANGEOPS:
                                SelectionKey key = change.socket.keyFor(this.selector);
                                key.interestOps(change.ops);
                                if (change.ops == SelectionKey.OP_READ) {
                                    //container.writecLog("read");
//                                    Log.i("SOCKET_SYNC", "READ");
                                } else {
                                    //container.writecLog("write");
//                                    Log.i("SOCKET_SYNC", "WRITE");
                                }
                                break;
                            case ChangeRequest.REGISTER:
                                change.socket.register(this.selector, change.ops);
                                if (change.socket.finishConnect()) {
                                    //container.writecLog("connect");
//                                    Log.i("SOCKET_SYNC", "CONNECT");
                                }
                                break;
                        }
                    }
                    this.changeRequests.clear();
                }

                // Wait for an event one of the registered channels
                int selectorStatus = 0;
                selectorStatus = this.selector.select();
//                Log.i("SELECTOR STATUS", String.valueOf(selectorStatus));

                // Iterate over the set of keys for which events are available
//                Log.i("SELECTOR SIZE", String.valueOf(this.selector.selectedKeys().size()));
                Iterator selectedKeys = this.selector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    SelectionKey key = (SelectionKey) selectedKeys.next();
                    selectedKeys.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    // Check what event is available and deal with it
                    if (key.isReadable()) {
                        this.read(key);
                    } else if (key.isWritable()) {
                        this.write(key);
                    }
                }
            } catch (Exception e) {
                //log.info(e);
                //reconnect without notice
                //comment next line to auto reconnect
                running = false;
                //log.info("Cannot connect to server : "+e.toString());
//                Log.i("SOCKET_SYNC", "Error : Cannot connect to server : "+e.toString());
            } catch (Throwable e) {
                //log.info(e);
//                Log.e("SOCKET_SYNC", e.getMessage());
                //reconnect without notice
                //comment next line to auto reconnect
                running = false;
            }
        }
        terminate();
    }

    public void terminate() {
        try {
            InetSocketAddress isa = new InetSocketAddress(this.hostAddress, this.port);
            if (channel!=null&&selector!=null) {
                String ch = channel.keyFor(selector).toString().split("@")[1];
                //log.info("Selector disconnected from : " + isa.toString() + " ID : " + ch);
//                Log.d("SOCKET_SEL", "Selector disconnected from : " + isa.toString() + " ID : " + ch);
                //worker.terminate();
                 selector.close();
                 channel.socket().close();
                 channel.close();
            }
            running = false;
        } catch (IOException ex) {
            //log.info(ex);
            Log.e("SOCKET", "IO Error " + ex.getMessage());
        }
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        // Clear out our read buffer so it's ready for new data
        this.readBuffer.clear();

        // Attempt to read off the channel
        int numRead;
        try {
            numRead = socketChannel.read(this.readBuffer);
            String ch = key.toString().split("@")[1];
//            Log.d("SOCKET_C","Incoming message from ID : " + ch);
        } catch (IOException e) {
            // The remote forcibly closed the connection, cancel
            // the selection key and close the channel.
            key.cancel();
            socketChannel.close();
            return;
        }

        if (numRead == -1) {
            // Remote entity shut the socket down cleanly. Do the
            // same from our end and cancel the channel.
            String ch = key.toString().split("@")[1];
//            Log.d("SOCKET_C","Server disconnected, ID : " + ch);
            key.channel().close();
            key.cancel();
            return;
        }

        // Hand the data off to our worker thread
        reply = new byte[numRead];
        System.arraycopy(this.readBuffer.array(), 0, reply, 0, numRead);
        stage = 2;
        //this.worker.processData(this, socketChannel, this.readBuffer.array(), numRead);
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        synchronized (this.pendingData) {
            List queue = (List) this.pendingData.get(socketChannel);

            // Write until there's not more data ...
            while (!queue.isEmpty()) {
                ByteBuffer buf = (ByteBuffer) queue.get(0);
                socketChannel.write(buf);
                if (buf.remaining() > 0) {
                    // ... or the socket's buffer fills up
                    break;
                }
                queue.remove(0);
            }

            if (queue.isEmpty()) {
                // We wrote away all data, so we're no longer interested
                // in writing on this socket. Switch back to waiting for
                // data.
                key.interestOps(SelectionKey.OP_READ);
            } else {
                String ch = key.toString().split("@")[1];
//                Log.d("SOCKET_C","Outgoing message to ID : " + ch);
            }
        }
    }
}
