package movies.com.br.xy_inc.bo.adapter;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Handler;

import movies.com.br.xy_inc.MoviesApplication;
import movies.com.br.xy_inc.R;
import movies.com.br.xy_inc.connect.ConnectTask;
import movies.com.br.xy_inc.util.ViewHolder;

/**
 * Created by danilo on 11/03/16.
 */
public class SearchListAdapter extends BaseAdapter {

    private List<Map> movies = null;
    private LayoutInflater layoutInflater = null;
    private Map<String, Bitmap> posterLoaded = null;
    private Context context = null;

    public SearchListAdapter(List<Map> movies, Context context) {
        this.movies = movies;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        posterLoaded = new HashMap<String, Bitmap>();
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
        ImageButton addButton = (ImageButton) convertView.findViewById(R.id.list_search_movie_add);

        Map<String, Object> movie = movies.get(position);

        String title = (String) movie.get("Title");
        String year = (String) movie.get("Year");
        String url = (String) movie.get("Poster");
        tvTitle.setText(title);
        tvYear.setText(year);
        loadImage(url, poster);
        buttonAddListener(addButton, (String) movie.get("imdbID"));

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
    }

    public void salveMovie(Map<String, Object> movie) {
        Map<String, Object> moviesNameLowerCase = new HashMap<String, Object>();
        for (Map.Entry<String, Object> value :movie.entrySet()) {
            //para ajustar no mesmo formato do banco
            if (value.getKey().equals("pk")) {
                continue;
            }
            moviesNameLowerCase.put(value.getKey().toLowerCase(), value.getValue());
        }
        try {
            MoviesApplication.getApplication().getPersistence().save("tb_movies", moviesNameLowerCase);
        } catch (SQLiteConstraintException e) {
            Log.w("Movie", "Error saving record already exists!");
            Toast.makeText(context, "Esse registro já foi adicionado!" ,Toast.LENGTH_LONG).show();
        }
        Log.i("Movie", "Save done!");
    }


    private void loadImage(final String url, final ImageView poster) {

        if (posterLoaded.containsKey(url)) {
            poster.setImageBitmap(posterLoaded.get(url));
        } else {

            android.os.Handler handler = new android.os.Handler();

            handler.post(new Runnable() {
                @Override
                public void run() {
                    new DownloadImage(poster).execute(url);
                }
            });
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


}
