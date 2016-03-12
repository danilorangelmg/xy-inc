package movies.com.br.xy_inc.db;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by danilo on 12/03/16.
 */
public class Tables {

    /**
     * Retorna o nome e as colunas das tabelas a serem criadas
     * @return
     */
    public static Map<String, Map<String, String>> getTables() {
        Map<String, Map<String, String>> tables = new HashMap<String, Map<String, String>>();
        tables.put("tb_movies", getTableMoviesColumns());
        return tables;
    }

    public static Map<String, String> getColumn(String table) {
        return getTables().get(table);
    }


    /**
     * Retorna no formato Nome coluna - Tipo
     * Primary key retorna com a chave PK
     * @return
     */
    public static Map<String, String> getTableMoviesColumns() {
        Map<String, String> columns = new LinkedHashMap<String, String>();
        columns.put("imdbid", "text");//utilizo o mesmo id
        columns.put("title", "text");
        columns.put("year", "text");
        columns.put("released", "text");
        columns.put("runtime", "text");
        columns.put("genre", "text");
        columns.put("director", "text");
        columns.put("writer", "text");
        columns.put("actors", "text");
        columns.put("plot", "text");
        columns.put("language", "text");
        columns.put("country", "text");
        columns.put("awards", "text");
        columns.put("poster", "text");
        columns.put("type", "text");
        columns.put("image_path", "text");
        columns.put("pk", "imdbid");

        return columns;

    }

    







}
