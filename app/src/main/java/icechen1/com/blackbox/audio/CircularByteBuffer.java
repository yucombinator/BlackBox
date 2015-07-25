package icechen1.com.blackbox.audio;

import java.io.EOFException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

/**
 * Description of CircularByteBuffer.
 */
public class CircularByteBuffer extends CustomByteBuffer
{
    /**
     * Constructs the CircularByteBuffer.
     *
     * @param size
     */
    public CircularByteBuffer( int size )
    {
        this.size = size;
        buf = new byte[size];
    }

    private final int size;

    private final byte[] buf;

    private int length;

    private int nextGet;

    private int nextPut;

    @Override
    public int size()
    {
        return size;
    }

    @Override
    public int length()
    {
        return length;
    }

    @Override
    public void clear()
    {
        length = 0;
        nextGet = 0;
        nextPut = 0;
    }

    @Override
    public byte get() throws EOFException
    {
        if (isEmpty())
            throw new EOFException();

        length--;
        byte b = buf[nextGet++];
        if (nextGet >= size)
            nextGet = 0;
        return b;
    }

    @Override
    public void put( byte b ) throws BufferOverflowException
    {
        if (!isFull()){
            length++;
        }
        buf[nextPut++] = b;
        if (nextPut >= size)
            nextPut = 0;
    }
}