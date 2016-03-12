package movies.com.br.xy_inc;

import android.app.Application;

import movies.com.br.xy_inc.db.Persistence;

/**
 * Created by danilo on 10/03/16.
 */
public class MoviesApplication extends Application {


    private static MoviesApplication instance;
    private Persistence persistence;

    public MoviesApplication() {
        instance = this;
    }

    public static MoviesApplication getApplication() {
        return instance;
    }

    public Persistence getPersistence() {
        return persistence;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        //assim que a aplicação é criada, cria tambem a camada de persistencia
        persistence = new Persistence(getApplicationContext());
    }
}
