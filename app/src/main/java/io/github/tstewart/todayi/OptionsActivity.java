package io.github.tstewart.todayi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import io.github.tstewart.todayi.data.LocalDatabaseIO;
import io.github.tstewart.todayi.error.ImportFailedException;
import io.github.tstewart.todayi.sql.DBConstants;
import io.github.tstewart.todayi.sql.Database;

import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

public class OptionsActivity extends AppCompatActivity {

    // DEBUG ACTIVITY VARS
    // Number of taps on the version TextView required to open debug menu
    private final int DEBUG_ACTIVITY_TAP_REQUIREMENT = 5;
    // Current tap count
    private int debug_activity_tap_count = 0;
    //

    Button importDataButton;
    Button exportDataButton;
    Button restoreBackupButton;
    Button forceBackupButton;
    Button googleSignInButton;
    Button eraseAllDataButton;

    TextView lastBackedUpTv;
    TextView currentVersionTv;

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
        currentVersionTv = findViewById(R.id.textViewAboutVersion);

        if(importDataButton != null) importDataButton.setOnClickListener(this::onImportDataButtonClicked);
        if(exportDataButton != null) exportDataButton.setOnClickListener(this::onExportDataButtonClicked);
        if(restoreBackupButton != null) restoreBackupButton.setOnClickListener(this::onRestoreBackupButtonClicked);
        if(forceBackupButton != null) forceBackupButton.setOnClickListener(this::onForceBackupButtonClicked);
        if(googleSignInButton != null) googleSignInButton.setOnClickListener(this::onGoogleSignInButtonClicked);
        if(eraseAllDataButton != null) eraseAllDataButton.setOnClickListener(this::eraseButtonClicked);
        if(currentVersionTv != null) {
            String currentVersion = getCurrentVersion();
            currentVersionTv.setText(String.format(getString(R.string.about_version), currentVersion));

            // Add onClick listener to access debug
            currentVersionTv.setOnClickListener(this::OnDebugViewClickedListener);
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if(lastBackedUpTv != null) {
            setLastBackedUpText();
        }
    }

    private String getCurrentVersion() {
        Context appContext = getApplicationContext();
        if(appContext != null) {
            try {
                PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
                return pInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return "Unknown";
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

    private void OnDebugViewClickedListener(View view) {
        debug_activity_tap_count++;

        if(debug_activity_tap_count >= DEBUG_ACTIVITY_TAP_REQUIREMENT) {
            Intent intent = new Intent(this, DebugActivity.class);
            startActivity(intent);

            debug_activity_tap_count = 0;
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
                            Log.e(this.getClass().getSimpleName(), Objects.requireNonNull(e.getMessage()));

                            Toast.makeText(context,"Failed to import backup:" + e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.button_no, null)
                .create()
                .show();

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
