package com.example.fingertip;

public class OcrResult
{
    public int left;
    public int right;
    public int bottom;
    public int top;
    public String result;

    public OcrResult(int[] rect, String result)
    {
        left = rect[0];
        right = rect[1];
        bottom = rect[2];
        top = rect[3];
        this.result = result;
    }
}
