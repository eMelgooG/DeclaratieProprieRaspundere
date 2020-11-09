package com.lea.Declaratie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.FileProvider;
import androidx.core.widget.NestedScrollView;

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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    //Views
    TextInputLayout numeTextInput, ziuaNasteriiTextInput, lunaNasteriiTextInput, anulNasteriiTextInput, domiciliuTextInput, resedintaTextInput, localitateaTextInput, dataTF,
            companieTextInput, sediulTextInput, adresa1TextInput, adresa2TextInput;
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
    String semnaturaUriString = null;
    boolean isExpanded = false;
    byte motivDeplasareTint = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = this.getSharedPreferences("com.lea.Declaratie", Context.MODE_PRIVATE);

        //Initialize text inputs
        localitateaTextInput = findViewById(R.id.localitateaTF);
        numeTextInput = findViewById(R.id.subsemnatulTF);
        ziuaNasteriiTextInput = findViewById(R.id.ziNastereTF);
        lunaNasteriiTextInput = findViewById(R.id.lunaNastereTF);
        anulNasteriiTextInput = findViewById(R.id.anNastereTF);
        domiciliuTextInput = findViewById(R.id.domiciliuTF);
        resedintaTextInput = findViewById(R.id.resedintaTF);

        companieTextInput = findViewById(R.id.companieTF);
        sediulTextInput = findViewById(R.id.sediulTF);
        adresa1TextInput = findViewById(R.id.adresa1TF);
        adresa2TextInput = findViewById(R.id.adresa2TF);

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
            checkedItems = savedInstanceState.getBooleanArray("motive");
            dataTF.getEditText().setText(savedInstanceState.getString("data"));
            isExpanded = savedInstanceState.getBoolean("isExpanded");
            if (isExpanded) {
                Helper.hideShowViews(companieTextInput, sediulTextInput, adresa1TextInput, adresa2TextInput, View.VISIBLE, (ConstraintLayout) findViewById(R.id.nestedConstraintLayout), (NestedScrollView) findViewById(R.id.nestedScrollView));
            }
            motivDeplasareTint = savedInstanceState.getByte("motivDeplasareTint");
            switch (motivDeplasareTint) {
                case (0): {
                    motiveDeplasareButon.setIcon(getDrawable(R.drawable.ic_add_24dp));
                    break;
                }
                case (1): {
                    motiveDeplasareButon.setIconTintResource(R.color.buttonColor2);
                    motiveDeplasareButon.setIcon(getDrawable(R.drawable.ic_check_black_24dp));
                    break;
                }
                case (2): {
                    motiveDeplasareButon.setIconTintResource(R.color.red);
                }
            }
        } else {
            checkedItems = new boolean[listaMotive.length];
        }
        //Initializare data de azi
        dataTF.getEditText().setText(dataFormat.format(c.getTime()));
        localitateaTextInput.getEditText().setText(sharedPreferences.getString("localitate", ""));
        numeTextInput.getEditText().setText(sharedPreferences.getString("nume", ""));
        ziuaNasteriiTextInput.getEditText().setText(sharedPreferences.getString("zi", ""));
        lunaNasteriiTextInput.getEditText().setText(sharedPreferences.getString("luna", ""));
        anulNasteriiTextInput.getEditText().setText(sharedPreferences.getString("an", ""));
        domiciliuTextInput.getEditText().setText(sharedPreferences.getString("domiciliu", ""));
        resedintaTextInput.getEditText().setText(sharedPreferences.getString("resedinta", ""));
        semnaturaUriString = sharedPreferences.getString("semnatura", null);
        companieTextInput.getEditText().setText(sharedPreferences.getString("companie", ""));
        sediulTextInput.getEditText().setText(sharedPreferences.getString("sediul", ""));
        adresa1TextInput.getEditText().setText(sharedPreferences.getString("adresa1", ""));
        adresa2TextInput.getEditText().setText(sharedPreferences.getString("adresa2", ""));

        if (semnaturaUriString != null) {
            Uri uri = Uri.parse(semnaturaUriString);
            updateImageView(uri);
        }
        //Listeners
        generarePdfButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get the user input
                String nume = numeTextInput.getEditText().getText().toString(),
                        zi = ziuaNasteriiTextInput.getEditText().getText().toString(),
                        luna = lunaNasteriiTextInput.getEditText().getText().toString(),
                        anul = anulNasteriiTextInput.getEditText().getText().toString(),
                        domiciliu = domiciliuTextInput.getEditText().getText().toString(),
                        resedinta = resedintaTextInput.getEditText().getText().toString(),
                        localitate = localitateaTextInput.getEditText().getText().toString(),
                        data = dataTF.getEditText().getText().toString(),
                        companie = companieTextInput.getEditText().getText().toString(),
                        sediul = sediulTextInput.getEditText().getText().toString(),
                        adresa1 = adresa1TextInput.getEditText().getText().toString(),
                        adresa2 = adresa2TextInput.getEditText().getText().toString(),
                        dataNasterii = "";


                // check to see if all the text fields are filled
                if (zi.length() > 0 && luna.length() > 0 && anul.length() > 0) {
                    dataNasterii = zi + "/" +
                            luna + "/" + anul;


                    if (nume.length() > 0 && domiciliu.length() > 0) {
                        // check to see if there are any reasons selected and also retrieve the Motivele in String format to write to to the PDF
                        if (isExpanded) {
                            if (!adresa2.isEmpty()) {
                                adresa1 += "\n";
                            }
                            listMotivePdf[0] = String.format(Helper.sablonInteresProfesionalMotiv, companie, sediul, adresa1, adresa2);
                        } else {
                            listMotivePdf[0] = String.format(Helper.sablonInteresProfesionalMotiv, "", "", "", "");
                        }
                        String motive = getMotive();
                        if (motive.length() > 0) {
                            if (anul.length() < 4) {
                                anulNasteriiTextInput.setError("Invalid");
                                anulNasteriiTextInput.requestFocus();
                                return;
                            }

                            if (data.length() < 8) {
                                Toast.makeText(getApplicationContext(), "Data introdusă este invalidă!", Toast.LENGTH_SHORT).show();
                                dataTF.requestFocus();
                                return;
                            }

                            // try to create the pdf
                            if (Helper.generatePdf(nume, dataNasterii, domiciliu, resedinta, localitate, data, motive, semnaturaUriString, MainActivity.this)) {

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
                            motivDeplasareTint = 2;
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
                                motivDeplasareTint = 1;

                                //if we choose first option we unhide the views and change constraints

                                if (checkedItems[0] == true && !isExpanded) {
                                    Helper.hideShowViews(companieTextInput, sediulTextInput, adresa1TextInput, adresa2TextInput, View.VISIBLE, (ConstraintLayout) findViewById(R.id.nestedConstraintLayout), (NestedScrollView) findViewById(R.id.nestedScrollView));
                                    isExpanded = true;

                                } else if (isExpanded && checkedItems[0] == false) {
                                    Helper.hideShowViews(companieTextInput, sediulTextInput, adresa1TextInput, adresa2TextInput, View.INVISIBLE, (ConstraintLayout) findViewById(R.id.nestedConstraintLayout), (NestedScrollView) findViewById(R.id.nestedScrollView));
                                    isExpanded = false;
                                }
                                return;
                            }
                        if (i == checkedItems.length) {
                            motiveDeplasareButon.setIcon(getDrawable(R.drawable.ic_add_24dp));
                            motivDeplasareTint = 0;
                        }
                        if (isExpanded && checkedItems[0] == false) {
                            Helper.hideShowViews(companieTextInput, sediulTextInput, adresa1TextInput, adresa2TextInput, View.INVISIBLE, (ConstraintLayout) findViewById(R.id.nestedConstraintLayout), (NestedScrollView) findViewById(R.id.nestedScrollView));
                            ConstraintLayout cl = findViewById(R.id.constraintLayout);
                            NestedScrollView scrollView = findViewById(R.id.nestedScrollView);
                            scrollView.scrollTo(0, 0);
                            isExpanded = false;
                        }
                    }

                });

                mBuilder.setNeutralButton("Șterge tot", null);

                final AlertDialog d = mBuilder.create();
                d.show();
                d.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

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
                        domiciliuTextInput.requestFocus();
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
                motive.append(Helper.positiveCheckbox + listMotivePdf[i] + '\n');
                x = true;
            } else {
                motive.append(Helper.negativeCheckbox + listMotivePdf[i] + '\n');
            }
            motive.append('\n');
        }
        if (x) return motive.toString();
        else return "";
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isExpanded", isExpanded);
        outState.putBooleanArray("motive", checkedItems);
        outState.putString("data", dataTF.getEditText().getText().toString());
        outState.putByte("motivDeplasareTint", motivDeplasareTint);

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
        SharedPreferences.Editor sharedPreferencesEdit = sharedPreferences.edit();
        sharedPreferencesEdit.putString("semnatura", semnaturaUriString);
        sharedPreferencesEdit.putString("nume", numeTextInput.getEditText().getText().toString());
        sharedPreferencesEdit.putString("domiciliu", domiciliuTextInput.getEditText().getText().toString());
        sharedPreferencesEdit.putString("resedinta", resedintaTextInput.getEditText().getText().toString());
        sharedPreferencesEdit.putString("localitate", localitateaTextInput.getEditText().getText().toString());
        sharedPreferencesEdit.putString("zi", ziuaNasteriiTextInput.getEditText().getText().toString());
        sharedPreferencesEdit.putString("luna", lunaNasteriiTextInput.getEditText().getText().toString());
        sharedPreferencesEdit.putString("an", anulNasteriiTextInput.getEditText().getText().toString());
        sharedPreferencesEdit.putString("companie", companieTextInput.getEditText().getText().toString());
        sharedPreferencesEdit.putString("sediul", sediulTextInput.getEditText().getText().toString());
        sharedPreferencesEdit.putString("adresa1", adresa1TextInput.getEditText().getText().toString());
        sharedPreferencesEdit.putString("adresa2", adresa2TextInput.getEditText().getText().toString());
        sharedPreferencesEdit.apply();
    }
}
