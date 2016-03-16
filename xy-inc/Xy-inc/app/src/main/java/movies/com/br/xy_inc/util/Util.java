package movies.com.br.xy_inc.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by danilo on 13/03/16.
 */
public class Util {

    //checa e pede permissão
    public static void checkPermission(Activity activity, String[] permissions) {
//        Manifest.permission.READ_CONTACTS
        for (String permission : permissions) {

            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        permission)) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(activity,
                            new String[]{permission},
                            0);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }
        }
    }


    public static String salveImage(String imdbid, Bitmap bitmap) throws FileNotFoundException {

        String path = "";

        File rootDir = new File(Const.IMAGE_PATH);
        if (!rootDir.exists()) {
            rootDir.mkdir();
        }

        try {
            File image = new File(rootDir.getAbsolutePath(), imdbid.concat(".jpg"));

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

            image.createNewFile();
            FileOutputStream fo = new FileOutputStream(image);
            fo.write(bytes.toByteArray());

            fo.close();

            path = image.getAbsolutePath();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return path;
    }


    public static void removeImage(String path) {
        File removeFile = new File(path);
        removeFile.delete();
    }

}