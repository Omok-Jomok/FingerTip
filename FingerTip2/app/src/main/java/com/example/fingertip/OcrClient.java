package com.example.fingertip;
import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.String;

public class OcrClient implements Runnable
{
//    protected final String host = "ec2-52-79-105-90.ap-northeast-2.compute.amazonaws.com";
//    protected final int port = 9879;
    protected final String host = "ec2-3-39-194-102.ap-northeast-2.compute.amazonaws.com";
    //protected final String host = "10.0.2.2"; // localhost
    protected final int port = 5000;

    public Bitmap bitmap;
    private ByteArrayOutputStream stream;
    private Socket socket = new Socket();

    public int[] result = null;

    public OcrClient(Bitmap bitmap)
    {
        // bitmap to bytes
        this.bitmap = bitmap;
        stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
    }

    @Override

    public void run() {
        String result = null;

        try {
            result = request();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                // close socket
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (result != null) {
            int[] best = new int[4];
            int area = 0;
            int[] temp = new int[4];

            Pattern pattern = Pattern.compile("\\d+,\\d+,\\d+,\\d+");
            Matcher matcher = pattern.matcher(result.replace(" ", ""));

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            while (matcher.find()) {
                String[] rect = matcher.group().split(",");
                temp[0] = Integer.parseInt(rect[0]);
                temp[1] = Integer.parseInt(rect[1]);
                temp[2] = Integer.parseInt(rect[2]);
                temp[3] = Integer.parseInt(rect[3]);
                int t = (temp[1] - temp[0]) * (temp[3] - temp[2]);
                if (t > area)
                {
                    if (temp[0] < 0 || temp[1] > width)
                        continue;
                    if (temp[2] < 0 || temp[3] > height)
                        continue;
                    if (temp[0] > temp[1])
                        continue;
                    if (temp[2] > temp[3])
                        continue;
                    area = t;
                    best[0] = temp[0];
                    best[1] = temp[1];
                    best[2] = temp[2];
                    best[3] = temp[3];
                }
            }

            if (area > 0) {
                bitmap = Bitmap.createBitmap(bitmap, best[0], best[2], best[1] - best[0], best[3] - best[2]);
            }
        }

    }

    private String request() throws IOException {
        if (stream == null || bitmap == null) {
            return null;
        }
        // connect ocr server
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port));

        // send image bytes to server
        socket.getOutputStream().write(stream.toByteArray());
        socket.getOutputStream().flush();

        // first 4 bytes: detection result length
        byte[] buffer = new byte[4];
        socket.getInputStream().read(buffer);
        int length = ByteBuffer.wrap(buffer).getInt();

        // read detection result
        if (length > 12)
        {
            buffer = new byte[length];
            socket.getInputStream().read(buffer);
            return new String(buffer);
        }
        return null;
    }
}