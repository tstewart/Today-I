package io.github.tstewart.todayi;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import io.github.tstewart.todayi.sql.DBConstants;
import io.github.tstewart.todayi.sql.Database;
import io.github.tstewart.todayi.ui.dialog.EraseDataDialog;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class OptionsActivity extends AppCompatActivity {

    Button exportDataButton;
    Button googleSignInButton;
    Button eraseAllDataButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        exportDataButton = findViewById(R.id.buttonExportData);
        googleSignInButton = findViewById(R.id.buttonGoogleSignIn);
        eraseAllDataButton = findViewById(R.id.buttonEraseAll);

        if(exportDataButton != null) exportDataButton.setOnClickListener(this::onExportDataButtonClicked);
        if(googleSignInButton != null) googleSignInButton.setOnClickListener(this::onGoogleSignInButtonClicked);
        if(eraseAllDataButton != null) eraseAllDataButton.setOnClickListener(this::eraseButtonClicked);
    }

    private void onExportDataButtonClicked(View view) {
        //TODO
    }

    private void onGoogleSignInButtonClicked(View view) {
        //TODO
    }

    private void eraseButtonClicked(View view) {
        Database database = new Database(this);
        SQLiteDatabase db = database.getWritableDatabase();

        EraseDataDialog dialog = new EraseDataDialog(this);
        dialog.setPositiveClickListener((dialogInterface, which) -> {
            database.eraseAllData(db, DBConstants.ACCOMPLISHMENT_TABLE);

            Toast.makeText(this, R.string.erase_all_confirmed, Toast.LENGTH_LONG).show();

            returnWithResponse(Activity.RESULT_OK);
        });
        dialog.setNegativeButton(null);

        dialog.create().show();
    }

    private void returnWithResponse(int response) {
        Intent returnIntent = new Intent();
        setResult(response,returnIntent);
        finish();
    }

}
