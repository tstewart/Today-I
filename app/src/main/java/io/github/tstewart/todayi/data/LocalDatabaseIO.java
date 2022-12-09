package io.github.tstewart.todayi.data;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    /* Database file name used in the application */
    private static final String DATABASE_FILE_NAME = DBConstants.DB_NAME;

    /* Private constructor prevents initialisation of helper class */
    private LocalDatabaseIO() {
    }

    /**
     * Export the provided database to the default backup location with the default name for backups (backup_(filename))
     *
     * @param context Environment context, provides the location of objects at runtime
     * @throws ExportFailedException If the export process was interrupted (e.g. If the database could not be read)
     */
    public static void backupDb(Context context) throws ExportFailedException {
        /* Runs export function, but enforces standard backup file name */

        /* Location of folder to write backup file to */
        File databaseBackupFolder = context.getExternalFilesDir(DATABASE_BACKUP_DEFAULT_LOCATION);

        exportDb(context, "backup_" + DATABASE_FILE_NAME + ".db", databaseBackupFolder);
    }

    /**
     * Export the provided database to the chosen location
     *
     * @param context        Environment context, provides the location of objects at runtime
     * @param backupLocation Location to back up the database to
     */
    public static void exportDbToFile(Context context, File backupLocation) throws ExportFailedException {
        exportDb(context, backupLocation.getName(), new File(backupLocation.getParent()));
    }

    /**
     * Exports the provided database to the provided export folder
     *
     * @param context      Environment context, provides the location of objects at runtime
     * @param newFileName  Name of the new backup file
     * @param exportFolder Folder to export database to
     * @throws ExportFailedException If the export process was interrupted (e.g. If the database could not be read)
     */
    private static void exportDb(Context context, String newFileName, File exportFolder) throws ExportFailedException {
        /* Location of existing database file */
        File currentDatabaseFile = context.getDatabasePath(DATABASE_FILE_NAME);
        /* Location to write backup database to */
        File databaseBackupFile = new File(exportFolder, newFileName);

        /* Check if it is possible to write to the backup folder */
        if (!exportFolder.canWrite()) {
            throw new ExportFailedException("Cannot write to downloads folder. Check your permissions.");
        }

        /* If the existing database file exists */
        if (!currentDatabaseFile.exists()) {
            throw new ExportFailedException("Database file to copy does not exist.");
        }

        try {
            /* Try and write existing database file to backup location */
            writeToPath(currentDatabaseFile, databaseBackupFile, context);

            /* If, after the write process is complete the file does not exist in the new location */
            if (!databaseBackupFile.exists()) {
                throw new ExportFailedException("Potential backup failure? File does not exist.");
            } else {
                /* If the file does exist, backup was successful */
                Log.i(CLASS_LOG_TAG, "Database backed up at " + databaseBackupFile.getAbsolutePath());
            }
        } catch (IOException e) {
            /* Catch IOException, caused by an error in writing the file */
            e.printStackTrace();
            throw new ExportFailedException(e.getMessage());
        }
    }

    /**
     * Import the provided database from the default backup location with the default name for backups (backup_(filename))
     *
     * @param context Environment context, provides the location of objects at runtime
     * @throws ImportFailedException If the import process was interrupted (e.g. If the database could not be read)
     */
    public static void importBackupDb(Context context) throws ImportFailedException {
        /* Location of folder to read backup file from */
        File databaseBackupFolder = context.getExternalFilesDir(DATABASE_BACKUP_DEFAULT_LOCATION);

        /* Location of the backup file */
        File databaseBackupFile = new File(databaseBackupFolder, "backup_" + DATABASE_FILE_NAME + ".db");

        /* Runs import function, but enforces standard backup file name */
        importDb(context, databaseBackupFile);
    }

    /**
     * Import the provided database from the provided backup file
     *
     * @param context      Environment context, provides the location of objects at runtime
     * @param databaseFile Database file to import into the application
     * @throws ImportFailedException If the import process was interrupted (e.g. If the database could not be read)
     */
    public static void importBackupDbFromFile(Context context, File databaseFile) throws ImportFailedException {
        /* Runs import function, but enforces standard backup file name */
        importDb(context, databaseFile);
    }

    /**
     * Import the provided database from the application's default backup location
     *
     * @param context    Environment context, provides the location of objects at runtime
     * @param backupFile Path of the backup to import
     * @throws ImportFailedException If the import process was interrupted (e.g. If the database could not be read)
     */
    private static void importDb(Context context, File backupFile) throws ImportFailedException {

        /* Location of the existing database file */
        File currentDatabaseFile = context.getDatabasePath(DATABASE_FILE_NAME);

        /* Check if the existing database file can be written */
        if (!currentDatabaseFile.canWrite()) {
            throw new ImportFailedException("Could not write to existing database.");
        }

        /* Check if the backup file to load is a valid SQLite database */
        if (!isValidSQLite(backupFile.getPath())) {
            throw new ImportFailedException("Backup was corrupt or invalid.");
        }

        /* If the database and backup file was valid, attempt backup */
        try {
            /* Replace existing database with backup database */
            writeToPath(backupFile, currentDatabaseFile, context);
            /* Notify activities that the existing database has been replaced */
            OnDatabaseInteracted.notifyDatabaseInteracted();

            /* Backup replaced successfully */
            Log.i(CLASS_LOG_TAG, "Database restored from " + backupFile.getAbsolutePath());
        } catch (IOException e) {
            /* Catch IOException, caused by an error in writing the file */
            throw new ImportFailedException(e.getMessage());
        }
    }

    /**
     * Write file to output path
     *
     * @param inputPath  File to read from
     * @param outputPath File to write to
     * @throws IOException If an error occurred in writing the file
     */
    private static void writeToPath(File inputPath, File outputPath, Context context) throws IOException {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            final ContentResolver resolver = context.getContentResolver();
            try (InputStream in = resolver.openInputStream(Uri.fromFile(inputPath));
                 OutputStream out = resolver.openOutputStream(Uri.fromFile(outputPath))) {

                byte[] buffer = new byte[1024];
                int len = in.read(buffer);
                while (len != -1) {
                    out.write(buffer, 0, len);
                    len = in.read(buffer);
                }
            }
        }

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
     * <p>
     * Checks if the file at the provided path is a valid SQLite database or not.
     *
     * @param dbPath Path of file to check
     * @return True if the file is a valid SQLite database, false if not.
     */
    public static boolean isValidSQLite(String dbPath) {
        File databaseFile = new File(dbPath);

        /* Use try with resources to auto close FileReader */
        try (FileReader fr = new FileReader(databaseFile)) {
            char[] buffer = new char[16];

            /* Read the first 16 chars of the file */
            int bytesRead = fr.read(buffer, 0, 16);

            /* If 16 bytes could be read */
            if (bytesRead >= 16) {
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
