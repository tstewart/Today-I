package io.github.tstewart.todayi.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;

import io.github.tstewart.todayi.error.ImportFailedException;
import io.github.tstewart.todayi.event.OnDatabaseInteracted;
import io.github.tstewart.todayi.sql.Database;

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
                    writeToPath(databaseFile, backupFile);
                } catch (IOException e) {
                    //TODO MANAGE
                    Log.e(CLASS_LOG_TAG, e.getMessage());
                }
                finally {
                    if(!backupFile.exists()) {
                        Log.e(CLASS_LOG_TAG,"Potential backup failure? File does not exist.");
                    }
                    else {
                        Log.i(CLASS_LOG_TAG,"Database backed up at " + backupFile.getAbsolutePath());
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

    public static void importBackup(Context context, String databaseName) throws ImportFailedException {
        File dbBackupLocation = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);

        if(dbBackupLocation.canRead()) {
            File dbBackup = new File(dbBackupLocation, "backup_"+databaseName);

            if(dbBackup.exists() && dbBackup.canRead()) {

                File existingDBLocation = context.getDatabasePath(databaseName);

                if(existingDBLocation.canWrite()) {
                    try {
                        writeToPath(dbBackup, existingDBLocation);
                        //Notify activities that the existing database has been replaced
                        OnDatabaseInteracted.notifyDatabaseInteracted();

                        Log.i(CLASS_LOG_TAG,"Database restored from " + dbBackup.getAbsolutePath());
                    } catch (IOException e) {
                        throw new ImportFailedException(e.getMessage());
                    }
                }
                else {
                    throw new ImportFailedException("Could not write to existing database.");
                }
            }
            else {
                throw new ImportFailedException("Database to import does not exist or could not be read.");
            }
        }
        else {
            throw new ImportFailedException("Database import location could not be read.");
        }
    }

    private static void writeToPath(File inputPath, File outputPath) throws IOException {
        FileChannel input = new FileInputStream(inputPath).getChannel();
        FileChannel output = new FileOutputStream(outputPath).getChannel();

        output.transferFrom(input,0,input.size());

        input.close();
        output.close();
    }

    /*
    https://stackoverflow.com/questions/39576646/android-check-if-a-file-is-a-valid-sqlite-database
    Author: Drilon Kurti
     */
    public static boolean isValidSQLite(String dbPath) {
        File file = new File(dbPath);

        if (!file.exists() || !file.canRead()) {
            return false;
        }

        try {
            FileReader fr = new FileReader(file);
            char[] buffer = new char[16];

            int bytesRead = fr.read(buffer, 0, 16);
            String str = String.valueOf(buffer);
            fr.close();

            return str.equals("SQLite format 3\u0000");

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
