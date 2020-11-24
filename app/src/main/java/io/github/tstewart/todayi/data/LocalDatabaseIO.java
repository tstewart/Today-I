package io.github.tstewart.todayi.data;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import io.github.tstewart.todayi.sql.DBConstants;

/**
 * Local import/export of database information
 */
public class LocalDatabaseIO {
    private static final String CLASS_LOG_TAG = LocalDatabaseIO.class.getSimpleName();

    private LocalDatabaseIO(){}

    public static void export(Context context, String databaseName, String newFileName)  {

        File storage = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);

        if(storage.canWrite()) {
            File databaseFile = context.getDatabasePath(databaseName);

            if(databaseFile.exists()) {
                File backupFile = new File(storage, newFileName);

                try {
                    FileChannel input = new FileInputStream(databaseFile).getChannel();
                    FileChannel output = new FileOutputStream(backupFile).getChannel();

                    output.transferFrom(input,0,input.size());

                    input.close();
                    output.close();
                } catch (IOException e) {
                    //TODO MANAGE
                    Log.e(CLASS_LOG_TAG, e.getMessage());
                }
                finally {
                    if(!backupFile.exists()) {
                        Log.e(CLASS_LOG_TAG,"Potential backup failure? File does not exist.");
                    }
                    else {
                        Log.i(CLASS_LOG_TAG,"New file created at " + backupFile.getAbsolutePath());
                    }
                }
            }
            else {
                Log.e(CLASS_LOG_TAG, "Backup failed. Database file to copy does not exist.");
            }
        }
        else {
            Log.e(CLASS_LOG_TAG, "Backup failed. Cannot write to internal storage.");
        }

    }

    public static void backup(Context context, String databaseName) {
        //Append .db if the string doesn't contain it already.
        //if(!databaseName.endsWith(".db")) databaseName += ".db";

        export(context, databaseName, "backup_"+databaseName);
    }
}
