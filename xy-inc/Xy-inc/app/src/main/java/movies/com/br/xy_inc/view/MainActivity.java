package movies.com.br.xy_inc.view;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import movies.com.br.xy_inc.MoviesApplication;
import movies.com.br.xy_inc.R;
import movies.com.br.xy_inc.bo.adapter.SearchListAdapter;
import movies.com.br.xy_inc.connect.ConnectTask;
import movies.com.br.xy_inc.db.Persistence;
import movies.com.br.xy_inc.db.Query;
import movies.com.br.xy_inc.util.Util;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ListView lView = null;
    private SearchListAdapter adapter = null;
    private SearchView searchView = null;
    //preferi usar a flag para consultar os registros salvos do que criar uma nova tela só para isso.
    private boolean isMyMovieList = true;
    private Map<String, SearchListAdapter> adapterMap = null;
    private String lastSearchServer = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_principal);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //novo modelo de permissão para o android 6
        Util.checkPermission(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE});

        adapterMap = new HashMap<String, SearchListAdapter>();
        lView = (ListView) findViewById(R.id.listMovies);
        searchView = (SearchView) findViewById(R.id.search_movie);
        searchMyMovies(null, false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (isMyMovieList) {
                    searchMyMovies(query, false);
                } else {
                    searchMoviesServer(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.act_principal, menu);

        MenuItem search = menu.findItem(R.id.action_search);
        MenuItem list = menu.findItem(R.id.action_list);

        if (isMyMovieList) {
            search.setVisible(true);
            list.setVisible(false);
        } else {
            search.setVisible(false);
            list.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            // Handle the camera action
            if (!lastSearchServer.equals("")) {
                searchMoviesServer(lastSearchServer);
            }
            isMyMovieList = false;
        } else {
            searchMyMovies("", false);
            isMyMovieList = true;
        }

        this.invalidateOptionsMenu();


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_search_movies) {
            // Handle the camera action
            if (!lastSearchServer.equals("")) {
                searchMoviesServer(lastSearchServer);
            }
            isMyMovieList = false;
        } else if (id == R.id.nav_my_list) {
            searchMyMovies("", false);
            isMyMovieList = true;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void carregarLista(List<Map> movies) {
        if (movies != null && movies.size() > 0) {
            adapter = new SearchListAdapter(movies, MainActivity.this, this);
            lView.setAdapter(adapter);
        } else {
            Toast.makeText(MainActivity.this, "A busca não retornou resultados!", Toast.LENGTH_LONG).show();
        }
    }


    public  void searchMyMovies(String param, boolean recarregar) {
        Persistence persistence = MoviesApplication.getApplication().getPersistence();
        String where = "";
        if (param != null) {
            where = persistence.generateWhereLike("title", param);
        }
        Cursor cursor = persistence.find(Query.QUERY_FIND_MOVIE.concat(where));
        if (cursor.getCount() > 0) {
            List<Map> movies = persistence.convertToList(cursor);

            //dava até para usar o add na list, mas teria que percorrer, fica melhor criar um nov adapter
            adapter = new SearchListAdapter(movies, MainActivity.this,this);
            lView.setAdapter(adapter);
        } else {
            Toast.makeText(MainActivity.this, "A busca não retornou resultados!", Toast.LENGTH_LONG).show();
            if (recarregar) {
                adapter = new SearchListAdapter(new ArrayList<Map>(), MainActivity.this,this);
                lView.setAdapter(adapter);
            }
        }
    }

    private void searchMoviesServer(String param) {
        lastSearchServer = param;
        Map<String, String> params = new HashMap<String, String>();
        params.put("s", param);
        ConnectTask task = new ConnectTask(params, MainActivity.this);
        task.execute();
    }

}
