package com.github.periket2000;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.net.InetSocketAddress;

public class NioSocketClient {

    static BlockingQueue<Future> q = new LinkedBlockingQueue<>();
    static Thread t;
    static AsynchronousSocketChannel client;
    static int RECONNECT_TIME = 5000;

    
    public static void checkQueue() {
        Runnable r = new Runnable() {
            public void run() {
                while (true) {
                    try {
                        while(true) {
                            Future f = q.take();
                            while (!f.isDone()) {
                                System.out.println("... ");
                            }
                            System.out.println("Processed request! " + (Integer) f.get());
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        t.interrupt();
                    }
                    try {
                        Thread.sleep(RECONNECT_TIME);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        };
        new Thread(r).start();        
    }

    public static void main (String [] args)
            throws Exception {

        checkQueue();
        new NioSocketClient().go();
    }

    private void go()
            throws IOException, InterruptedException, ExecutionException {

        while(true) {
            t = new Thread(() -> {
                try {
                    client = AsynchronousSocketChannel.open();
                    InetSocketAddress hostAddress = new InetSocketAddress("localhost", 5000);
                    Future future = client.connect(hostAddress);
                    future.get(); // returns null

                    System.out.println("Client is started: " + client.isOpen());
                    System.out.println("Sending messages to server: ");

                    while(true) {
                        String [] messages = new String [] {"Time goes fast.", "What now?", "Bye."};

                        for (int i = 0; i < messages.length; i++) {

                            byte [] message = new String(messages [i]).getBytes();
                            ByteBuffer buffer = ByteBuffer.wrap(message);
                            Future result = client.write(buffer);

                            q.add(result);

                            System.out.println(messages [i]);
                            buffer.clear();
                            Thread.sleep(1000);
                        }
                    }
                } catch(Exception e) {
                    try {
                        client.close();
                        t.interrupt();
                    } catch(Exception e1) {
                        t.interrupt();
                    }
                }
            });
            t.start();
            t.join();
            Thread.sleep(RECONNECT_TIME);
            System.out.println("Trying to reconnect!");
        }
    }
}
