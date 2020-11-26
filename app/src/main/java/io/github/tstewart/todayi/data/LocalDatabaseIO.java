package io.github.tstewart.todayi.data;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;

import io.github.tstewart.todayi.error.ExportFailedException;
import io.github.tstewart.todayi.error.ImportFailedException;
import io.github.tstewart.todayi.event.OnDatabaseInteracted;

/**
 * Local import/export of database information
 */
public class LocalDatabaseIO {
    private static final String CLASS_LOG_TAG = LocalDatabaseIO.class.getSimpleName();
    private static final String DATABASE_BACKUP_DEFAULT_LOCATION = Environment.DIRECTORY_DOCUMENTS;

    private LocalDatabaseIO() {
    }

    public static void export(Context context, String databaseName, String newFileName) throws ExportFailedException {

        File databaseBackupFolder = context.getExternalFilesDir(DATABASE_BACKUP_DEFAULT_LOCATION);

        if (databaseBackupFolder.canWrite()) {
            File currentDatabaseFile = context.getDatabasePath(databaseName);

            if (currentDatabaseFile.exists()) {
                File databaseBackupFile = new File(databaseBackupFolder, newFileName);

                try {
                    writeToPath(currentDatabaseFile, databaseBackupFile);

                    if (!databaseBackupFile.exists()) {
                        throw new ExportFailedException("Potential backup failure? File does not exist.");
                    } else {
                        Log.i(CLASS_LOG_TAG, "Database backed up at " + databaseBackupFile.getAbsolutePath());
                    }
                } catch (IOException e) {
                    throw new ExportFailedException(e.getMessage());
                }
            } else {
                throw new ExportFailedException("Backup failed. Database file to copy does not exist.");
            }
        } else {
            throw new ExportFailedException("Backup failed. Cannot write to internal storage.");
        }

    }

    public static void backup(Context context, String databaseName) throws ExportFailedException {
        export(context, databaseName, "backup_" + databaseName);
    }

    public static void importBackup(Context context, String databaseName) throws ImportFailedException {
        File databaseBackupFolder = context.getExternalFilesDir(DATABASE_BACKUP_DEFAULT_LOCATION);

        if (databaseBackupFolder.canRead()) {
            File databaseBackupFile = new File(databaseBackupFolder, "backup_" + databaseName);

            if (databaseBackupFile.exists() && databaseBackupFile.canRead()) {

                File currentDatabaseFile = context.getDatabasePath(databaseName);

                if (currentDatabaseFile.canWrite()) {
                    if (isValidSQLite(databaseBackupFile.getPath())) {
                        try {
                            writeToPath(databaseBackupFile, currentDatabaseFile);
                            //Notify activities that the existing database has been replaced
                            OnDatabaseInteracted.notifyDatabaseInteracted();

                            Log.i(CLASS_LOG_TAG, "Database restored from " + databaseBackupFile.getAbsolutePath());
                        } catch (IOException e) {
                            throw new ImportFailedException(e.getMessage());
                        }
                    } else {
                        throw new ImportFailedException("Backup was corrupt or invalid.");
                    }
                } else {
                    throw new ImportFailedException("Could not write to existing database.");
                }
            } else {
                throw new ImportFailedException("Database to import does not exist or could not be read.");
            }
        } else {
            throw new ImportFailedException("Database import location could not be read.");
        }
    }

    private static void writeToPath(File inputPath, File outputPath) throws IOException {
        FileChannel input = new FileInputStream(inputPath).getChannel();
        FileChannel output = new FileOutputStream(outputPath).getChannel();

        output.transferFrom(input, 0, input.size());

        input.close();
        output.close();
    }

    /*
    https://stackoverflow.com/questions/39576646/android-check-if-a-file-is-a-valid-sqlite-database
    Author: Drilon Kurti
    TODO implement
     */
    public static boolean isValidSQLite(String dbPath) {
        File databaseFile = new File(dbPath);

        if (!databaseFile.exists() || !databaseFile.canRead()) {
            return false;
        }

        try {
            FileReader fr = new FileReader(databaseFile);
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
