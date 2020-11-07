package com.lea.Declaratie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
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
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    //Constants
    private static final String TITLU_PDF = "DECLARAȚIE PE PROPRIE RĂSPUNDERE",
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

    //Views
    TextInputLayout numeTextInput, ziuaNasteriiTextInput, lunaNasteriiTextInput, anulNasteriiTextInput, adresaLocuinteiTextInput, locurileDeplasariiTextInput, dataTextInput, dataTF;
    MaterialButton motiveDeplasareButon, generarePdfButon, semnaturaButon;
    ImageView semnaturaImageView;
    ImageButton imageButtonRemove;

    //Properties
    String[] listaMotive;
    String[] listMotivePdf;
    boolean[] checkedItems;
    SimpleDateFormat dataFormat = new SimpleDateFormat("dd/MM/yyyy"); //default: data de azi
    Calendar c = Calendar.getInstance();
    SharedPreferences sharedPreferences;
    private static String semnaturaUriString = null;
    private String locurileDeplasarii = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = this.getSharedPreferences("com.lea.Declaratie", Context.MODE_PRIVATE);

        //Initialize text inputs
        numeTextInput = findViewById(R.id.numPrenumeTF);
        ziuaNasteriiTextInput = findViewById(R.id.ziNastereTF);
        lunaNasteriiTextInput = findViewById(R.id.lunaNastereTF);
        anulNasteriiTextInput = findViewById(R.id.anNastereTF);
        adresaLocuinteiTextInput = findViewById(R.id.adresaTF);
        locurileDeplasariiTextInput = findViewById(R.id.deplasareTF);
        dataTextInput = findViewById(R.id.dataTF);
        dataTF = findViewById(R.id.dataTF);

        //Initialize other views
        imageButtonRemove = findViewById(R.id.imageButtonRemove);
        motiveDeplasareButon = findViewById(R.id.motiveleDeplasariiButon);
        semnaturaButon = findViewById(R.id.semnaturaButon);
        generarePdfButon = findViewById(R.id.geneatePdfButon);
        semnaturaImageView = findViewById(R.id.imageViewSemnatura);

        // Initialize variables
        listaMotive = getResources().getStringArray(R.array.motivele_deplasarii);
        listMotivePdf = getResources().getStringArray(R.array.motivele_deplasarii_pdf);

        //Reinitialize the state if configuration change occurs
        if (savedInstanceState != null) {
            numeTextInput.getEditText().setText(savedInstanceState.getString("nume"));
            ziuaNasteriiTextInput.getEditText().setText(savedInstanceState.getString("zi"));
            lunaNasteriiTextInput.getEditText().setText(savedInstanceState.getString("luna"));
            anulNasteriiTextInput.getEditText().setText(savedInstanceState.getString("an"));
            adresaLocuinteiTextInput.getEditText().setText(savedInstanceState.getString("adresa"));
            locurileDeplasariiTextInput.getEditText().setText(savedInstanceState.getString("locuri"));
            checkedItems = savedInstanceState.getBooleanArray("motive");
            dataTextInput.getEditText().setText(savedInstanceState.getString("data"));
            semnaturaUriString = savedInstanceState.getString("semnatura");
        } else {
            //Otherwise update everything
            checkedItems = new boolean[listaMotive.length];
            //Initializare data de azi
            dataTF.getEditText().setText(dataFormat.format(c.getTime()));
            locurileDeplasarii = sharedPreferences.getString("locuri",null);
            numeTextInput.getEditText().setText(sharedPreferences.getString("nume", null));
            ziuaNasteriiTextInput.getEditText().setText(sharedPreferences.getString("zi", null));
            lunaNasteriiTextInput.getEditText().setText(sharedPreferences.getString("luna", null));
            anulNasteriiTextInput.getEditText().setText(sharedPreferences.getString("an", null));
            adresaLocuinteiTextInput.getEditText().setText(sharedPreferences.getString("adresa", null));
            semnaturaUriString = sharedPreferences.getString("semnatura", null);
            locurileDeplasariiTextInput.getEditText().setText(locurileDeplasarii);
        }

        if (semnaturaUriString != null) {
            Uri uri = Uri.parse(semnaturaUriString);
            updateImageView(uri);
        }
        //Listeners
                        generarePdfButon.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                //Get the user input
                                String nume = numeTextInput.getEditText().getText().toString();
                                String dataNasterii = "";
                                String zi = ziuaNasteriiTextInput.getEditText().getText().toString();
                                String luna = lunaNasteriiTextInput.getEditText().getText().toString();
                                String anul = anulNasteriiTextInput.getEditText().getText().toString();

                                SharedPreferences.Editor sharedPreferencesEdit = sharedPreferences.edit();
                                // check to see if all the text fields are filled
                                if (zi.length() > 0 && luna.length() > 0 && anul.length() > 0) {
                                    sharedPreferencesEdit.putString("zi", zi);
                                    sharedPreferencesEdit.putString("luna", luna);
                                    sharedPreferencesEdit.putString("an", anul);
                                    dataNasterii = zi + "/" +
                                            luna + "/" + anul;
                                    String adresaLocutintei = adresaLocuinteiTextInput.getEditText().getText().toString();
                                     locurileDeplasarii = locurileDeplasariiTextInput.getEditText().getText().toString();
                                    String data = dataTextInput.getEditText().getText().toString();

                                    if (nume.length() > 0 && adresaLocutintei.length() > 0) {
                                        sharedPreferencesEdit.putString("nume", nume);
                                        sharedPreferencesEdit.putString("adresa", adresaLocutintei);
                                        sharedPreferencesEdit.apply();

                                        if (locurileDeplasarii.length() > 0) {

                                            // check to see if there are any reasons selected and also retrieve the Motivele in String format to write to to the PDF
                                            String motive = getMotive();
                                            if (motive.length() > 0) {
                                                if (anul.length() < 4) {
                                                    anulNasteriiTextInput.setError("Invalid");
                                                    anulNasteriiTextInput.requestFocus();
                                                    return;
                                                }

                                                if (data.length() < 8) {
                                                    Toast.makeText(getApplicationContext(), "Data introdusă este invalidă!", Toast.LENGTH_SHORT).show();
                                                    dataTextInput.requestFocus();
                                                    return;
                                                }
                                                // try to create the pdf
                                                if (generatePdf(nume, dataNasterii, adresaLocutintei, locurileDeplasarii, data, motive)) {
                                                    sharedPreferencesEdit.putString("locuri",locurileDeplasarii).apply();
                                                    //Open pdf
                                                    try {
                                                        Intent pdfOpenintent = new Intent(Intent.ACTION_VIEW);
                                                        pdfOpenintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                                                        File file = new File(getExternalFilesDir(null), "myFile.pdf");

                                                        Uri path = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName(), file);

                                                        pdfOpenintent.setDataAndType(path, "application/pdf");
                                                        pdfOpenintent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                                        startActivity(pdfOpenintent);

                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                        Toast.makeText(getApplicationContext(), "Fișierul nu poate fi deschis!", Toast.LENGTH_SHORT).show();
                                                    }
                                                    Toast.makeText(getApplicationContext(), "Succes!", Toast.LENGTH_SHORT).show();

                                                } else {
                                                    Toast.makeText(getApplicationContext(), "Ceva nu a mers cum trebuie!", Toast.LENGTH_SHORT).show();
                                                }

                                            } else {
                                                Toast.makeText(getApplicationContext(), "Alege cel puțin un motiv!", Toast.LENGTH_SHORT).show();
                                                //to be done show warning sign
                                                motiveDeplasareButon.setIconTintResource(R.color.red);
                                            }

                                        } else {
                                            Toast.makeText(getApplicationContext(), "Completează toate câmpurile!", Toast.LENGTH_SHORT).show();
                                        }

                                    } else {
                                        Toast.makeText(getApplicationContext(), "Completează toate câmpurile!", Toast.LENGTH_SHORT).show();
                                    }


                                } else
                                    Toast.makeText(getApplicationContext(), "Completează toate câmpurile!", Toast.LENGTH_SHORT).show();
                            }
                        });

                        //If permission granted also set listener to semnatura buton
                        semnaturaButon.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SignatureViewDemoDialog dialog = new SignatureViewDemoDialog();
                                dialog.show(getFragmentManager(), "semnaturaFragment");
                            }
                        });

        //Afisarea iconiteti cu calendar din text field-ul 'Data' si afisarea calendarului
        dataTF.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePicker = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        c.set(year, month, dayOfMonth);
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

                final boolean[] isOk = new boolean[1];

                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
                mBuilder.setTitle("Alege cel puțin o variantă:");

                mBuilder.setMultiChoiceItems(listaMotive, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    }
                });

                mBuilder.setCancelable(false);
                mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int i = 0;
                        for (; i < checkedItems.length; i++)
                            if (checkedItems[i]) {
                                motiveDeplasareButon.setIconTintResource(R.color.buttonColor2);
                                motiveDeplasareButon.setIcon(getDrawable(R.drawable.ic_check_black_24dp));
                                return;
                            }
                        if (i == checkedItems.length)
                            motiveDeplasareButon.setIcon(getDrawable(R.drawable.ic_add_24dp));
                    }

                });

                mBuilder.setNeutralButton("Șterge tot", null);

                final AlertDialog d = mBuilder.create();

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
                        isOk[0] = true;
                    }
                });
            }

        });

        //Zi nastere editText listener
        ziuaNasteriiTextInput.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (ziuaNasteriiTextInput.getEditText().getText().toString().length() == 2) {
                    if (ziuaNasteriiTextInput.getEditText().getSelectionEnd() == 2)
                        lunaNasteriiTextInput.getEditText().requestFocus();
                }
            }
        });

        lunaNasteriiTextInput.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (lunaNasteriiTextInput.getEditText().getText().toString().length() == 2) {
                    if (lunaNasteriiTextInput.getEditText().getSelectionEnd() == 2)
                        anulNasteriiTextInput.getEditText().requestFocus();
                }
            }
        });

        anulNasteriiTextInput.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (anulNasteriiTextInput.getEditText().getText().toString().length() == 4) {
                    if (anulNasteriiTextInput.getEditText().getSelectionEnd() == 4) {
                        adresaLocuinteiTextInput.requestFocus();
                        anulNasteriiTextInput.setErrorEnabled(false);
                    }
                }
            }
        });

        imageButtonRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(getFilesDir(), "semnatura.png");
                if (file.exists()) file.delete();
                semnaturaUriString = null;
                updateImageView(null);
            }
        });

    }


    private boolean generatePdf(String name, String birthDate, String address, String placesToGo, String date, String motive) {
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
        if (semnaturaUriString != null) {
            Bitmap bm = null;
            try {
                InputStream is = getContentResolver().openInputStream(Uri.parse(semnaturaUriString));
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
        File myFile = new File(getExternalFilesDir(null), "myFile.pdf");

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

    private int[] separateOnTwoRows(String text, float desiredWidth, Paint textNormalPaint) {
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


    private float breakLine(float y, Paint paint, float howMany) {
        return y += (paint.descent() - paint.ascent()) * howMany;
    }

    private String getMotive() {
        StringBuilder motive = new StringBuilder("");
        boolean x = false;
        for (int i = 0; i < checkedItems.length; i++) {
            if (checkedItems[i]) {
                motive.append(positiveCheckbox + listMotivePdf[i]);
                x = true;
            } else {
                motive.append(negativeCheckbox + listMotivePdf[i]);
            }
            motive.append("\n");
        }
        if (x) return motive.toString();
        else return "";
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("nume", numeTextInput.getEditText().getText().toString());
        outState.putString("zi", ziuaNasteriiTextInput.getEditText().getText().toString());
        outState.putString("luna", lunaNasteriiTextInput.getEditText().getText().toString());
        outState.putString("an", anulNasteriiTextInput.getEditText().getText().toString());
        outState.putString("adresa", adresaLocuinteiTextInput.getEditText().getText().toString());
        outState.putString("locuri", locurileDeplasariiTextInput.getEditText().getText().toString());
        outState.putBooleanArray("motive", checkedItems);
        outState.putString("data", dataTF.getEditText().getText().toString());
        outState.putString("semnatura", semnaturaUriString);
    }


    protected void updateImageView(Uri uriSemnatura) {
        if (uriSemnatura != null) {
            semnaturaUriString = uriSemnatura.toString();
            semnaturaImageView.setImageURI(uriSemnatura);
            setVisibleSemnatura();
        } else {
            semnaturaUriString = null;
            semnaturaImageView.setImageBitmap(null);
            semnaturaImageView.invalidate();
            setInvisibleSemnatura();
        }
    }


    private void setVisibleSemnatura() {
        semnaturaButon.setVisibility(View.GONE);
        semnaturaImageView.setVisibility(View.VISIBLE);
        imageButtonRemove.setVisibility(View.VISIBLE);
    }

    private void setInvisibleSemnatura() {
        semnaturaButon.setVisibility(View.VISIBLE);
        semnaturaImageView.setVisibility(View.INVISIBLE);
        imageButtonRemove.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sharedPreferences.edit().putString("semnatura", semnaturaUriString).apply();
    }
}
