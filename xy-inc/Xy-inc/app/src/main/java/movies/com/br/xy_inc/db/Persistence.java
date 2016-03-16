package movies.com.br.xy_inc.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import movies.com.br.xy_inc.exception.ValidationException;

/**
 * Created by danilo on 10/03/16.
 */
public class Persistence {

    private SQLiteDatabase database;
    private Context context;

    public Persistence(Context context) {
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

        //a partir daqui cria a(s) tabela(s)
        try {
            createTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    public void beginTransaction() {
        //TODO controlar transacao ja aberta, exception
        database.beginTransaction();
    }

    public void endTransaction() {
        //TODO controlar transac
        try {
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
     }

    //FIXME problema? criar novas colunas fica dificil com o ddl statico
    private void createTables() throws SQLException {

        Map<String, Map<String, String>> tables = Tables.getTables();

        for (Map.Entry<String, Map<String, String>> table : tables.entrySet()) {
            StringBuilder builder = new StringBuilder();
            builder.append("CREATE TABLE IF NOT EXISTS ");
            builder.append(table.getKey()).append("(");

            //começa a percorrer as colunas
            for(Map.Entry<String, String> column: table.getValue().entrySet()) {
                if (column.getKey().equals("pk")) {
                    builder.append("PRIMARY KEY(").append(column.getValue()).append("))");
                } else {
                    builder.append(column.getKey()).append(" ").append(column.getValue()).append(",");
                }
            }

            try {
                validateTable(table.getKey(), table.getValue());
            } catch (ValidationException e) {
                Log.i("Movie", e.getMessage());
                Log.i("Movie", "Recreate table "+table.getKey());
                dropTable(table.getKey());
            }

            database.execSQL(builder.toString());
        }

    }

    public void dropTable(String tableName) throws SQLException{
        String dropQuery = "DROP TABLE IF EXISTS "+tableName;
        database.execSQL(dropQuery);
    }

    private void validateTable(String tableName , Map<String, String> columns) throws ValidationException{
        try {
            Set<String> columnsName = columns.keySet();
            StringBuilder str = new StringBuilder();
            String delim = "";
            for (String c : columnsName) {
                if (c.equals("pk")) {
                    continue;
                }
                str.append(delim);
                str.append(c);

                delim = ", ";
            }
            String query = "select " +  str.toString() + " from " + tableName + " where 1 = 2";
            database.execSQL(query);
        } catch (SQLException e) {
           throw new ValidationException("Invalid table "+tableName);
        }

    }

    public void save(String table, Map<String, Object> value) throws SQLException {
        Map<String, String> columns = Tables.getColumn(table);
        ContentValues values = new ContentValues();
        for (Map.Entry<String, String> column : columns.entrySet()) {
            Object objValue = value.get(column.getKey());
            if (objValue instanceof Integer) {
                values.put(column.getKey(), (Integer) objValue);
            } else if (objValue instanceof String) {
                values.put(column.getKey(), (String) objValue);
            }
            //se precisar de mais tipagem é só colocar mais ifs
        }

        beginTransaction();
        database.insert(table, null, values);
        endTransaction();
    }


    public void delete(String table, Map<String, String> where) throws SQLException {
        beginTransaction();

        String delete = "delete from ".concat(table);

        String whereStr = "";
        if (where != null && where.size() > 0) {
            whereStr = " where ";

            for (Map.Entry<String, String> entry: where.entrySet()) {
                //deixei padrao and, se precisar de or é so colocar mais um parametro
                whereStr = whereStr.concat(entry.getKey()).concat(" ").concat(entry.getValue()).concat(" and ");
            }

            whereStr = whereStr.substring(0, whereStr.length()-3);
        }

        delete.concat(whereStr);
        database.execSQL(delete);

        endTransaction();
    }

    public void delete(String table, String where, String[] args) throws SQLException {
        beginTransaction();
        database.delete(table, where, args);
        endTransaction();
    }

    public Cursor find(String query) {
        return database.rawQuery(query, null);
    }

    public List<Map> convertToList(Cursor cursor) {
        List<Map> list = new ArrayList<Map>();
        if (cursor.moveToFirst()) {
            do {
                Map<String, Object> row = new HashMap<String, Object>();
                int countColumns = cursor.getColumnCount();
                for (int i = 0; i < countColumns; i++) {
                    //deixei string pq nesse caso todas as colunas são de texto
                    row.put(cursor.getColumnName(i), cursor.getString(i));
                }

                list.add(row);

            } while(cursor.moveToNext());
        }
        return list;
    }

    public String generateWhereLike(String column, String param) {
        String where = " where ";
        where = where.concat(column.concat(" like ").concat("'%").concat(param).concat("%'"));

        return where;

    }

}
