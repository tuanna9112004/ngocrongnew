package nro.login;

import nro.server.io.Message;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import lombok.Getter;


public class LoginSession {

    @Getter
    private boolean connected;
    private LoginController controller;
    @Getter
    private LoginService service;
    public boolean isStopSend = false;
    private DataOutputStream dos;
    public DataInputStream dis;
    public Socket sc;
    public boolean connecting;
    private final Sender sender = new Sender();
    public int sendByteCount;
    public int recvByteCount;
    boolean getKeyComplete;
    public byte[] key = null;
    private byte curR, curW;
    long timeConnected;
    public String strRecvByteCount = "";
    public boolean isCancel;
    private Vector sendingMessage;
    private String host;
    private int port;

    // Thread pool TÁI SỬ DỤNG - static để share giữa các LoginSession
    private static ExecutorService loginExecutor;
    private Future<?> initFuture;
    private Future<?> timeoutFuture;
    private Future<?> senderFuture;
    private Future<?> collectorFuture;

    // Khởi tạo thread pool khi class được load
    static {
        loginExecutor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("LoginSession-Worker");
            return t;
        });
    }

    public LoginSession() {
        this.controller = new LoginController(this);
        this.service = new LoginService(this);
    }

    public void connect(String host, int port) {
        if (connected || connecting) {
            return;
        } else {
            this.host = host;
            this.port = port;
            getKeyComplete = false;
            sc = null;
            
            // SỬ DỤNG thread pool thay vì new Thread
            initFuture = loginExecutor.submit(new NetworkInit(host, port));
        }
    }

    public void reconnect() {
        System.out.println("ket noi lai!");
        connect(host, port);
    }

    class NetworkInit implements Runnable {
        private final String connectHost;
        private final int connectPort;

        NetworkInit(String h, int p) {
            connectHost = h;
            connectPort = p;
        }

        public void run() {
            isCancel = false;
            
            // Timeout thread - SỬ DỤNG thread pool
            timeoutFuture = loginExecutor.submit(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(20000);
                    } catch (InterruptedException e) {
                        return; // Bị interrupt, thoát ngay
                    }
                    if (connecting) {
                        try {
                            sc.close();
                        } catch (Exception e) {
                        }
                        isCancel = true;
                        connecting = false;
                        connected = false;
                        controller.onConnectionFail();
                    }
                }
            });
            
            connecting = true;
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            connected = true;
            
            try {
                doConnect(connectHost, connectPort);
                controller.onConnectOK();
                
                // Hủy timeout thread nếu connect thành công
                if (timeoutFuture != null) {
                    timeoutFuture.cancel(true);
                }
            } catch (Exception ex) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
                if (isCancel) {
                    return;
                }
                if (controller != null) {
                    close();
                    controller.onConnectionFail();
                }
            }
        }

        public void doConnect(String host, int port) throws Exception {
            sc = new Socket(host, port);
            dos = new DataOutputStream(sc.getOutputStream());
            dis = new DataInputStream(sc.getInputStream());
            
            // Sender thread - SỬ DỤNG thread pool
            senderFuture = loginExecutor.submit(sender);
            
            // Collector thread - SỬ DỤNG thread pool
            collectorFuture = loginExecutor.submit(new MessageCollector());
            
            timeConnected = System.currentTimeMillis();
            doSendMessage(new Message(-27));
            connecting = false;
        }
    }

    public void sendMessage(Message message) {
        sender.AddMessage(message);
    }

    private synchronized void doSendMessage(Message m) throws IOException {
        byte[] data = m.getData();
        try {
            if (getKeyComplete) {
                byte b = (writeKey(m.command));
                dos.writeByte(b);
            } else {
                dos.writeByte(m.command);
            }
            
            if (data != null) {
                int size = data.length;
                if (m.command == -31) {
                    dos.writeShort(size);
                } else if (getKeyComplete) {
                    int byte1 = writeKey((byte) (size >> 8));
                    dos.writeByte(byte1);
                    int byte2 = writeKey((byte) (size & 0xFF));
                    dos.writeByte(byte2);
                } else {
                    dos.writeShort(size);
                }
                if (getKeyComplete) {
                    for (int i = 0; i < data.length; i++) {
                        data[i] = writeKey(data[i]);
                    }
                }
                dos.write(data);
                sendByteCount += (5 + data.length);
            } else {
                dos.writeShort(0);
                sendByteCount += 5;
            }
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte readKey(byte b) {
        byte i = (byte) ((key[curR++] & 0xff) ^ (b & 0xff));
        if (curR >= key.length) {
            curR %= key.length;
        }
        return i;
    }

    private byte writeKey(byte b) {
        byte i = (byte) ((key[curW++] & 0xff) ^ (b & 0xff));
        if (curW >= key.length) {
            curW %= key.length;
        }
        return i;
    }

    private class Sender implements Runnable {

        public Sender() {
            sendingMessage = new Vector();
        }

        public void AddMessage(Message message) {
            sendingMessage.addElement(message);
        }

        public void run() {
            while (connected) {
                try {
                    if (getKeyComplete) {
                        while (sendingMessage.size() > 0) {
                            Message m = (Message) sendingMessage.elementAt(0);
                            sendingMessage.removeElementAt(0);
                            doSendMessage(m);
                        }
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        break; // Bị interrupt, thoát ngay
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    class MessageCollector implements Runnable {

        public void run() {
            Message message;
            try {
                while (isConnected()) {
                    message = readMessage();
                    if (message != null) {
                        try {
                            if (message.command == -27) {
                                getKey(message);
                            } else {
                                controller.process(message);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        break;
                    }
                }
            } catch (Exception ex) {
            }
            if (connected) {
                if (controller != null) {
                    if (System.currentTimeMillis() - timeConnected > 500) {
                        controller.onDisconnected();
                    } else {
                        controller.onConnectionFail();
                    }
                }
                if (sc != null) {
                    cleanNetwork();
                }
            }
        }

        private void getKey(Message message) throws IOException {
            byte keySize = message.reader().readByte();
            key = new byte[keySize];
            for (int i = 0; i < keySize; i++) {
                key[i] = message.reader().readByte();
            }
            for (int i = 0; i < key.length - 1; i++) {
                key[i + 1] ^= key[i];
            }
            getKeyComplete = true;
        }

        private Message readMessage() throws Exception {

            // read message command
            byte cmd = dis.readByte();
            if (getKeyComplete) {
                cmd = readKey(cmd);
            }
            // read size of data
            int size;

            if (cmd == -32) {
                cmd = dis.readByte();
                if (getKeyComplete) {
                    cmd = readKey(cmd);
                }
                byte b1 = readKey(dis.readByte());
                byte b2 = readKey(dis.readByte());
                byte b3 = readKey(dis.readByte());
                byte b4 = readKey(dis.readByte());
                size = ((b1 & 0xff) << 24) | ((b2 & 0xff) << 16)
                        | ((b3 & 0xff) << 8) | (b4 & 0xff);
            } else if (getKeyComplete) {
                byte b1 = dis.readByte();
                byte b2 = dis.readByte();
                size = (readKey(b1) & 0xff) << 8 | readKey(b2) & 0xff;
            } else {
                size = dis.readUnsignedShort();
            }
            byte data[] = new byte[size];
            int len = 0;
            int byteRead = 0;
            while (len != -1 && byteRead < size) {
                len = dis.read(data, byteRead, size - byteRead);
                if (len > 0) {
                    byteRead += len;
                    recvByteCount += (5 + byteRead);
                    int Kb = (recvByteCount + sendByteCount);
                    strRecvByteCount = Kb / 1024 + "." + Kb % 1024 / 102 + "Kb";
                }
            }
            if (getKeyComplete) {
                for (int i = 0; i < data.length; i++) {
                    data[i] = readKey(data[i]);
                }
            }
            Message msg = new Message(cmd, data);
            return msg;
        }
    }

    public void close() {
        cleanNetwork();
    }

    private void cleanNetwork() {
        key = null;
        curR = 0;
        curW = 0;
        try {
            connected = false;
            connecting = false;
            
            // Cancel tất cả các Future đang chạy
            if (timeoutFuture != null) {
                timeoutFuture.cancel(true);
            }
            if (senderFuture != null) {
                senderFuture.cancel(true);
            }
            if (collectorFuture != null) {
                collectorFuture.cancel(true);
            }
            if (initFuture != null) {
                initFuture.cancel(true);
            }
            
            if (sc != null) {
                sc.close();
                sc = null;
            }
            if (dos != null) {
                dos.close();
                dos = null;
            }
            if (dis != null) {
                dis.close();
                dis = null;
            }
            System.gc();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Shutdown thread pool khi server tắt - gọi từ ServerManager
     */
    public static void shutdownExecutor() {
        if (loginExecutor != null) {
            try {
                loginExecutor.shutdown();
                if (!loginExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    loginExecutor.shutdownNow();
                }
            } catch (Exception e) {
                loginExecutor.shutdownNow();
            }
        }
    }
}