package movies.com.br.xy_inc.connect;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Created by danilo on 14/03/16.
 */
public class DownloadImage extends AsyncTask<String, Void, Bitmap> {

    private ImageView poster = null;
    private Context context = null;
    Map<String, Bitmap> posterLoaded = null;

    public DownloadImage(ImageView imageView, Context context, Map<String, Bitmap> posterLoaded) {
        this.poster = imageView;
        this.context = context;
        //cache das imagens
        this.posterLoaded = posterLoaded;
    }

    @Override
    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap bitmap = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            bitmap = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }

        //se não carregou imagem
        if (bitmap == null) {
            AssetManager assetManager = context.getAssets();
            try {
                String[] files = assetManager.list("");
                InputStream is = assetManager.open(files[1]);
                bitmap = BitmapFactory.decodeStream(is);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        posterLoaded.put(urldisplay, bitmap);

        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        this.poster.setImageBitmap(bitmap);

    }
}

