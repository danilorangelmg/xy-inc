package movies.com.br.xy_inc;

import android.Manifest;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import movies.com.br.xy_inc.bo.adapter.SearchListAdapter;
import movies.com.br.xy_inc.connect.ConnectTask;
import movies.com.br.xy_inc.db.Persistence;
import movies.com.br.xy_inc.util.QueryUtil;
import movies.com.br.xy_inc.util.Util;

public class ActPrincipal extends AppCompatActivity
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
        searchMyMovies("");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (isMyMovieList) {
                    searchMyMovies(query);
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

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
            searchMyMovies("");
            isMyMovieList = true;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void carregarLista(List<Map> movies) {
        adapter = new SearchListAdapter(movies, ActPrincipal.this.getApplicationContext());
        lView.setAdapter(adapter);
    }

    public void onLongItemClickListener() {
        lView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Object> movie = (Map<String, Object>) adapter.getItem(position);
                return false;
            }
        });
    }

    private void searchMyMovies(String param) {
        Persistence persistence = MoviesApplication.getApplication().getPersistence();
        String where = "";
        if (param != null) {
            where = persistence.generateWhereLike("title", param);
        }
        Cursor cursor = persistence.find(QueryUtil.QUERY_FIND_MOVIE.concat(where));
        List<Map> movies = persistence.convertToList(cursor);

        adapter = new SearchListAdapter(movies, ActPrincipal.this.getApplicationContext());
        lView.setAdapter(adapter);
    }

    private void searchMoviesServer(String param) {
        lastSearchServer = param;
        Map<String, String> params = new HashMap<String, String>();
        params.put("s", param);
        ConnectTask task = new ConnectTask(params, ActPrincipal.this);
        task.execute();
    }

}
