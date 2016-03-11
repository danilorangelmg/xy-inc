package movies.com.br.xy_inc.connect;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import movies.com.br.xy_inc.util.Const;

public class DownloadImageTask extends AsyncTask<Void,String, Bitmap> {

    private String urlImage;
    //representa a chave que sera usada para encontrar a imagem posteriormente
    private String key;
    private File image;
    private File rootDir;

    public DownloadImageTask(String url, String key) {
        this.urlImage = url;
        this.key = key;
    }

    @Override
    protected void onPreExecute() {
        String rootPathImage = Const.IMAGE_PATH.concat(File.pathSeparator).concat("movies");
        File rootDir = new File(rootPathImage);

        if (!rootDir.exists()) {
            //se não existir cria o diretoria padrão;
            rootDir.mkdirs();
        }

        image = new File(rootDir.getPath().concat(File.pathSeparator).concat(key));
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        Bitmap bitmap = null;
        HttpURLConnection connection = null;
        InputStream is = null;
        ByteArrayOutputStream out = null;


            if (image.exists()) {
                bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), null);
            } else {
                try {

                connection = (HttpURLConnection) new URL(this.urlImage).openConnection();
//                if (displayProgress) {
//                    connection.connect();
//                    final int length = connection.getContentLength();
//                    if (length <= 0) {
////                        error = new ImageError("Invalid content length. The URL is probably not pointing to a file")
////                                .setErrorCode(ImageError.ERROR_INVALID_FILE);
//                        this.cancel(true);
//                    }
//                    is = new BufferedInputStream(connection.getInputStream(), 8192);
//                    out = new ByteArrayOutputStream();
//                    byte bytes[] = new byte[8192];
//                    int count;
//                    long read = 0;
//                    while ((count = is.read(bytes)) != -1) {
//                        read += count;
//                        out.write(bytes, 0, count);
////                        publishProgress((int) ((read * 100) / length));
//                    }
//                    bitmap = BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size());
//                } else {
                is = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(is);
//                }
            }catch(Throwable e){
                if (!this.isCancelled()) {
//                    error = new ImageError(e).setErrorCode(ImageError.ERROR_GENERAL_EXCEPTION);
                    this.cancel(true);
                }
            }finally{
                try {
                    if (connection != null)
                        connection.disconnect();
                    if (out != null) {
                        out.flush();
                        out.close();
                    }
                    if (is != null)
                        is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (bitmap == null) {
            //erro;
        } else {
            try {
                //salva no cartao
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 0 /*ignored for PNG*/, bos);
                byte[] bitmapdata = bos.toByteArray();
                ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);

               FileOutputStream fos = new FileOutputStream(image);
                int size = 1024*1024;
                byte[] buf = new byte[size];
                int byteRead;
                while (((byteRead = bs.read(buf)) != -1)) {
                    fos.write(buf, 0, byteRead);
                }
                fos.close();
                bs.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}


