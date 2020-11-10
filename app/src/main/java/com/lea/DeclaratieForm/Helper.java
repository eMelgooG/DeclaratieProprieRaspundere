package com.lea.DeclaratieForm;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.widget.NestedScrollView;
import com.google.android.material.textfield.TextInputLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class Helper {

    //Constants
    static final String TITLU_PDF = "DECLARAȚIE PE PROPRIE RĂSPUNDERE",
            DATA_NASTERII_PDF = "născut/ă în data de:                                       în localitatea  ",
            NUME_PDF = "Subsemnatul/a: ",
            DOMICILIU_PDF = "domiciliat/ă în:  ",
            RESEDINTA_PDF = "cu reședința în fapt în:  ",
            MOTIVE_HELPER_PDF = "declar pe proprie răspundere, cunoscând prevederile articolului 326 din Codul Penal privind falsul în declarații, că mă deplasez în afara locuinței, " +
                    "în intervalul orar 23.00 – 05.00, din următorul/" +
                    " următoarele motive:",
            HELPER_FINAL_PDF = "*Declarația pe propria răspundere poate fi scrisă de mână, cu condiția preluării tuturor elementelor prezentate mai sus.\n" +
                    "**Declarația pe propria răspundere poate fi stocată și prezentată pentru control pe dispozitive electronice mobile, cu " +
                    "condiția ca pe documentul prezentat să existe semnătura olografă a persoanei care folosește Declarația și data pentru care " +
                    "este valabilă declarația.",
            DATA_PDF = "Data",
            SEMNATURA_PDF = "Semnătura",
            positiveCheckbox = "(X) ",
            negativeCheckbox = "(  ) ",
            sablonInteresProfesionalMotiv = "În interes profesional. Menționez că îmi desfășor activitatea profesională la\n" +
                    "instituția/societatea/organizația %s\n" +
                    "cu sediul în %s\n" +
                    "și cu punct/e de lucru la următoarele adrese:\n" +
                    "%s" +
                    "%s";


    static void hideShowViews(TextInputLayout comp, TextInputLayout sediul, TextInputLayout adr1, TextInputLayout adr2, int view, ConstraintLayout cl, NestedScrollView scrollView) {
        ConstraintSet cs = new ConstraintSet();
        cs.clone(cl);

        if (view == View.VISIBLE) {
            int idCompanie = comp.getId();
            cs.connect(idCompanie, ConstraintSet.TOP, R.id.localitateaTF, ConstraintSet.BOTTOM);
            cs.connect(idCompanie, ConstraintSet.LEFT, R.id.localitateaTF, ConstraintSet.LEFT);
            cs.connect(R.id.dataTF, ConstraintSet.TOP, adr2.getId(), ConstraintSet.BOTTOM);
        } else {
            cs.connect(R.id.dataTF, ConstraintSet.TOP, R.id.localitateaTF, ConstraintSet.BOTTOM);
            scrollView.smoothScrollTo(0, 1500);
        }
        cs.applyTo(cl);
        comp.setVisibility(view);
        sediul.setVisibility(view);
        adr1.setVisibility(view);
        adr2.setVisibility(view);
    }


    static boolean generatePdf(String name, String birthDate, String domiciliu, String resedinta, String localitate, String date, String motive, String semnaturaUriString, Context context) {
        PdfDocument myPdfDocument = new PdfDocument();
        PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page myPage = myPdfDocument.startPage(myPageInfo);

        //titlu
        Paint titluPaint = new Paint();
        titluPaint.setTypeface(Typeface.SANS_SERIF);
        titluPaint.setFakeBoldText(true);
        titluPaint.setTextSize(16f);
        titluPaint.setTextAlign(Paint.Align.CENTER);

        //text normal
        Paint textNormalPaint = new Paint();
        textNormalPaint.setTextSize(13.5f);

        TextPaint mTextNormalPaint = new TextPaint(textNormalPaint);
        textNormalPaint.setTypeface(Typeface.DEFAULT);


        //text bold
        Paint textBoldPaint = new Paint();
        textBoldPaint.setTypeface(Typeface.SANS_SERIF);
        textBoldPaint.setTextSize(13.5f);
        textBoldPaint.setFakeBoldText(true);

        //Motive text
        Paint textMotivePaint = new Paint();
        textMotivePaint.setTextSize(12f);

        //Helper text
        Paint helperTextPaint = new Paint();
        helperTextPaint.setTextSize(10f);

        StaticLayout mTextLayout;

        Canvas canvas = myPage.getCanvas();
        float y = 95;
        float x = 54;
        float z = x + textNormalPaint.measureText(RESEDINTA_PDF);

        //draw image stamp
        Bitmap bim = BitmapFactory.decodeResource(context.getResources(), R.raw.precaut);
        bim.setHasMipMap(true);
        canvas.drawBitmap(bim, null, new RectF(myPageInfo.getPageWidth()/1.6f, y/7.5f, myPageInfo.getPageWidth()/1.6f+160, y/7.5f+70), null);

        y += 10;
        //draw title
        canvas.drawText(TITLU_PDF, myPageInfo.getPageWidth() / 2, y, titluPaint);
        y = breakLine(y, titluPaint, 2.2f);

        //draw name, birth date, address
        canvas.drawText(NUME_PDF, x, y, textBoldPaint);
        canvas.drawText(name, z, y, textNormalPaint);
        y = breakLine(y, textNormalPaint, 1.8f);

        canvas.drawText(DOMICILIU_PDF, x, y, textNormalPaint);
//        canvas.drawText(domiciliu, z, y, textNormalPaint);

        float width = textNormalPaint.measureText(domiciliu);
        float desiredWidth = canvas.getWidth() - z - 50;
        //check to see if the address is long
        if (width < desiredWidth) {
            canvas.drawText(domiciliu, z, y, textNormalPaint);
        } else {
            int[] indexes = separateOnTwoRows(domiciliu, desiredWidth, textNormalPaint);
            canvas.drawText(domiciliu.substring(0, indexes[0]), z, y, textNormalPaint);
            y = breakLine(y, textNormalPaint, 1f);
            canvas.drawText(domiciliu.substring(indexes[1]), z, y, textNormalPaint);
        }
        y = breakLine(y, textNormalPaint, 1.8f);

        canvas.drawText(RESEDINTA_PDF, x, y, textNormalPaint);
        width = textNormalPaint.measureText(resedinta);
        desiredWidth = canvas.getWidth() - z - 50;
        //check to see if the address is long
        if (width < desiredWidth) {
            canvas.drawText(resedinta, z, y, textNormalPaint);
        } else {
            int[] indexes = separateOnTwoRows(resedinta, desiredWidth, textNormalPaint);
            canvas.drawText(resedinta.substring(0, indexes[0]), z, y, textNormalPaint);
            y = breakLine(y, textNormalPaint, 1f);
            canvas.drawText(resedinta.substring(indexes[1]), z, y, textNormalPaint);
        }
        y = breakLine(y, textNormalPaint, 1.8f);

        canvas.drawText(DATA_NASTERII_PDF, x, y, textNormalPaint);
        canvas.drawText(birthDate, z, y, textNormalPaint);
        canvas.drawText(localitate, x + textNormalPaint.measureText(DATA_NASTERII_PDF), y, textNormalPaint);
        y = breakLine(y, textNormalPaint, 3f);


//        float widthPdfTag = textNormalPaint.measureText(DOMICILIU_PDF);

//        float width = textNormalPaint.measureText(address);
//        float desiredWidth = canvas.getWidth() - 70 - widthPdfTag;
        //check to see if the address is long
//        if (width < desiredWidth) {
//            canvas.drawText(address, x + widthPdfTag, y, textNormalPaint);
//        } else {
//            int[] indexes = separateOnTwoRows(address, desiredWidth, textNormalPaint);
        //separate it on two rows

//            canvas.drawText(address.substring(0, indexes[0]), x + widthPdfTag, y, textNormalPaint);
//            y = breakLine(y, textNormalPaint, 0.9f);
//            canvas.drawText(address.substring(indexes[1]), x, y, textNormalPaint);
//        }
//        y = breakLine(y, helperTextPaint, 0.3f);


//        Helper pentru motive
        canvas.save();
        TextPaint mTextPaint;
        mTextPaint = new TextPaint(textNormalPaint);
        mTextLayout = new StaticLayout(MOTIVE_HELPER_PDF, mTextPaint, canvas.getWidth() - 120, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.5f, false);
        canvas.translate(x, y);
        mTextLayout.draw(canvas);
        canvas.restore();

        //Helper pentru adresa
//        canvas.save();
//      mTextLayout = new StaticLayout(ADRESA_HELPER_PDF, mTextPaint, canvas.getWidth() - 70, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
//        canvas.translate(x, y);
//        mTextLayout.draw(canvas);
//        canvas.restore();

        y = y + mTextNormalPaint.getTextSize() * 6 + 1f;
//        y = breakLine(y, helperTextPaint, 2.2f);

//        canvas.drawText(LOCUL_DEPLASARII_PDF, x, y, textBoldPaint);
//        widthPdfTag = textNormalPaint.measureText(LOCUL_DEPLASARII_PDF);
//        width = textNormalPaint.measureText(placesToGo);
//        desiredWidth = canvas.getWidth() - 70 - widthPdfTag;

//        if (width < desiredWidth) {
////            canvas.drawText(placesToGo, x + textBoldPaint.measureText(LOCUL_DEPLASARII_PDF), y, textNormalPaint);
////        } else {
////            int[] indexes = separateOnTwoRows(placesToGo, desiredWidth, textNormalPaint);
////            //separate it on two rows
////            canvas.drawText(placesToGo.substring(0, indexes[0]), x + widthPdfTag, y, textNormalPaint);
////            y = breakLine(y, textNormalPaint, 0.9f);
////            canvas.drawText(placesToGo.substring(indexes[1]), x, y, textNormalPaint);
////        }
//        y = breakLine(y, helperTextPaint, 0.3f);

        //Helper text locul deplasarii
//        mTextLayout = new StaticLayout(LOCUL_HELPER_PDF, mTextPaint, canvas.getWidth() - 70, Layout.Alignment.ALIGN_NORMAL, 1.1f, 0.5f, false);
//        canvas.save();
//        canvas.translate(x, y);
//        mTextLayout.draw(canvas);
//        canvas.restore();

//        y = y + mTextPaint.getTextSize() * 2 + 1f;
//        y = breakLine(y, helperTextPaint, 2.2f);

        //Scrie motivele

        mTextLayout = new StaticLayout(motive, mTextPaint, canvas.getWidth() - 120, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.5f, false);
        canvas.save();
        canvas.translate(x + 16, y);
        mTextLayout.draw(canvas);
        canvas.restore();

        y = y + mTextLayout.getHeight() - mTextPaint.getTextSize();

//        //Helper text la motive
//        mTextPaint = new TextPaint(helperTextPaint);
//        mTextLayout = new StaticLayout(MOTIVE_HELPER_PDF, mTextPaint, canvas.getWidth() - 70, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0f, false);
//        canvas.save();
//        canvas.translate(x, y);
//        mTextLayout.draw(canvas);
//        canvas.restore();
//
//        y = y + mTextLayout.getHeight() - mTextPaint.getTextSize();

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

        if (semnaturaUriString != null) {
            Bitmap bm = null;
            try {
                InputStream is = context.getContentResolver().openInputStream(Uri.parse(semnaturaUriString));
                bm = BitmapFactory.decodeStream(is);
                is.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (bm != null) {
                x-=45;
                bm.setHasMipMap(true);
                canvas.drawBitmap(bm, null, new RectF(x, y, x+90, y+40), null);
            }
        }


        //reset x and y
        x = 60;
        y = y + 70;

        //add last helper text in bold
        mTextPaint.setFakeBoldText(false);
        mTextPaint.setTextSize(9.5f);
        mTextLayout = new StaticLayout(HELPER_FINAL_PDF, mTextPaint, canvas.getWidth() - 100, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0f, false);
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
