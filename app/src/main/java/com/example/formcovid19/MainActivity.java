package com.example.formcovid19;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Documented;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends AppCompatActivity {
    TextInputLayout dataTF;
     Button motiveDeplasareButon;
     Button generatePdfButon;
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
        motiveDeplasareButon=findViewById(R.id.motiveleDeplasariiButon);
        generatePdfButon = findViewById(R.id.geneatePdfButon);
        dataTF = findViewById(R.id.dataTF);

        // Initialize vaiables
        listaMotive = getResources().getStringArray(R.array.motivele_deplasarii);
        checkedItems = new boolean[listaMotive.length];

        //Initializare data de azi
        dataTF.getEditText().setText(dataFormat.format(c.getTime()));

        //Set Up Dexter
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        generatePdfButon.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                createPdfFile(Common.getAppPath(MainActivity.this) + "test_pdf.pdf");
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


        //Listeners

        //show calendar
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



        //pop up checkboxes pentru a selecta motivele deplasarii
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

    private void createPdfFile(String path) {
        if(new File(path).exists())
            new File(path).delete();
        try{
            Document document = new Document();
            //Save
            PdfWriter.getInstance(document,new FileOutputStream(path));
            //Open to write
            document.open();

            //Setting
            document.setPageSize(PageSize.A4);
            document.addCreationDate();
            document.addAuthor("Covid");
            document.addCreator("Emanuel");

            //Font setting
            BaseColor colorAccent = new BaseColor(0,153,104,255);
            float fontSize = 20.0f;
            float valueFontSize = 26.0f;

            //Custom font
            BaseFont fontName = BaseFont.createFont("assets/fonts/greek.ttf","UTF-8",BaseFont.EMBEDDED);

            //Create title of Document
            Font titleFont = new Font(fontName,36.0f,Font.NORMAL,BaseColor.BLACK);
            addNewItem(document,"DECLARATIE PE PROPRIE ΑΣΔΧΣΖΣΑ", Element.ALIGN_CENTER,titleFont);

            //Add more
            document.add(new Paragraph(""));
            document.add(new Paragraph(""));
            addNewItem(document,"BLAL BLAB LBALBLALBAasdasd LBALBLALBAasdasd LBALBLALBAasdasd LBALBLALBAasdasd LBALBασδτοăszțîLALBAasdasd LBALBLALBAasdasd",Element.ALIGN_LEFT,new Font(fontName,fontSize,Font.NORMAL,BaseColor.BLACK));

            document.close();
            Toast.makeText(this,"Succes",Toast.LENGTH_SHORT).show();
            printPdf();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printPdf() {
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        try{
            PrintDocumentAdapter printDocumentAdapter = new PdfDocumentAdapter(MainActivity.this,Common.getAppPath(MainActivity.this)+"test_pdf.pdf");
            printManager.print("Document",printDocumentAdapter,new PrintAttributes.Builder().build());
        } catch (Exception ex) {
            Log.e("COVID19:", " " + ex.getMessage());
        }
    }

    private void addNewItem(Document document, String text, int align, Font font) throws DocumentException {
        Chunk chunk = new Chunk(text,font);
        Log.i("Let's see: ", text);
        Paragraph paragraph = new Paragraph(chunk);
        paragraph.setAlignment(align);
        document.add(paragraph);

    }

}
