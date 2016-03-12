package movies.com.br.xy_inc.bo.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Handler;

import movies.com.br.xy_inc.R;
import movies.com.br.xy_inc.util.ViewHolder;

/**
 * Created by danilo on 11/03/16.
 */
public class SearchListAdapter extends BaseAdapter {

    private List<Map> movies = null;
    private LayoutInflater layoutInflater = null;
    private Map<String, Bitmap> posterLoaded = null;

    public SearchListAdapter(List<Map> movies, Context context) {
        this.movies = movies;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        posterLoaded = new HashMap<String, Bitmap>();
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

//        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_search_movies, null);
//        }

        ViewHolder holder = (ViewHolder) convertView.getTag();
        TextView tvTitle = null;
        TextView tvYear = null;
        ImageView poster = null;
        if (holder == null) {
            holder = new ViewHolder();
            tvTitle = (TextView) convertView.findViewById(R.id.list_search_movie_title);
            tvYear = (TextView) convertView.findViewById(R.id.list_search_movie_year);
            poster = (ImageView) convertView.findViewById(R.id.list_search_movie_poster);
            holder.setProperty(tvTitle, "title");
            holder.setProperty(tvYear, "year");
            holder.setProperty(poster, "poster");
            convertView.setTag(holder);
        }

        tvTitle = (TextView) holder.getProperty("title");
        tvYear = (TextView) holder.getProperty("year");
        poster = (ImageView) holder.getProperty("poster");

        Map<String, Object> movie = movies.get(position);

        String title = (String) movie.get("Title");
        String year = (String) movie.get("Year");
        String url = (String) movie.get("Poster");
        tvTitle.setText(title);
        tvYear.setText(year);
        loadImage(url, poster);

        return convertView;
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
            posterLoaded.put(urldisplay, bitmap);

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            this.poster.setImageBitmap(bitmap);

        }
    }


}
