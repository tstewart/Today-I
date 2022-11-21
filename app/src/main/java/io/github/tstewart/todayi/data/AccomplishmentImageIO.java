package io.github.tstewart.todayi.data;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
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

    public boolean deleteImage() {
        if(mFilePath != null) {
            return mFilePath.getAbsoluteFile().delete();
        }
        return false;
    }
}
