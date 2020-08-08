package fr.redstonneur1256.utils;

import java.io.ByteArrayOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class Compression {

    public static byte[] decompress(byte[] compressed) throws DataFormatException {
        Inflater inflater = new Inflater();
        inflater.setInput(compressed);

        byte[] buffer = new byte[1024];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        while(!inflater.finished()) {
            int length = inflater.inflate(buffer);
            outputStream.write(buffer, 0, length);
        }
        return outputStream.toByteArray();
    }

    public static byte[] compress(byte[] decompressed) {
        Deflater deflater = new Deflater();
        deflater.setInput(decompressed);
        deflater.finish();
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        while(!deflater.finished()) {
            int length = deflater.deflate(buffer);
            outputStream.write(buffer, 0, length);
        }
        deflater.end();
        return outputStream.toByteArray();
    }

}