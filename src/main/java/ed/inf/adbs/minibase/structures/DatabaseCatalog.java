package ed.inf.adbs.minibase.structures;


import java.io.File;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DatabaseCatalog {
    private volatile static DatabaseCatalog singleton;
    private final HashMap<String, String> location_map;
    private final HashMap<String, List<Boolean>> type_map;
    private DatabaseCatalog(){
        location_map = new HashMap<>();
        location_map.put("R", "data"+File.separator+"evaluation"+File.separator+"db"+File.separator+"files"+File.separator+"R.csv");
        location_map.put("S", "data"+File.separator+"evaluation"+File.separator+"db"+File.separator+"files"+File.separator+"S.csv");
        location_map.put("T", "data"+File.separator+"evaluation"+File.separator+"db"+File.separator+"files"+File.separator+"T.csv");
        type_map = new HashMap<>();
        type_map.put("R", Arrays.asList(false,false,true));
        type_map.put("S", Arrays.asList(false,true,false));
        type_map.put("T", Arrays.asList(true,true));
    }
    public static DatabaseCatalog getCatalog(){
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

    public HashMap<String, List<Boolean>> getType_map() {
        return type_map;
    }
}


