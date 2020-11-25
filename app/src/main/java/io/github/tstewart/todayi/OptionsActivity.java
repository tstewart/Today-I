package io.github.tstewart.todayi;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import io.github.tstewart.todayi.data.LocalDatabaseIO;
import io.github.tstewart.todayi.error.ImportFailedException;
import io.github.tstewart.todayi.sql.DBConstants;
import io.github.tstewart.todayi.sql.Database;
import io.github.tstewart.todayi.ui.dialog.EraseDataDialog;

import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;

public class OptionsActivity extends AppCompatActivity {

    Button importDataButton;
    Button exportDataButton;
    Button restoreBackupButton;
    Button forceBackupButton;
    Button googleSignInButton;
    Button eraseAllDataButton;

    TextView lastBackedUpTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        importDataButton = findViewById(R.id.buttonImportData);
        exportDataButton = findViewById(R.id.buttonExportData);
        restoreBackupButton = findViewById(R.id.buttonRestoreBackup);
        forceBackupButton = findViewById(R.id.buttonForceBackup);
        googleSignInButton = findViewById(R.id.buttonGoogleSignIn);
        eraseAllDataButton = findViewById(R.id.buttonEraseAll);
        lastBackedUpTv = findViewById(R.id.textViewLastBackedUp);

        if(importDataButton != null) importDataButton.setOnClickListener(this::onImportDataButtonClicked);
        if(exportDataButton != null) exportDataButton.setOnClickListener(this::onExportDataButtonClicked);
        if(restoreBackupButton != null) restoreBackupButton.setOnClickListener(this::onRestoreBackupButtonClicked);
        if(forceBackupButton != null) forceBackupButton.setOnClickListener(this::onForceBackupButtonClicked);
        if(googleSignInButton != null) googleSignInButton.setOnClickListener(this::onGoogleSignInButtonClicked);
        if(eraseAllDataButton != null) eraseAllDataButton.setOnClickListener(this::eraseButtonClicked);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if(lastBackedUpTv != null) {
            setLastBackedUpText();
        }
    }

    private void setLastBackedUpText() {
        lastBackedUpTv.setText(String.format(getText(R.string.last_backed_up).toString(), getLastBackedUpRelativeString()));
    }

    private String getLastBackedUpRelativeString() {
        SharedPreferences prefs = getSharedPreferences(getString(R.string.user_prefs_file_location_key), MODE_PRIVATE);

        long lastBackedUp = prefs.getLong(getString(R.string.user_prefs_last_backed_up_key), -1);

        if(lastBackedUp > 0) {
            return DateUtils.getRelativeTimeSpanString(lastBackedUp).toString();
        }
        else {
            return "Unknown";
        }
    }

    private void onRestoreBackupButtonClicked(View view) {
        try {
            LocalDatabaseIO.importBackup(this,DBConstants.DB_NAME);

            //Exit options with result code.
            returnWithResponse(Activity.RESULT_OK);
            Toast.makeText(this,"Backup restored successfully!",Toast.LENGTH_SHORT).show();

        } catch (ImportFailedException e) {
            Log.e(this.getClass().getSimpleName(), Objects.requireNonNull(e.getMessage()));

            Toast.makeText(this,"Failed to import backup:" + e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    private void onForceBackupButtonClicked(View view) {
        LocalDatabaseIO.backup(this, DBConstants.DB_NAME);

        getSharedPreferences(getString(R.string.user_prefs_file_location_key), MODE_PRIVATE)
                .edit()
                .putLong(getString(R.string.user_prefs_last_backed_up_key), System.currentTimeMillis())
                .apply();

        setLastBackedUpText();
    }

    private void onImportDataButtonClicked(View view) {
        Toast.makeText(this,"Coming soon!",Toast.LENGTH_SHORT).show();
        //TODO
    }

    private void onExportDataButtonClicked(View view) {
        Toast.makeText(this,"Coming soon!",Toast.LENGTH_SHORT).show();
        //TODO
    }

    private void onGoogleSignInButtonClicked(View view) {
        Toast.makeText(this,"Coming soon!",Toast.LENGTH_SHORT).show();
        //TODO
    }

    private void eraseButtonClicked(View view) {
        Database database = new Database(this);
        SQLiteDatabase db = database.getWritableDatabase();

        EraseDataDialog dialog = new EraseDataDialog(this)
                .setPositiveClickListener((dialogInterface, which) -> {
                    database.eraseAllData(db, DBConstants.ACCOMPLISHMENT_TABLE);
                    database.eraseAllData(db, DBConstants.RATING_TABLE);

                    Toast.makeText(this, R.string.erase_all_confirmed, Toast.LENGTH_LONG).show();

                    returnWithResponse(Activity.RESULT_OK);
                })
                .setNegativeButton(null);

        dialog.create().show();
    }

    private void returnWithResponse(int response) {
        Intent returnIntent = new Intent();
        setResult(response,returnIntent);
        finish();
    }

}
