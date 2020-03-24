package com.github.periket2000;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NioSocketServer
{
    final AsynchronousServerSocketChannel listener = AsynchronousServerSocketChannel.open();

    public AsynchronousServerSocketChannel getListener() {
        return listener;
    }

    public NioSocketServer() throws IOException {
        try
        {
            // Create an AsynchronousServerSocketChannel that will listen on port 5000
            listener.bind(new InetSocketAddress(5000));

            // Listen for a new request
            listener.accept( null, new CompletionHandler<AsynchronousSocketChannel,Void>() {

                @Override
                public void completed(AsynchronousSocketChannel ch, Void att)
                {
                    System.out.println("Inside : " + Thread.currentThread().getName());

                    // Accept the next connection
                    listener.accept( null, this );

                    // Greet the client
                    ch.write( ByteBuffer.wrap( "Hello, I am Echo Server 2020, let's have an engaging conversation!\n".getBytes() ) );

                    // Allocate a byte buffer (4K) to read from the client
                    ByteBuffer byteBuffer = ByteBuffer.allocate( 4096 );
                    try
                    {
                        // Read the first line
                        int bytesRead = ch.read( byteBuffer ).get( 20, TimeUnit.SECONDS );

                        boolean running = true;
                        while( bytesRead != -1 && running )
                        {
                            System.out.println( "bytes read: " + bytesRead );

                            // Make sure that we have data to read
                            if( byteBuffer.position() > 2 )
                            {
                                // Make the buffer ready to read
                                byteBuffer.flip();

                                // Convert the buffer into a line
                                byte[] lineBytes = new byte[ bytesRead ];
                                byteBuffer.get( lineBytes, 0, bytesRead );
                                String line = new String( lineBytes );

                                // Debug
                                System.out.println( "Message: " + line );

                                // Echo back to the caller
                                ch.write( ByteBuffer.wrap( line.getBytes() ) );

                                // Make the buffer ready to write
                                byteBuffer.clear();

                                // Read the next line
                                bytesRead = ch.read( byteBuffer ).get( 20, TimeUnit.SECONDS );
                            }
                            else
                            {
                                // An empty line signifies the end of the conversation in our protocol
                                running = false;
                            }
                        }
                    }
                    catch (InterruptedException e)
                    {
                        ///e.printStackTrace();
                    }
                    catch (ExecutionException e)
                    {
                        ///e.printStackTrace();
                    }
                    catch (TimeoutException e)
                    {
                        // The user exceeded the 20 second timeout, so close the connection
                        ch.write( ByteBuffer.wrap( "Good Bye\n".getBytes() ) );
                        System.out.println( "Connection timed out, closing connection" );
                    }

                    System.out.println( "End of conversation" );
                    try
                    {
                        // Close the connection if we need to
                        if( ch.isOpen() )
                        {
                            ch.close();
                        }
                    }
                    catch (IOException e1)
                    {
                        ///e1.printStackTrace();
                    }
                }

                @Override
                public void failed(Throwable exc, Void att) {
                    ///...
                }
            });
        }
        catch (IOException e)
        {
            ///e.printStackTrace();
        }
    }

    public static void main( String[] args )
    {
        Runnable runnable = () -> {
            System.out.println("Inside : " + Thread.currentThread().getName());
            NioSocketServer server = null;
            try
            {
                server = new NioSocketServer();
                Thread.currentThread().join();
            }
            catch( Exception e )
            {
                ///e.printStackTrace();
            }
            finally {
                if(server != null) {
                    try {
                        server.getListener().close();
                    } catch (IOException e) {
                        ///...
                    }
                }
            }
        };

        System.out.println("Creating Thread...");
        Thread thread = new Thread(runnable);

        System.out.println("Starting Thread...");
        thread.start();
    }
}