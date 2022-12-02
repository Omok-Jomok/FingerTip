package com.example.fingertip;
import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.String;

public class OcrClient implements Runnable
{

    protected final String host = "ec2-3-39-194-102.ap-northeast-2.compute.amazonaws.com";
    //protected final String host = "10.0.2.2"; // localhost
    protected final int port = 5000;


    private Socket socket = new Socket();
    private byte[] bytes;

    public OcrResult[] result = null;

    public OcrClient(byte[] bytes){
        this.bytes = bytes;
    }

    public OcrClient(Bitmap bitmap)
    {
        // bitmap to bytes
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        bytes = stream.toByteArray();
    }

    public void finalize()
    {
        if (socket != null) {
            try {
                // close socket
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        Log.e("", "RUNNING!");
        try {
            Parse(request());
        } catch (IOException e) {
            Log.e("ocr", "Connection Failed...");
            e.printStackTrace();
        } catch (JSONException e) {
            Log.e("ocr", "Parsing Failed...");
            e.printStackTrace();
        } finally {
            try {
                // close socket
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void Parse(String input) throws JSONException {
        Log.e("", "PARSING!");
        JSONObject jObject = new JSONObject(input);
        JSONArray jsonArray = jObject.getJSONArray("results");
        int[] temp = new int[4];
        result = new OcrResult[jsonArray.length()];
        for(int i = 0; i < jsonArray.length(); i++){
            JSONObject each = jsonArray.getJSONObject(i);
            JSONArray rect = each.getJSONArray("rect");
            if (rect.length() != 4)
                continue;
            for (int j = 0; j < 4; j++)
                temp[j] = rect.getInt(j);
            String text = each.getString("text");
            result[i] = new OcrResult(temp, text);
        }
    }

    private String request() throws IOException {
        Log.e("", "REQUESTING!...");
        if (bytes == null) {
            return null;
        }
        Log.e("", "REQUESTING!..." + bytes.length);
        // connect ocr server
        socket.connect(new InetSocketAddress(host, port));
        Log.e("", "REQUESTING!..." + socket.getInetAddress().toString());
        // send image bytes to server
        socket.getOutputStream().write(ByteBuffer.allocate(4).putInt(bytes.length).array());
        socket.getOutputStream().flush();
        socket.getOutputStream().write(bytes);
        socket.getOutputStream().flush();

        // first 4 bytes: detection result length
        byte[] buffer = new byte[4];
        socket.getInputStream().read(buffer);
        int length = ByteBuffer.wrap(buffer).getInt();

        // read detection result
        buffer = new byte[length];
        socket.getInputStream().read(buffer);
        return new String(buffer);
    }
}