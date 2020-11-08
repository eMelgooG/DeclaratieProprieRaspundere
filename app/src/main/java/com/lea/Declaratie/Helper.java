package com.lea.Declaratie;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;



public class Helper {

    //Constants
     static final String TITLU_PDF = "DECLARAȚIE PE PROPRIE RĂSPUNDERE",
            DATA_NASTERII_PDF = "Data nașterii: ",
            NUME_PDF = "Nume, prenume: ",
            ADRESA_PDF = "Adresa locuinței: ",
            ADRESA_HELPER_PDF = "Se va completa adresa locuinței în care persoana locuiește în fapt, indiferent dacă este indentică sau nu cu cea menționată în actul de identitate.",
            LOCUL_HELPER_PDF = "Se vor menționa locurile în care persoana se deplasează, în ordinea în care aceasta intenționează să-și desfășoare traseul.",
            MOTIVE_HELPER_PDF = "Se va bifa doar motivul/motivele deplasării dintre cele prevăzute în listă, nefiind permise deplasări realizate invoând alte motive decât cele prevăzute în Ordonanța Militară nr. 3/2020..",
            HELPER_FINAL_PDF = "Persoanele care au împlinit vârsta de 65 de ani completează doar pentru motivele prevăzute în cămpurile 1-6, deplasarea fiind permisă zilnic doar în intervalul orar 11.00 - 13.00.",
            LOCUL_DEPLASARII_PDF = "Locul/locurile deplasării: ",
            MOTIVUL_PDF = "Motivul/motivele deplasării: ",
            DATA_PDF = "Data",
            SEMNATURA_PDF = "Semnătura",
            positiveCheckbox = "(x) ",
            negativeCheckbox = "( ) ";



