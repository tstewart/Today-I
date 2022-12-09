package io.github.tstewart.todayi.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

public class PermissionHelper {

    private PermissionHelper() {}

    public static void requestPermission(Activity source, String permission) {
        Context context = source.getApplicationContext();

        if(context != null) {
            int permissionStatus = context.checkCallingOrSelfPermission(permission);

            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(source,
                        new String[]{permission},
                        1);
            }
        } else {
            Log.w(PermissionHelper.class.getSimpleName(), "Could not open permissions request. Missing context.");
        }
    }

    public static boolean permissionGranted(Context context, String permission) {
        return context.checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }
}
