package com.example.formcovid19;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.w3c.dom.Text;

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
    private static final String LOCUL_HELPER_PDF = "Se vor menționa locurile în care persoana se deplasează, în ordinea în care aceasta intenționează să-și desfășoare traseul.";
    private static final String MOTIVE_HELPER_PDF = "Se va bifa doar motivul/motivele deplasării dintre cele prevăzute în listă, nefiind permise deplasări realizate invoând alte motive decât cele prevăzute în Ordonanța Militară nr. 3/2020..";
    private static final String HELPER_FINAL_PDF = "Persoanele care au împlinit vârsta de 65 de ani completează doar pentru motivele prevăzute în cămpurile 1-6, deplasarea fiind permisă zilnic doar în intervalul orar 11.00 - 13.00.";
    private static final String LOCUL_DEPLASARII_PDF = "Locul/locurile deplasării: ";
    private static final String MOTIVUL_PDF = "Motivul/motivele deplasării: ";
    private static final String DATA_PDF = "Data:";
    private static final String SEMNATURA_PDF = "Semnătura:";
    private static final String positiveCheckbox = "(x) ";
    private static final String negativeCheckbox = "( ) ";

    //edit text fields
    TextInputLayout numeTextInput;
    TextInputLayout ziuaNasteriiTextInput;
    TextInputLayout lunaNasteriiTextInput;
    TextInputLayout anulNasteriiTextInput;
    TextInputLayout adresaLocuinteiTextInput;
    TextInputLayout locurileDeplasariiTextInput;
    TextInputLayout dataTextInput;


    //Properties
    TextInputLayout dataTF;
     Button motiveDeplasareButon, generarePdfButon;
     private DatePickerDialog.OnDateSetListener mBirthDateSetListener;
     String[] listaMotive;
     boolean[] checkedItems;
    SimpleDateFormat dataFormat = new SimpleDateFormat("dd/MM/yyyy"); //default: data de azi
    Calendar c = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize text inputs
        numeTextInput = findViewById(R.id.numPrenumeTF);
        ziuaNasteriiTextInput = findViewById(R.id.ziNastereTF);
        lunaNasteriiTextInput = findViewById(R.id.lunaNastereTF);
        anulNasteriiTextInput = findViewById(R.id.anNastereTF);
        adresaLocuinteiTextInput = findViewById(R.id.adresaTF);
        locurileDeplasariiTextInput = findViewById(R.id.deplasareTF);
        dataTextInput = findViewById(R.id.dataTF);

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

                                String nume = numeTextInput.getEditText().getText().toString();
                                String dataNasterii = ziuaNasteriiTextInput.getEditText().getText().toString() + "/" +
                                        lunaNasteriiTextInput.getEditText().getText().toString() + "/" +
                                        anulNasteriiTextInput.getEditText().getText().toString();
                                String adresaLocutintei = adresaLocuinteiTextInput.getEditText().getText().toString();
                                String locurileDeplasarii = locurileDeplasariiTextInput.getEditText().getText().toString();
                                String data = dataTextInput.getEditText().getText().toString();

                                if(nume.length()>0 && dataNasterii.length()>=8 && adresaLocutintei.length()>0 && locurileDeplasarii.length()>0 && data.length()>0) {
                                  if(generatePdf(nume,dataNasterii,adresaLocutintei,locurileDeplasarii,data)) {
                                      Toast.makeText(getApplicationContext(),"Succes!",Toast.LENGTH_SHORT).show();

                                      //Open pdf
                                      try {
                                          Intent pdfOpenintent = new Intent(Intent.ACTION_VIEW);
                                          pdfOpenintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                          File file = new File(Environment.getExternalStorageDirectory(),
                                                  "myFile.pdf");
                                          Uri path = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName(), file);

                                          pdfOpenintent.setDataAndType(path, "application/pdf");
                                          pdfOpenintent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                          startActivity(pdfOpenintent);

                                      } catch (Exception e) {
                                          e.printStackTrace();
                                          Toast.makeText(getApplicationContext(), "Fișierul nu poate fi deschis!", Toast.LENGTH_SHORT).show();

                                      }

                                  }
                                } else {
                                    Toast.makeText(getApplicationContext(),"Completează toate câmpurile!",Toast.LENGTH_SHORT).show();
                                }



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

                //Clear previous focus
                View current = getCurrentFocus();
                if (current != null) current.clearFocus();

                //hide keyboard
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (inputMethodManager != null)
                    inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);

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

                mBuilder.setNeutralButton("Șterge tot", null);


               final AlertDialog d =  mBuilder.create();;

                d.show();

               Button neutralButton = d.getButton(DialogInterface.BUTTON_NEUTRAL);

               neutralButton.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       ListView listView = d.getListView();
                       for (int i = 0; i < checkedItems.length; i++) {
                           checkedItems[i] = false;
                           listView.setItemChecked(i, false);
                       }
                   }
               });
        }

    });
    }


    private boolean generatePdf(String name, String birthDate, String address, String placesToGo, String date) {
        PdfDocument myPdfDocument = new PdfDocument();
        PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(595,842,1).create();
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
        canvas.drawText(TITLU_PDF,myPageInfo.getPageWidth()/2,y,titluPaint);
        y = breakLine(y,titluPaint,2f);

        //draw name, birth date, address
        canvas.drawText(NUME_PDF,x,y,textBoldPaint);
        canvas.drawText(name,x+textNormalPaint.measureText(NUME_PDF),y,textNormalPaint);
        y=breakLine(y,textNormalPaint,1.1f);

        canvas.drawText(DATA_NASTERII_PDF,x,y,textBoldPaint);
        canvas.drawText(birthDate,x+textNormalPaint.measureText(DATA_NASTERII_PDF),y,textNormalPaint);
        y=breakLine(y,textNormalPaint,1.1f);

        canvas.drawText(ADRESA_PDF,x,y,textBoldPaint);
        canvas.drawText(address,x+textNormalPaint.measureText(ADRESA_PDF),y,textNormalPaint);
        y=breakLine(y,helperTextPaint,0.3f);

        //Helper pentru adresa
        TextPaint mTextPaint = new TextPaint(helperTextPaint);
        canvas.save();
        StaticLayout mTextLayout = new StaticLayout(ADRESA_HELPER_PDF,mTextPaint,canvas.getWidth()-70, Layout.Alignment.ALIGN_NORMAL,1.0f,0.0f,false);
        canvas.translate(x,y);
        mTextLayout.draw(canvas);
        canvas.restore();

        y = y + mTextPaint.getTextSize()*2+1f;
        y = breakLine(y,helperTextPaint,2.2f);

        canvas.drawText(LOCUL_DEPLASARII_PDF,x,y,textBoldPaint);
        canvas.drawText(placesToGo,x+textBoldPaint.measureText(LOCUL_DEPLASARII_PDF),y,textNormalPaint);
        y = breakLine(y,helperTextPaint,0.3f);

        //Helper text locul deplasarii
        mTextLayout = new StaticLayout(LOCUL_HELPER_PDF,mTextPaint,canvas.getWidth()-70, Layout.Alignment.ALIGN_NORMAL,1.1f,0.5f,false);
        canvas.save();
        canvas.translate(x,y);
        mTextLayout.draw(canvas);
        canvas.restore();

        y = y + mTextPaint.getTextSize()*2+1f;
        y = breakLine(y,helperTextPaint,2.2f);

        canvas.drawText(MOTIVUL_PDF,x,y,textBoldPaint);
        y = breakLine(y,helperTextPaint,0.3f);

        //Scrie motivele
        String motive = getMotive();
        mTextPaint = new TextPaint(textMotivePaint);
        mTextLayout = new StaticLayout(motive,mTextPaint,canvas.getWidth()-70, Layout.Alignment.ALIGN_NORMAL,1.0f,0.5f,false);
        canvas.save();
        canvas.translate(x,y);
        mTextLayout.draw(canvas);
        canvas.restore();

        y = y+mTextLayout.getHeight()-mTextPaint.getTextSize();

        //Helper text la motive
        mTextPaint = new TextPaint(helperTextPaint);
        mTextLayout = new StaticLayout(MOTIVE_HELPER_PDF,mTextPaint,canvas.getWidth()-70, Layout.Alignment.ALIGN_NORMAL,1.0f,0f,false);
        canvas.save();
        canvas.translate(x,y);
        mTextLayout.draw(canvas);
        canvas.restore();

        y = y +mTextLayout.getHeight()-mTextPaint.getTextSize();

        //Data
        x = x+35;
        y = breakLine(y,helperTextPaint,4.2f);
        canvas.drawText(DATA_PDF,x,y,textNormalPaint);
        //Semnatura

        textNormalPaint.setTextAlign(Paint.Align.CENTER);
        float newY = breakLine(y,textNormalPaint,1.2f);
        canvas.drawText(date,x+textNormalPaint.measureText(DATA_PDF)/2,newY,textNormalPaint);

        //Semnatura
        x = canvas.getWidth()-120;
        canvas.drawText(SEMNATURA_PDF,x,y,textNormalPaint);




        //reset x and y
        x = 40;y = y+70;

        //add last helper text in bold
        mTextPaint.setFakeBoldText(true);
        mTextPaint.setTextSize(11.5f);
        mTextLayout = new StaticLayout(HELPER_FINAL_PDF,mTextPaint,canvas.getWidth()-70, Layout.Alignment.ALIGN_NORMAL,1.0f,0f,false);
        canvas.save();
        canvas.translate(x,y);
        mTextLayout.draw(canvas);
        canvas.restore();



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
            myPdfDocument.close();
            return false;
        }
        myPdfDocument.close();
        return true;
    }


    private float breakLine(float y, Paint paint,float howMany){
        return y+=(paint.descent()-paint.ascent())*howMany;
    }

    private String getMotive() {
        StringBuilder motive = new StringBuilder("");
        for(int i=0;i<checkedItems.length;i++){
            if(checkedItems[i]) {
                motive.append(positiveCheckbox + listaMotive[i]);
            } else {
                motive.append(negativeCheckbox + listaMotive[i]);
            }
            motive.append("\n");
        }
        return motive.toString();
    }

}
