package io.github.tstewart.todayi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import io.github.tstewart.todayi.data.LocalDatabaseIO;
import io.github.tstewart.todayi.error.ExportFailedException;
import io.github.tstewart.todayi.error.ImportFailedException;
import io.github.tstewart.todayi.sql.DBConstants;
import io.github.tstewart.todayi.sql.Database;

import android.text.format.DateUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

public class OptionsActivity extends AppCompatActivity {

    private final String CLASS_LOG_TAG = this.getClass().getSimpleName();

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

        if(getSupportActionBar() != null) getSupportActionBar().setTitle(R.string.activity_settings);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if(lastBackedUpTv != null) {
            setLastBackedUpText();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            returnWithResponse(Activity.RESULT_CANCELED);
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        new AlertDialog.Builder(this)
                .setTitle(R.string.button_restore_backup)
                .setMessage(R.string.restore_backup_confirmation)
                .setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Context context = getApplicationContext();
                        try {
                            LocalDatabaseIO.importBackup(context,DBConstants.DB_NAME);

                            //Exit options with result code.
                            returnWithResponse(Activity.RESULT_OK);
                            Toast.makeText(context,"Backup restored successfully!",Toast.LENGTH_SHORT).show();

                        } catch (ImportFailedException e) {
                            Log.w(this.getClass().getSimpleName(), Objects.requireNonNull(e.getMessage()));

                            Toast.makeText(context,"Failed to import backup:" + e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.button_no, null)
                .create()
                .show();

    }

    private void onForceBackupButtonClicked(View view) {
        try {
            LocalDatabaseIO.backup(this, DBConstants.DB_NAME);
        } catch (ExportFailedException e) {
            Log.w(CLASS_LOG_TAG,e.getMessage(), e);
            Toast.makeText(this,"Backup failed: " + e.getMessage(),Toast.LENGTH_LONG).show();
        }

        getSharedPreferences(getString(R.string.user_prefs_file_location_key), MODE_PRIVATE)
                .edit()
                .putLong(getString(R.string.user_prefs_last_backed_up_key), System.currentTimeMillis())
                .apply();

        setLastBackedUpText();
    }

    private void onImportDataButtonClicked(View view) {
        Toast.makeText(this,"Coming soon!",Toast.LENGTH_SHORT).show();
    }

    private void onExportDataButtonClicked(View view) {
        Toast.makeText(this,"Coming soon!",Toast.LENGTH_SHORT).show();
    }

    private void onGoogleSignInButtonClicked(View view) {
        Toast.makeText(this,"Coming soon!",Toast.LENGTH_SHORT).show();
    }

    private void eraseButtonClicked(View view) {
        Database database = new Database(this);
        SQLiteDatabase db = database.getWritableDatabase();

        new AlertDialog.Builder(this)
                .setTitle(R.string.erase_all_warning_dialog_title)
                .setMessage(R.string.erase_all_warning_dialog_message)
                .setPositiveButton(R.string.button_yes, (dialogInterface, which) -> {
                    database.eraseAllData(db, DBConstants.ACCOMPLISHMENT_TABLE);
                    database.eraseAllData(db, DBConstants.RATING_TABLE);

                    Toast.makeText(this, R.string.erase_all_confirmed, Toast.LENGTH_LONG).show();

                    returnWithResponse(Activity.RESULT_OK);
                })
                .setNegativeButton(R.string.button_no, null)
                .create()
                .show();
    }

    private void returnWithResponse(int response) {
        Intent returnIntent = new Intent();
        setResult(response,returnIntent);
        finish();
    }

}
