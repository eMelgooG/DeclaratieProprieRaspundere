package com.example.formcovid19;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class SignatureView extends View implements View.OnTouchListener {

    private List<List<Path>> signaturePaths;
    private List<Path> activeSignaturePaths;
    private Paint signaturePaint;
    private Paint baselinePaint;

    private int baselinePaddingHorizontal = 0;
    private int baselinePaddingBottom = 0;
    private int baselineXMark = 0;
    private int baselineXMarkOffsetVertical = 0;

    private int[] lastTouchEvent;

    public SignatureView(Context context) {
        super(context);

        initDefaultValues();
    }

    public SignatureView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initDefaultValues();
    }

    public SignatureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initDefaultValues();
    }

    protected void initDefaultValues() {
        setOnTouchListener(this);
        signaturePaths = new ArrayList<>();
        signaturePaint = new Paint();
        signaturePaint.setAntiAlias(true);
        signaturePaint.setColor(Color.BLACK);
        signaturePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        signaturePaint.setStrokeWidth(15f);
        signaturePaint.setStrokeCap(Paint.Cap.ROUND);

        baselinePaint = new Paint();
        baselinePaint.setAntiAlias(true);
        baselinePaint.setColor(Color.BLACK);
        baselinePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        baselinePaint.setStrokeWidth(1f);
        baselinePaint.setStrokeCap(Paint.Cap.ROUND);
    }


    @Override
    public boolean onTouch(View view, MotionEvent e) {
        int[] event = new int[]{(int) e.getX(), (int) e.getY()};

        if (e.getAction() == MotionEvent.ACTION_UP) {
            lastTouchEvent = null;

            signaturePaths.add(activeSignaturePaths);
            activeSignaturePaths = new ArrayList<>();
        } else if (e.getAction() == MotionEvent.ACTION_DOWN) {
            if (!(activeSignaturePaths == null || activeSignaturePaths.size() < 1))
                signaturePaths.add(activeSignaturePaths);

            activeSignaturePaths = new ArrayList<>();
        } else {
            Path path = new Path();
            path.moveTo(lastTouchEvent[0], lastTouchEvent[1]);
            path.lineTo(event[0], event[1]);

            activeSignaturePaths.add(path);
        }

        lastTouchEvent = event;

        postInvalidate();

        return true;
    }

    public void undoLastSignaturePath() {
        if (0 < signaturePaths.size()) {
            signaturePaths.remove(signaturePaths.size() - 1);

            postInvalidate();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawIndicators(canvas);
        drawSignaturePaths(canvas);
    }


    public void clearCanvas( ) {
        initDefaultValues();
        postInvalidate();
    }

    protected void drawIndicators(Canvas canvas) {
        canvas.drawLine(baselinePaddingHorizontal,
                canvas.getHeight() - baselinePaddingBottom,
                canvas.getWidth() - baselinePaddingHorizontal,
                canvas.getHeight() - baselinePaddingBottom,
                baselinePaint);

        drawXMark(canvas);
    }

    protected void drawXMark(Canvas canvas) {
        int radius = baselineXMark / 2;
        int cX = baselinePaddingHorizontal + radius;
        int cY = canvas.getHeight() - baselinePaddingBottom - radius - baselineXMarkOffsetVertical;

        canvas.save();
        canvas.rotate(-45, cX, cY);
        canvas.drawLine(cX - radius, cY, cX + radius, cY, baselinePaint);
        canvas.restore();

        canvas.save();
        canvas.rotate(45, cX, cY);
        canvas.drawLine(cX - radius, cY, cX + radius, cY, baselinePaint);
        canvas.restore();
    }

    protected void drawSignaturePaths(Canvas canvas) {
        for (List<Path> l : signaturePaths) {
            for (Path p : l) {
                canvas.drawPath(p, signaturePaint);
            }
        }

        if (activeSignaturePaths != null) {
            for (Path p : activeSignaturePaths) {
                canvas.drawPath(p, signaturePaint);
            }
        }
    }
    // Scale a bitmap preserving the aspect ratio.

    /**
     * @param bitmap the Bitmap to be scaled
     * @param threshold the maxium dimension (either width or height) of the scaled bitmap
     * @param isNecessaryToKeepOrig is it necessary to keep the original bitmap? If not recycle the original bitmap to prevent memory leak.
     * */

    public static Bitmap getScaledDownBitmap(Bitmap bitmap, int threshold, boolean isNecessaryToKeepOrig){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = width;
        int newHeight = height;

        if(width > height && width > threshold){
            newWidth = threshold;
            newHeight = (int)(height * (float)newWidth/width);
        }

        if(width > height && width <= threshold){
            //the bitmap is already smaller than our required dimension, no need to resize it
            return bitmap;
        }

        if(width < height && height > threshold){
            newHeight = threshold;
            newWidth = (int)(width * (float)newHeight/height);
        }

        if(width < height && height <= threshold){
            //the bitmap is already smaller than our required dimension, no need to resize it
            return bitmap;
        }

        if(width == height && width > threshold){
            newWidth = threshold;
            newHeight = newWidth;
        }

        if(width == height && width <= threshold){
            //the bitmap is already smaller than our required dimension, no need to resize it
            return bitmap;
        }

        return getResizedBitmap(bitmap, newWidth, newHeight, isNecessaryToKeepOrig);
    }

    private static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight, boolean isNecessaryToKeepOrig) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        if(!isNecessaryToKeepOrig){
            bm.recycle();
        }
        return resizedBitmap;
    }

    public Bitmap getContentDataBMP() {
        setDrawingCacheEnabled(true);

        Bitmap bitmap = getDrawingCache();
        Bitmap cropBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight() - 1);
        bitmap.recycle();

        Bitmap resizedBitmap = getScaledDownBitmap(cropBitmap,400,false);
//                Bitmap.createScaledBitmap(cropBitmap, 200, 50, true);
        cropBitmap.recycle();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        setDrawingCacheEnabled(false);

        if(signaturePaths.size()>0)
        return resizedBitmap; else return null;
    }

    public String getContentDataURI() {
        setDrawingCacheEnabled(true);

        Bitmap bitmap = getDrawingCache();
        Bitmap cropBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight() - 1);
        bitmap.recycle();

        Bitmap resizedBitmap = getScaledDownBitmap(cropBitmap,400,false);
        cropBitmap.recycle();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

        setDrawingCacheEnabled(false);

        return "data:image/png;base64," + Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP);
    }

    public String encodeImageURI(Bitmap bm) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        return "data:image/png;base64," + Base64.encodeToString(stream.toByteArray(),Base64.NO_WRAP);
    }
}