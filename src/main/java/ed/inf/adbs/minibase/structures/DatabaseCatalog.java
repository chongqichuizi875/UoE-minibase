package ed.inf.adbs.minibase.structures;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class DatabaseCatalog {
    /**
     * a singleton to store the schema information, database information and program path information
     *
     *    singleton:  this, a singleton
     * location_map:  if its key is R, then its value is the corresponding file path of R.csv
     *   schema_map:  store the schema of database, if key is R, value is a list of true/false,
     *                true at index i means the ith column of R.csv is of string type
     *
     * */
    private volatile static DatabaseCatalog singleton;
    private final HashMap<String, String> location_map;
    private final HashMap<String, List<Boolean>> schema_map;
    private DatabaseCatalog() {
        /**
         * constructor, initializing the database information, get schema information
         * */
        String db_path = "data"+File.separator+"evaluation"+File.separator+"db";
        location_map = new HashMap<>();
        schema_map = new HashMap<>();
        String next;
        try{
            BufferedReader br = new BufferedReader(new FileReader(db_path+File.separator+"schema.txt"));
            while ((next = br.readLine())!=null){
                List<String> ls = new ArrayList<>(Arrays.asList(next.split(" ")));
                String name = ls.remove(0);
                location_map.put(name, db_path+File.separator+"files"+File.separator+name+".csv");
                schema_map.put(name, ls.stream().map(s->s.equals("string")).collect(Collectors.toList()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static DatabaseCatalog getCatalog() {
        /**
         * Used for initializing the singleton
         * @return:
         *  the singleton instance
         * */
        if (singleton == null){ // reduce the unnecessary synchronization
            synchronized (DatabaseCatalog.class){ // synchronization for thread safety
                if (singleton == null) singleton = new DatabaseCatalog();
                }
            }
        return singleton;
    }
    public HashMap<String, String> getLocation_map(){
        return this.location_map;
    }

    public HashMap<String, List<Boolean>> getSchema_map() {
        return schema_map;
    }

}


