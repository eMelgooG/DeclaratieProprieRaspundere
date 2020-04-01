package com.example.formcovid19;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import com.google.android.material.textfield.TextInputLayout;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    //Constants
    private static final String TITLU_PDF = "DECLARAȚIE PE PROPRIE RĂSPUNDERE";
    private static final String DATA_NASTERII_PDF = "Data nașterii: ";
    private static final String NUME_PDF = "Nume, prenume: ";
    private static final String ADRESA_PDF = "Adresa locuinței: ";
    private static final String ADRESA_HELPER_PDF = "Se va completa adresa locuinței în care persoana locuiește în fapt, indiferent dacă este indentică sau nu cu cea menționată în actul de identitate.";
    //Properties
    TextInputLayout dataTF;
     Button motiveDeplasareButon, generarePdfButon;
     private DatePickerDialog.OnDateSetListener mBirthDateSetListener;
     String[] listaMotive;
     boolean[] checkedItems;
    SimpleDateFormat dataFormat = new SimpleDateFormat("dd/MM/yyyy"); //default: data de azi
    Calendar c = Calendar.getInstance();
     ArrayList<Integer> listUserMotive = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize Views
        motiveDeplasareButon=findViewById(R.id.motiveleDeplasariiButon);
        generarePdfButon = findViewById(R.id.geneatePdfButon);
        dataTF = findViewById(R.id.dataTF);

        // Initialize vaiables
        listaMotive = getResources().getStringArray(R.array.motivele_deplasarii);
        checkedItems = new boolean[listaMotive.length];

        //Initializare data de azi
        dataTF.getEditText().setText(dataFormat.format(c.getTime()));

        //Listeners
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        generarePdfButon.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                generatePdf("Lucaci Emanuel","06/01/1993","Strada Vasile Alecsandri, nr. 2");
                            }
                        });
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                })
                .check();

        //Afisarea iconiteti cu calendar din text field-ul 'Data' si afisarea calendarului
        dataTF.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePicker = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        c.set(year,month,dayOfMonth);
                        dataTF.getEditText().setText(dataFormat.format(c.getTime()));
                    }
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                datePicker.show();
            }
        });




        motiveDeplasareButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
                mBuilder.setTitle("Alege cel puțin o variantă");

                mBuilder.setMultiChoiceItems(listaMotive, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                       //Do nothing
                    }
                });

                mBuilder.setCancelable(false);
                mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TO DO WHEN OK IS PRESSED
                    }
                });

                mBuilder.setNeutralButton("Șterge tot", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       for(int i = 0;i<checkedItems.length;i++){
                           checkedItems[i] = false;
                       }
                        listUserMotive.clear();
                       mBuilder.show();
                    }
                });
                mBuilder.show();
            }
        });

    }

    private void generatePdf(String name, String birthDate, String address) {
        PdfDocument myPdfDocument = new PdfDocument();
        PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(595,842,1).create();
        PdfDocument.Page myPage = myPdfDocument.startPage(myPageInfo);

        //titlu
        Paint titluPaint = new Paint();
        titluPaint.setTypeface(Typeface.SERIF);
        titluPaint.setTextSize(10.5f);
        titluPaint.setTextAlign(Paint.Align.CENTER);

        //text normal
        Paint textNormalPaint = new Paint();
        textNormalPaint.setTextSize(9f);

        //text bold
        Paint textBoldPaint = new Paint();
        textBoldPaint.setTextSize(10f);
        textBoldPaint.setFakeBoldText(true);

        //Helper text
        Paint helperTextPaint = new Paint();
        helperTextPaint.setTextScaleX(1.4f);
        helperTextPaint.setTextSize(4.7f);



        Canvas canvas = myPage.getCanvas();
        float y = 80;
        float x = 80;

        //draw title
        canvas.drawText(TITLU_PDF,myPageInfo.getPageWidth()/2,y,titluPaint);
        y = breakLine(y,titluPaint,3);

        //draw name, birth date, address
        canvas.drawText(NUME_PDF,x,y,textNormalPaint);
        canvas.drawText(name,x+textNormalPaint.measureText(NUME_PDF),y,textBoldPaint);
        y=breakLine(y,textNormalPaint,1.3f);

        canvas.drawText(DATA_NASTERII_PDF,x,y,textNormalPaint);
        canvas.drawText(birthDate,x+textNormalPaint.measureText(DATA_NASTERII_PDF),y,textBoldPaint);
        y=breakLine(y,textNormalPaint,1.3f);

        canvas.drawText(ADRESA_PDF,x,y,textNormalPaint);
        canvas.drawText(address,x+textNormalPaint.measureText(ADRESA_PDF),y,textBoldPaint);
        y=breakLine(y,textNormalPaint,1f);
        //Helper pentru adresa
        canvas.drawText(ADRESA_HELPER_PDF,x,y,helperTextPaint);


        //finish document
        myPdfDocument.finishPage(myPage);

        //Create the file
        String filePath = Environment.getExternalStorageDirectory().getPath() + "/myFile.pdf";
        File myFile = new File(filePath);

        //write to outputstream
        try {
            myPdfDocument.writeTo(new FileOutputStream(myFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
        myPdfDocument.close();
    }


    private float breakLine(float y, Paint paint,float howMany){
        return y+=(paint.descent()-paint.ascent())*howMany;
    }


}
