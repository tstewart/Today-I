package io.github.tstewart.todayi.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import java.io.File;

import io.github.tstewart.todayi.data.FileUtils;
import io.github.tstewart.todayi.data.LocalDatabaseIO;
import io.github.tstewart.todayi.errors.ImportFailedException;

public class BackupImportActivity extends FragmentActivity {
    private static final int FILE_REQUEST_CODE = 1;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, FILE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        this.finish();

        Uri uri = null;
        if (resultData != null) {
            uri = resultData.getData();

            try {
                String filePath = FileUtils.getPath(uri, this);
                File backupFile = new File(filePath);

                LocalDatabaseIO.importBackupDbFromFile(getApplicationContext(), backupFile);

            } catch (NullPointerException e) {
                /* If failed, alert user and log */
                Log.w(this.getClass().getSimpleName(), e);

                Toast.makeText(getApplicationContext(), "Failed to read file. You may need to check your file permissions.", Toast.LENGTH_LONG).show();

            } catch (ImportFailedException e) {
                /* If failed, alert user and log */
                Log.w(this.getClass().getSimpleName(), e);

                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

}
