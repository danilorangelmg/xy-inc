package movies.com.br.xy_inc.bo.adapter;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Handler;

import movies.com.br.xy_inc.MoviesApplication;
import movies.com.br.xy_inc.R;
import movies.com.br.xy_inc.connect.ConnectTask;
import movies.com.br.xy_inc.util.Const;
import movies.com.br.xy_inc.util.OnTouchViewListener;
import movies.com.br.xy_inc.util.ViewHolder;

/**
 * Created by danilo on 11/03/16.
 */
public class SearchListAdapter extends BaseAdapter {

    private List<Map> movies = null;
    private LayoutInflater layoutInflater = null;
    private Map<String, Bitmap> posterLoaded = null;
    private Map<String, TextView> plotViews = null;
    private Context context = null;
    private boolean isPlot = false;

    public SearchListAdapter(List<Map> movies, Context context) {
        this.movies = movies;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        posterLoaded = new HashMap<String, Bitmap>();
        plotViews = new HashMap<String, TextView>();
        this.context = context;
    }


    @Override
    public int getCount() {
        return movies.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = layoutInflater.inflate(R.layout.list_search_movies, null);

        TextView tvTitle = (TextView) convertView.findViewById(R.id.list_search_movie_title);
        TextView tvYear = (TextView) convertView.findViewById(R.id.list_search_movie_year);
        ImageView poster = (ImageView) convertView.findViewById(R.id.list_search_movie_poster);
        TextView tvSinopse = (TextView) convertView.findViewById(R.id.list_search_movie_sinopse);
        TextView tvPlot = (TextView) convertView.findViewById(R.id.list_search_movie_plot);
        ImageButton addButton = (ImageButton) convertView.findViewById(R.id.list_search_movie_add);
        View scrollView = (View) convertView.findViewById(R.id.list_search_movie_scroll);

        Map<String, Object> movie = movies.get(position);

        String title = "";
        String year = "";
        String url = "";

        if (movie.containsKey("image_path")) {
            title = (String) movie.get("title");
            year = (String) movie.get("year");
            url = (String) movie.get("poster");
        } else {
            title = (String) movie.get("Title");
            year = (String) movie.get("Year");
            url = (String) movie.get("Poster");
        }

        tvTitle.setText(title);
        tvYear.setText(year);

        View container = (View) convertView.findViewById(R.id.list_search_movie_container);

        String path = movie.get("image_path") == null ? null : (String) movie.get("image_path");

        loadImage(url, poster, path);
        buttonAddListener(addButton, (String) movie.get("imdbID"));
        container.setOnClickListener(new SinopseClickListener(tvPlot, tvSinopse, scrollView,poster, (String) movie.get("imdbID")));
        container.setOnTouchListener(new OnTouchViewListener());
        return convertView;
    }


    private void buttonAddListener(ImageButton addButton, final String imdbid) {
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> params = new HashMap<String, String>();
                params.put("i", imdbid);
                params.put("plot", "full");
                ConnectTask task = new ConnectTask(params, SearchListAdapter.this);
                task.execute();
            }
        });

        //para dar o efeito de click do botao
        addButton.setOnTouchListener(new OnTouchViewListener());
    }

    public void onConnectResult(Map<String, Object> movie) {
        if (isPlot) {
            String id = (String) movie.get("imdbID");
            plotViews.get(id).setText((String) movie.get("Plot"));
            isPlot = false;
        } else {
            saveMovie(movie);
        }
    }

    private void saveMovie(Map<String, Object> movie) {
        Map<String, Object> moviesNameLowerCase = new HashMap<String, Object>();
        for (Map.Entry<String, Object> value :movie.entrySet()) {
            //para ajustar no mesmo formato do banco
            if (value.getKey().equals("pk")) {
                continue;
            }
            moviesNameLowerCase.put(value.getKey().toLowerCase(), value.getValue());
        }

        String path = "";
        try {
            String id = (String)moviesNameLowerCase.get("imdbid");
            Bitmap bitmap = (Bitmap) posterLoaded.get(moviesNameLowerCase.get("poster"));
            path = salveImage(id, bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        moviesNameLowerCase.put("image_path", path);

        try {
            MoviesApplication.getApplication().getPersistence().save("tb_movies", moviesNameLowerCase);
        } catch (SQLiteConstraintException e) {
            Log.w("Movie", "Error saving record already exists!");
            Toast.makeText(context, "Esse registro já foi adicionado!", Toast.LENGTH_LONG).show();
        }


        Log.i("Movie", "Save done!");
        Toast.makeText(context, "Registro adicionado com sucesso!" ,Toast.LENGTH_LONG).show();
    }

    private String salveImage(String imdbid, Bitmap bitmap) throws FileNotFoundException {

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



    private void loadImage(final String url, final ImageView poster, final String path) {
        android.os.Handler handler = new android.os.Handler();
        if (path != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Bitmap image = BitmapFactory.decodeFile(path, null);
                    poster.setImageBitmap(image);
                }
            });

        } else {
            if (posterLoaded.containsKey(url)) {
                poster.setImageBitmap(posterLoaded.get(url));
            } else {

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        new DownloadImage(poster).execute(url);
                    }
                });
            }
        }
    }

    private class DownloadImage extends AsyncTask<String, Void, Bitmap> {

        private ImageView poster = null;

        DownloadImage(ImageView imageView) {
            this.poster = imageView;
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

    private class SinopseClickListener implements View.OnClickListener {

        private TextView plotView = null;
        private TextView sinopseView = null;
        private View view = null;
        private boolean isVisible = false;
        private String imdbID = null;
        private ImageView posterView = null;

        public SinopseClickListener(TextView plotView, TextView sinopseView, View view, ImageView posterView, String imdbID) {
            this.sinopseView = sinopseView;
            this.plotView = plotView;
            this.imdbID = imdbID;
            this.view = view;
            this.posterView = posterView;
        }

        @Override
        public void onClick(View v) {
            if (!isVisible) {
                isPlot = true;
                view.setVisibility(View.VISIBLE);
                posterView.setVisibility(View.GONE);
                sinopseView.setText("Sinopse-");

                //guarda a view do plot para utilizar ela no resultado da busca
                if (!plotViews.containsKey(imdbID)) {
                    plotViews.put(imdbID, plotView);
                }

                Map<String, String> params = new HashMap<String, String>();
                params.put("i", imdbID);
                params.put("plot", "full");

                //vai buscar todos os dados somente quando for solicitado
                ConnectTask task = new ConnectTask(params, SearchListAdapter.this);
                task.execute();
            } else {
                plotView.setText("");
                view.setVisibility(View.GONE);
                posterView.setVisibility(View.VISIBLE);
                sinopseView.setText("Sinopse+");
            }

            isVisible = !isVisible;
        }
    }


}
