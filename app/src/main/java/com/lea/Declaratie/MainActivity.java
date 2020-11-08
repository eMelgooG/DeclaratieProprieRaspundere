package com.lea.Declaratie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {




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
    static String semnaturaUriString = null;
    private String locurileDeplasarii = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = this.getSharedPreferences("com.lea.Declaratie", Context.MODE_PRIVATE);

        //Initialize text inputs
        numeTextInput = findViewById(R.id.subsemnatulTF);
        ziuaNasteriiTextInput = findViewById(R.id.ziNastereTF);
        lunaNasteriiTextInput = findViewById(R.id.lunaNastereTF);
        anulNasteriiTextInput = findViewById(R.id.anNastereTF);
        adresaLocuinteiTextInput = findViewById(R.id.domiciliuTF);
        locurileDeplasariiTextInput = findViewById(R.id.resedintaTF);
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
                                                if (Helper.generatePdf(nume, dataNasterii, adresaLocutintei, locurileDeplasarii, data, motive,MainActivity.this)) {
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

                        //Set listener to semnatura buton
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






    private String getMotive() {
        StringBuilder motive = new StringBuilder("");
        boolean x = false;
        for (int i = 0; i < checkedItems.length; i++) {
            if (checkedItems[i]) {
                motive.append(Helper.positiveCheckbox + listMotivePdf[i]);
                x = true;
            } else {
                motive.append(Helper.negativeCheckbox + listMotivePdf[i]);
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
