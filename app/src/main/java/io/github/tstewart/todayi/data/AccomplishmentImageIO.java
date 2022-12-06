package io.github.tstewart.todayi.data;

import static android.os.Environment.DIRECTORY_PICTURES;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

public class AccomplishmentImageIO {

    private final File mFilePath;
    private final Context mContext;

    public AccomplishmentImageIO(Context context, File filePath) {
        this.mContext = context;
        this.mFilePath = filePath;
    }

    public void saveImage(Bitmap image) throws IOException {
        if(mFilePath != null) {
            FileOutputStream fos = new FileOutputStream(mFilePath);
            image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        }
    }

    public void saveImageThumbnail(Bitmap image) throws IOException {
        if(mFilePath != null) {
            FileOutputStream fos = new FileOutputStream(mFilePath);
            Bitmap thumbImage = ThumbnailUtils.extractThumbnail(image, image.getWidth()/10, image.getHeight()/10);
            thumbImage.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
        }
    }

    public boolean deleteImage() {
        if(mFilePath != null) {
            return mFilePath.getAbsoluteFile().delete();
        }
        return false;
    }

    /* Author: Barmaley
    * Link: https://stackoverflow.com/a/6449092 */
    public static File createTemporaryFile(Context context, String name, String ext) throws IOException
    {
        File tempDir = context.getExternalFilesDir(DIRECTORY_PICTURES);
        tempDir=new File(tempDir.getAbsolutePath()+"/.temp/");
        if(!tempDir.exists())
        {
            boolean created = tempDir.mkdirs();
            if(!created) {
                Log.w(AccomplishmentImageIO.class.getSimpleName(),"Couldn't create temporary directory.");
            }
        }
        return File.createTempFile(name, ext, tempDir);
    }
}