    static boolean generatePdf(String name, String birthDate, String address, String placesToGo, String date, String motive, Context context) {
        PdfDocument myPdfDocument = new PdfDocument();
        PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page myPage = myPdfDocument.startPage(myPageInfo);

        //titlu
        Paint titluPaint = new Paint();
        titluPaint.setTypeface(Typeface.SANS_SERIF);
        titluPaint.setFakeBoldText(true);
        titluPaint.setTextSize(18f);
        titluPaint.setTextAlign(Paint.Align.CENTER);

        //text normal
        Paint textNormalPaint = new Paint();
        textNormalPaint.setTypeface(Typeface.SANS_SERIF);
        textNormalPaint.setTextSize(15f);

        //text bold
        Paint textBoldPaint = new Paint();
        textBoldPaint.setTypeface(Typeface.SANS_SERIF);
        textBoldPaint.setTextSize(15f);
        textBoldPaint.setFakeBoldText(true);

        //Motive text
        Paint textMotivePaint = new Paint();
        textMotivePaint.setTextSize(12f);

        //Helper text
        Paint helperTextPaint = new Paint();
        helperTextPaint.setTextSize(10f);


        Canvas canvas = myPage.getCanvas();
        float y = 60;
        float x = 40;

        //draw title
        canvas.drawText(TITLU_PDF, myPageInfo.getPageWidth() / 2, y, titluPaint);
        y = breakLine(y, titluPaint, 2f);

        //draw name, birth date, address
        canvas.drawText(NUME_PDF, x, y, textBoldPaint);
        canvas.drawText(name, x + textNormalPaint.measureText(NUME_PDF), y, textNormalPaint);
        y = breakLine(y, textNormalPaint, 1.1f);

        canvas.drawText(DATA_NASTERII_PDF, x, y, textBoldPaint);
        canvas.drawText(birthDate, x + textNormalPaint.measureText(DATA_NASTERII_PDF), y, textNormalPaint);
        y = breakLine(y, textNormalPaint, 1.1f);

        canvas.drawText(ADRESA_PDF, x, y, textBoldPaint);

        float widthPdfTag = textNormalPaint.measureText(ADRESA_PDF);
        float width = textNormalPaint.measureText(address);
        float desiredWidth = canvas.getWidth() - 70 - widthPdfTag;
        //check to see if the address is long
        if (width < desiredWidth) {
            canvas.drawText(address, x + widthPdfTag, y, textNormalPaint);
        } else {
            int[] indexes = separateOnTwoRows(address, desiredWidth, textNormalPaint);
            //separate it on two rows

            canvas.drawText(address.substring(0, indexes[0]), x + widthPdfTag, y, textNormalPaint);
            y = breakLine(y, textNormalPaint, 0.9f);
            canvas.drawText(address.substring(indexes[1]), x, y, textNormalPaint);
        }
        y = breakLine(y, helperTextPaint, 0.3f);


        //Helper pentru adresa
        TextPaint mTextPaint = new TextPaint(helperTextPaint);
        canvas.save();
        StaticLayout mTextLayout = new StaticLayout(ADRESA_HELPER_PDF, mTextPaint, canvas.getWidth() - 70, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        canvas.translate(x, y);
        mTextLayout.draw(canvas);
        canvas.restore();

        y = y + mTextPaint.getTextSize() * 2 + 1f;
        y = breakLine(y, helperTextPaint, 2.2f);

        canvas.drawText(LOCUL_DEPLASARII_PDF, x, y, textBoldPaint);
        widthPdfTag = textNormalPaint.measureText(LOCUL_DEPLASARII_PDF);
        width = textNormalPaint.measureText(placesToGo);
        desiredWidth = canvas.getWidth() - 70 - widthPdfTag;

        if (width < desiredWidth) {
            canvas.drawText(placesToGo, x + textBoldPaint.measureText(LOCUL_DEPLASARII_PDF), y, textNormalPaint);
        } else {
            int[] indexes = separateOnTwoRows(placesToGo, desiredWidth, textNormalPaint);
            //separate it on two rows
            canvas.drawText(placesToGo.substring(0, indexes[0]), x + widthPdfTag, y, textNormalPaint);
            y = breakLine(y, textNormalPaint, 0.9f);
            canvas.drawText(placesToGo.substring(indexes[1]), x, y, textNormalPaint);
        }
        y = breakLine(y, helperTextPaint, 0.3f);

        //Helper text locul deplasarii
        mTextLayout = new StaticLayout(LOCUL_HELPER_PDF, mTextPaint, canvas.getWidth() - 70, Layout.Alignment.ALIGN_NORMAL, 1.1f, 0.5f, false);
        canvas.save();
        canvas.translate(x, y);
        mTextLayout.draw(canvas);
        canvas.restore();

        y = y + mTextPaint.getTextSize() * 2 + 1f;
        y = breakLine(y, helperTextPaint, 2.2f);

        canvas.drawText(MOTIVUL_PDF, x, y, textBoldPaint);
        y = breakLine(y, helperTextPaint, 0.3f);

        //Scrie motivele
        mTextPaint = new TextPaint(textMotivePaint);
        mTextLayout = new StaticLayout(motive, mTextPaint, canvas.getWidth() - 70, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.5f, false);
        canvas.save();
        canvas.translate(x, y);
        mTextLayout.draw(canvas);
        canvas.restore();

        y = y + mTextLayout.getHeight() - mTextPaint.getTextSize();

        //Helper text la motive
        mTextPaint = new TextPaint(helperTextPaint);
        mTextLayout = new StaticLayout(MOTIVE_HELPER_PDF, mTextPaint, canvas.getWidth() - 70, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0f, false);
        canvas.save();
        canvas.translate(x, y);
        mTextLayout.draw(canvas);
        canvas.restore();

        y = y + mTextLayout.getHeight() - mTextPaint.getTextSize();

        //Data
        x = x + 35;
        y = breakLine(y, helperTextPaint, 4.2f);
        canvas.drawText(DATA_PDF, x, y, textNormalPaint);
        //Semnatura

        textNormalPaint.setTextAlign(Paint.Align.CENTER);
        float newY = breakLine(y, textNormalPaint, 1.2f);
        canvas.drawText(date, x + textNormalPaint.measureText(DATA_PDF) / 2, newY, textNormalPaint);

        //Semnatura
        x = canvas.getWidth() - 120;
        canvas.drawText(SEMNATURA_PDF, x, y, textNormalPaint);
        Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
        mPaint.setFilterBitmap(true);
        if (MainActivity.semnaturaUriString != null) {
            Bitmap bm = null;
            try {
                InputStream is = context.getContentResolver().openInputStream(Uri.parse(MainActivity.semnaturaUriString));
                bm = BitmapFactory.decodeStream(is);
                is.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (bm != null) {
                bm.setDensity(bm.getDensity() * 4);
                bm.setHasMipMap(true);
                canvas.drawBitmap(bm, x - 45, y, mPaint);
                bm.setDensity(560);
            }
        }


        //reset x and y
        x = 40;
        y = y + 70;

        //add last helper text in bold
        mTextPaint.setFakeBoldText(true);
        mTextPaint.setTextSize(11.5f);
        mTextLayout = new StaticLayout(HELPER_FINAL_PDF, mTextPaint, canvas.getWidth() - 70, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0f, false);
        canvas.save();
        canvas.translate(x, y);
        mTextLayout.draw(canvas);
        canvas.restore();


        //finish document
        myPdfDocument.finishPage(myPage);

        //Create the file
        File myFile = new File(context.getExternalFilesDir(null), "myFile.pdf");

        //write to outputstream
        try {
            if (myFile.exists()) myFile.delete();
            myPdfDocument.writeTo(new FileOutputStream(myFile));
        } catch (Exception e) {
            e.printStackTrace();
            myPdfDocument.close();
            return false;
        }
        myPdfDocument.close();
        return true;
    }
    private static float breakLine(float y, Paint paint, float howMany) {
        return y += (paint.descent() - paint.ascent()) * howMany;
    }

    private static int[] separateOnTwoRows(String text, float desiredWidth, Paint textNormalPaint) {
        float total = 0;
        int i = 0;
        int indexComma = 0;
        int indexSpace = 0;
        for (; total <= desiredWidth; i++) {
            char c = text.charAt(i);
            total += textNormalPaint.measureText(Character.toString(c));
            if (c == ' ') indexSpace = i;
            if (c == ',') indexComma = i;
        }

        int index1 = 0;
        int index2 = 0;

        if (indexSpace > indexComma) {
            index1 = indexSpace;
            index2 = indexSpace + 1;
        } else if (indexComma > indexSpace) {
            index1 = indexComma;
            if (index1 + 1 == indexSpace) index2 = index1 + 1;
            else index2 = indexComma;
        } else {
            index1 = i;
            index2 = i;
        }
        return new int[]{index1, index2};
    }
}
