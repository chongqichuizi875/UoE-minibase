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
    private volatile static DatabaseCatalog singleton;
    private final HashMap<String, String> location_map;
    private final HashMap<String, List<Boolean>> schema_map;
    private DatabaseCatalog() {
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


