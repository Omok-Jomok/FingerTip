package com.example.fingertip;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

public class OcrClient
{
    protected final String host = "ec2-52-79-105-90.ap-northeast-2.compute.amazonaws.com";
    protected final int port = 9879;

    private ImageView view;
    private Bitmap bitmap;
    private ByteArrayOutputStream stream;

    public OcrClient(Bitmap bitmap, ImageView view)
    {
        this.bitmap = bitmap;
        this.view = view;
        // bitmap to bytes
        stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
    }

    public void Detect() throws IOException {
        if (stream == null || bitmap == null || view == null) {
            return;
        }

        // connect ocr server
        Socket s = new Socket();
        s.connect(new InetSocketAddress(host, port));

        // send image bytes to server
        s.getOutputStream().write(stream.toByteArray());
        s.getOutputStream().flush();

        // first 4 bytes: detection result length
        byte[] buffer = new byte[4];
        s.getInputStream().read(buffer);
        int length = ByteBuffer.wrap(buffer).getInt();

        // read detection result
        if (length > 12)
        {
            buffer = new byte[length];
            s.getInputStream().read(buffer);
            Log.e("", new String(buffer));
        }
        else {
            // no text detected
        }

        // close socket
        s.close();

        // show image
        view.setImageBitmap(bitmap);
    }
}