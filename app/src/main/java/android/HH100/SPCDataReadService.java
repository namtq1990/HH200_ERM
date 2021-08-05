package android.HH100;

import java.io.StringWriter;
import java.nio.ByteBuffer;

public class SPCDataReadService {

    ByteBuffer buffer;
    int position;
    int lengthData;

    int readCount;

    public SPCDataReadService(StringWriter strw)
    {
        byte[] data = strw.toString().getBytes();
        lengthData = data.length;

        buffer = ByteBuffer.allocate( lengthData );
        buffer.put(data);
        buffer.flip();

        position = 0;
        readCount = 0;
    }

    public byte[] ReadData(int count)
    {
        if(lengthData < count + position)
        {
            readCount = lengthData - position;
        }
        else
        {
            readCount = count;
        }

        byte[] ret = new byte[readCount];
        buffer.get(ret);

        position = buffer.position();

        return ret;
    }

    public int GerReadCount()
    {
        return readCount;
    }

    public int FileLength()
    {
        return lengthData;
    }

}
