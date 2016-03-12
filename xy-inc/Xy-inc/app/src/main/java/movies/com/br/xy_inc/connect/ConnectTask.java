package movies.com.br.xy_inc.connect;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import movies.com.br.xy_inc.ActPrincipal;
import movies.com.br.xy_inc.bo.adapter.SearchListAdapter;

/**
 * Created by danilo on 10/03/16.
 */
public class ConnectTask extends AsyncTask<Void, Void, Map<String, Object>> {

    private Map<String, String> params = null;
    private String baseUrl = "http://www.omdbapi.com/?";
    private ActPrincipal act = null;
    private SearchListAdapter adapter = null;

    public ConnectTask(Map<String, String> params, ActPrincipal act) {
        this.params = params;
        this.act = act;
    }

    public ConnectTask(Map<String, String> params, SearchListAdapter adapter) {
        this.params = params;
        this.adapter = adapter;
    }

    @Override
    protected Map<String, Object> doInBackground(Void... params) {
        HttpURLConnection conn = null;
        Map<String, Object> map = new HashMap<String, Object>();
        try {

            String response = "";
            URL url = new URL(getUrl(this.params));
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.flush();
            writer.close();
            os.close();
            int responseCode=conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line=br.readLine()) != null) {
                    response+=line;
                }

                Gson gson = new Gson();
                map = (Map<String, Object>)gson.fromJson(response, map.getClass());
            }
            else {
                //erro
            }

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
             if (conn != null) {
                 conn.disconnect();
             }
        }
        return map;
    }

    @Override
    protected void onPostExecute(Map<String, Object> result) {
         if (act != null) {
             List<Map> movies = (List<Map>) result.get("Search");
            act.carregarLista(movies);
        } else if (adapter != null) {
            adapter.salveMovie(result);
        }

    }


    private String getUrl(Map<String, String> params) throws UnsupportedEncodingException {

        StringBuilder urlFinal = new StringBuilder();
        urlFinal.append(baseUrl);
        for (Map.Entry<String, String> param : params.entrySet()) {
            urlFinal.append(param.getKey()).append("=").append(param.getValue()).append("&");
        }

        String url = urlFinal.toString();
        url = url.substring(0, url.length()-1);

        return url;

    }




}