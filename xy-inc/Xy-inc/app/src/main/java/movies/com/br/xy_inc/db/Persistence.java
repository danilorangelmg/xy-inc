package movies.com.br.xy_inc.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by danilo on 10/03/16.
 */
public class Persistence {

    private SQLiteDatabase database;
    private Context context;

    public Persistence() {
        this.context = context;
        createDatabase();
    }

    private void createDatabase() {
        SQLiteOpenHelper helper = new SQLiteOpenHelper(context, "moviesdb", null, 1) {
            @Override
            public void onCreate(SQLiteDatabase db) {

            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            }
        };

        database = helper.getWritableDatabase();
        database.execSQL("PRAGMA synchronous=NORMAL");

    }


    public void beginTransaction() {
        //TODO controlar transacao ja aberta, exception
        database.beginTransaction();
    }

    public void endTransaction() {
        //TODO controlar transac
     }

    //FIXME problema? criar novas colunas fica dificil com o ddl statico
    private void createTableMovie() {
        StringBuilder builder = new StringBuilder();
    }


}
