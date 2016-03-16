package movies.com.br.xy_inc.bo.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import movies.com.br.xy_inc.MoviesApplication;
import movies.com.br.xy_inc.R;
import movies.com.br.xy_inc.connect.ConnectTask;
import movies.com.br.xy_inc.connect.DownloadImage;
import movies.com.br.xy_inc.util.OnTouchViewListener;
import movies.com.br.xy_inc.util.Util;
import movies.com.br.xy_inc.view.MainActivity;

/**
 * Created by danilo on 11/03/16.
 */
public class SearchListAdapter extends BaseAdapter {

    private List<Map> movies = null;
    private LayoutInflater layoutInflater = null;
    private Map<String, Bitmap> posterLoaded = null;
    private Context context = null;
    private boolean isPlot = false;
    private MainActivity activity = null;

    public SearchListAdapter(List<Map> movies, Context context, MainActivity activity) {
        this.movies = movies;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        posterLoaded = new HashMap<String, Bitmap>();
        this.context = context;
        this.activity = activity;
    }


    @Override
    public int getCount() {
        return movies.size();
    }

    @Override
    public Object getItem(int position) {
        return movies.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = layoutInflater.inflate(R.layout.list_search_movies, null);

        Map<String, Object> movie = movies.get(position);

        if (movie.containsKey("image_path")) {
            loadViewList(movie, convertView);
        } else {
            loadViewSearch(movie, convertView);
        }

        return convertView;
    }

    private void loadViewSearch(Map<String, Object> movie, View convertView) {

        TextView tvTitle = (TextView) convertView.findViewById(R.id.list_search_movie_title);
        TextView tvYear = (TextView) convertView.findViewById(R.id.list_search_movie_year);
        TextView tvGenre = (TextView) convertView.findViewById(R.id.list_search_movie_genre);

        ImageView poster = (ImageView) convertView.findViewById(R.id.list_search_movie_poster);
        ImageButton addRemoveButton = (ImageButton) convertView.findViewById(R.id.list_search_movie_add);

        String title = (String) movie.get("Title");
        String year = (String) movie.get("Year");
        String url = (String) movie.get("Poster");
        String genre = (String) movie.get("genre");
        addRemoveButton.setImageResource(R.drawable.add);

        tvTitle.setText(title);
        tvYear.setText(year);
        tvGenre.setText(genre);

        View container = (View) convertView.findViewById(R.id.list_search_movie_container);

        loadImage(url, poster, null);
        buttonAddListener(addRemoveButton, (String) movie.get("imdbID"),null, false);
        container.setOnClickListener(new SinopseClickListener(movie));
        container.setOnTouchListener(new OnTouchViewListener());

    }

    private void loadViewList(Map<String, Object> movie, View convertView) {

        TextView tvTitle = (TextView) convertView.findViewById(R.id.list_search_movie_title);
        TextView tvYear = (TextView) convertView.findViewById(R.id.list_search_movie_year);
        TextView tvGenre = (TextView) convertView.findViewById(R.id.list_search_movie_genre);

        ImageView poster = (ImageView) convertView.findViewById(R.id.list_search_movie_poster);

        ImageButton addRemoveButton = (ImageButton) convertView.findViewById(R.id.list_search_movie_add);

        String title = (String) movie.get("title");
        String year = (String) movie.get("year");
        String url = (String) movie.get("poster");
        String genre = (String) movie.get("genre");
        addRemoveButton.setImageResource(R.drawable.remove);

        tvTitle.setText(title);
        tvYear.setText(year);
        tvGenre.setText(genre);

        View container = (View) convertView.findViewById(R.id.list_search_movie_container);

        String path = (String) movie.get("image_path");

        loadImage(url, poster, path);
        buttonAddListener(addRemoveButton, (String) movie.get("imdbid"),path, true);
        container.setOnClickListener(new SinopseClickListener(movie));
        container.setOnTouchListener(new OnTouchViewListener());
    }

    private void buttonAddListener(ImageButton addButton, final String imdbid, final String path, final boolean remove) {
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (remove) {
                    Map<String, String> where = new HashMap<String, String>();
                    where.put("imdbid", imdbid);
                    MoviesApplication.getApplication().getPersistence().delete("tb_movies", "imdbid='" + imdbid + "'", null);
                    Toast.makeText(context, "Registro removido com sucesso!", Toast.LENGTH_LONG).show();

                    Util.removeImage(path);

                    //recarrega toda a lista
                    activity.searchMyMovies(null, true);
                } else {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("i", imdbid);
                    params.put("plot", "full");
                    ConnectTask task = new ConnectTask(params, SearchListAdapter.this);
                    task.execute();
                }
            }
        });

        //para dar o efeito de click do botao
        addButton.setOnTouchListener(new OnTouchViewListener());
    }

    public void onConnectResult(Map<String, Object> movie) {
        if (isPlot) {
            showSinopse(movie);
        } else {
            saveMovie(movie);
        }
    }

    private void showSinopse(Map<String, Object> movie) {
        isPlot = false;
        View view = layoutInflater.inflate(R.layout.sinopse_dialog, null);
        TextView title = (TextView) view.findViewById(R.id.dialog_title);
        TextView body = (TextView) view.findViewById(R.id.dialog_sinopse);
        ImageView close = (ImageView) view.findViewById(R.id.dialog_close);

        String titleStr = (String) (movie.get("Title") == null ? movie.get("title") : movie.get("Title"));
        String bodyStr = (String) (movie.get("Plot") == null ? movie.get("plot") : movie.get("Plot"));

        title.setText(titleStr);
        body.setText(bodyStr);
        close.setOnTouchListener(new OnTouchViewListener());

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        final AlertDialog alert = builder.create();
        builder.show();

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });
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
            path = Util.salveImage(id, bitmap);
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
                        new DownloadImage(poster, context, posterLoaded).execute(url);
                    }
                });
            }
        }
    }

    private class SinopseClickListener implements View.OnClickListener {

        Map<String, Object> movie = null;

        public SinopseClickListener(Map<String, Object> movie) {
            this.movie = movie;
        }

        @Override
        public void onClick(View v) {
            isPlot = true;
            Map<String, String> params = new HashMap<String, String>();
            String path = movie.get("image_path") == null ? null : (String) movie.get("image_path");

            if (movie.containsKey("image_path")) {
                onConnectResult(movie);
            } else {
                params.put("i", (String) movie.get("imdbID"));
                params.put("plot", "full");

                //vai buscar todos os dados somente quando for solicitado
                ConnectTask task = new ConnectTask(params, SearchListAdapter.this);
                task.execute();
            }

        }
    }

}
