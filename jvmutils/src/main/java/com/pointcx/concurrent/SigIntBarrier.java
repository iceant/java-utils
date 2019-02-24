package com.pointcx.concurrent;


import sun.misc.Signal;

import java.util.concurrent.locks.LockSupport;

/**
 * One time barrier for blocking a thread until a SIGINT signal is received from the operating system.
 */
public class SigIntBarrier
{
    private final Thread thread;
    private volatile boolean running = true;

    /**
     * Construct and register the barrier ready for use.
     */
    public SigIntBarrier()
    {
        thread = Thread.currentThread();

        Signal.handle(
                new Signal("INT"),
                (signal) ->
                {
                    running = false;
                    LockSupport.unpark(thread);
                });
    }

    /**
     * Await the reception of the SIGINT signal.
     */
    public void await()
    {
        while (running)
        {
            LockSupport.park();
        }
    }
}
