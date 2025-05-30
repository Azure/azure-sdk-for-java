import com.azure.core.util.BinaryData;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Paths;

public class BinaryDataApis {
    public static void main(String... args) throws IOException {

        BinaryData binaryData = BinaryData.fromString("string");
        BinaryData binaryData2 = BinaryData.fromStream(new ByteArrayInputStream(new byte[8]));
        BinaryData binaryData3 = BinaryData.fromStream(new ByteArrayInputStream(new byte[8]), (long) 8);
        BinaryData binaryData4 = BinaryData.fromBytes(new byte[8]);
        BinaryData binaryData7 = BinaryData.fromObject(new Object());
        BinaryData binaryData8 = BinaryData.fromFile(Paths.get("path/to/file"));
        BinaryData binaryData9 = BinaryData.fromFile(Paths.get("path/to/file"), (long) 0,(long) 8);
        BinaryData binaryData10 = BinaryData.fromFile(Paths.get("path/to/file"), (long) 0, (long) 8, 2);

        byte[] bytes = binaryData.toBytes();
        String string = binaryData.toString();
        Object object = binaryData.toObject(Object.class);
        InputStream inputStream = binaryData.toStream();
        ByteBuffer byteBuffer = binaryData.toByteBuffer();

        OutputStream outputStream = new ByteArrayOutputStream();
        binaryData.writeTo(outputStream);

        WritableByteChannel writableByteChannel = null; // Assume this is initialized
        binaryData.writeTo(writableByteChannel);


        Long length = binaryData.getLength();
        boolean isReplayable = binaryData.isReplayable();

        BinaryData replayableBinaryData = binaryData.toReplayableBinaryData();
    }
}
