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

import io.github.tstewart.todayi.errors.ExportFailedException;
import io.github.tstewart.todayi.errors.ImportFailedException;
import io.github.tstewart.todayi.events.OnDatabaseInteracted;

/**
 * Local import/export of database information
 */
public class LocalDatabaseIO {
    /*
     Log tag, used for Logging
     Represents class name
    */
    private static final String CLASS_LOG_TAG = LocalDatabaseIO.class.getSimpleName();
    /* Default backup location for the database file */
    private static final String DATABASE_BACKUP_DEFAULT_LOCATION = Environment.DIRECTORY_DOCUMENTS;

    /* Private constructor prevents initialisation of helper class */
    private LocalDatabaseIO() {
    }

    public static void backupDb(Context context, String databaseName) throws ExportFailedException {
        /* Runs export function, but enforces standard backup file name */
        exportDb(context, databaseName, "backup_" + databaseName);
    }

    /**
     * Exports the provided database to the application's default backup location
     * @param context Environment context, provides the location of objects at runtime
     * @param databaseName Name of the database to be backed up
     * @param newFileName Name of the new backup file
     * @throws ExportFailedException If the export process was interrupted (e.g. If the database could not be read)
     */
    private static void exportDb(Context context, String databaseName, String newFileName) throws ExportFailedException {

        /* Location of folder to write backup file to */
        File databaseBackupFolder = context.getExternalFilesDir(DATABASE_BACKUP_DEFAULT_LOCATION);
        /* Location of existing database file */
        File currentDatabaseFile = context.getDatabasePath(databaseName);
        /* Location to write backup database to */
        File databaseBackupFile = new File(databaseBackupFolder, newFileName);

        /* Check if it is possible to write to the backup folder */
        if (!databaseBackupFolder.canWrite()) {
            throw new ExportFailedException("Backup failed. Cannot write to internal storage.");
        }

        /* If the existing database file exists */
        if (!currentDatabaseFile.exists()) {
            throw new ExportFailedException("Backup failed. Database file to copy does not exist.");
        }

        try {
            /* Try and write existing database file to backup location */
            writeToPath(currentDatabaseFile, databaseBackupFile);

            /* If, after the write process is complete the file does not exist in the new location */
            if (!databaseBackupFile.exists()) {
                throw new ExportFailedException("Potential backup failure? File does not exist.");
            } else {
                /* If the file does exist, backup was successful */
                Log.i(CLASS_LOG_TAG, "Database backed up at " + databaseBackupFile.getAbsolutePath());
            }
        } catch (IOException e) {
            /* Catch IOException, caused by an error in writing the file */
            throw new ExportFailedException(e.getMessage());
        }
    }

    public static void importBackupDb(Context context, String databaseName) throws ImportFailedException {
        /* Runs import function, but enforces standard backup file name */
        importDb(context, databaseName, "backup_" + databaseName);
    }

    private static void importDb(Context context, String databaseName, String backupFileName) throws ImportFailedException {
        /* Location of folder containing the backup file */
        File databaseBackupFolder = context.getExternalFilesDir(DATABASE_BACKUP_DEFAULT_LOCATION);
        /* Location of the backup file */
        File databaseBackupFile = new File(databaseBackupFolder, backupFileName);
        /* Location of the existing database file */
        File currentDatabaseFile = context.getDatabasePath(databaseName);

        /* Check if the database backup file can be read */
        if (!databaseBackupFile.canRead()) {
            throw new ImportFailedException("Database to import does not exist or could not be read.");
        }

        /* Check if the existing database file can be written */
        if (!currentDatabaseFile.canWrite()) {
            throw new ImportFailedException("Could not write to existing database.");
        }

        /* Check if the backup file to load is a valid SQLite database */
        if (!isValidSQLite(databaseBackupFile.getPath())) {
            throw new ImportFailedException("Backup was corrupt or invalid.");
        }

        /* If the database and backup file was valid, attempt backup */
        try {
            /* Replace existing database with backup database */
            writeToPath(databaseBackupFile, currentDatabaseFile);
            /* Notify activities that the existing database has been replaced */
            OnDatabaseInteracted.notifyDatabaseInteracted();

            /* Backup replaced successfully */
            Log.i(CLASS_LOG_TAG, "Database restored from " + databaseBackupFile.getAbsolutePath());
        } catch (IOException e) {
            /* Catch IOException, caused by an error in writing the file */
            throw new ImportFailedException(e.getMessage());
        }
    }

    /**
     * Write file to output path
     * @param inputPath File to read from
     * @param outputPath File to write to
     * @throws IOException If an error occurred in writing the file
     */
    private static void writeToPath(File inputPath, File outputPath) throws IOException {

        /* Use try with resources to auto close input and output stream */
        try (FileChannel input = new FileInputStream(inputPath).getChannel();
             FileChannel output = new FileOutputStream(outputPath).getChannel()) {

            /* Transfer entire file to output */
            output.transferFrom(input, 0, input.size());
        }
        /* Exceptions are not caught as they are thrown in method signature */
    }

    /**
     * isValidSQLite provided by:
     * https://stackoverflow.com/questions/39576646/android-check-if-a-file-is-a-valid-sqlite-database
     *
     * Checks if the file at the provided path is a valid SQLite database or not.
     * @param dbPath Path of file to check
     * @return True if the file is a valid SQLite database, false if not.
     */
    public static boolean isValidSQLite(String dbPath) {
        File databaseFile = new File(dbPath);

        /* If the file doesn't exist or can't be read, then don't check it */
        if (!databaseFile.exists() || !databaseFile.canRead()) {
            return false;
        }

        /* Use try with resources to auto close FileReader */
        try (FileReader fr = new FileReader(databaseFile)) {
            char[] buffer = new char[16];

            /* Read the first 16 chars of the file */
            int bytesRead = fr.read(buffer, 0, 16);

            /* If 16 bytes could be read */
            if(bytesRead >= 16) {
                String str = String.valueOf(buffer);

                /* If the first 16 chars equal the SQLite header, it is most likely a SQLite db. */
                return str.equals("SQLite format 3\u0000");
            }
        } catch (Exception e) {
            e.printStackTrace();
            /* If an exception occurred, we can assume the file is invalid */
            return false;
        }
        return false;
    }
}
