package ch.so.agi.geoserver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class LayerInfo {
    private static Logger LOGGER = Logger.getLogger(LayerInfo.class.getName());

    private static final String SQL_FILE_DIR = "layerinfo_sql";

    static {
        try {
            InputStream stream = LayerInfo.class.getClassLoader().getResourceAsStream("logging.properties");
            LogManager.getLogManager().readConfiguration(stream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Wenn ich nicht GeoServer und GeoTools als Abhängigkeit haben will, was zwar zur Runtime vorhanden ist 
    // aber zum Entwickeln und Testen mühsam ist, ist die BBOX (Klasse SRSEnvelope) einfach ein String resp. ich muss
    // toString verwenden. In der Map sind es Objects, d.h. toString ist immer gleich. Wenn aber toString ändert, 
    // funktioniert es nicht mehr.
    public static List<Map<String,Object>> executeSql(String jndiName, Map<String,Object> requestParam, String dataDir, String sqlFile) {
        
        
        List<Map<String,Object>> list = new ArrayList<>();
        
        System.out.println("requestParam: " + requestParam);
        System.out.println(requestParam.get("BBOX"));
        System.out.println(requestParam.get("BBOX").getClass());
        
        
        System.out.println("dataDir: " + dataDir);
        System.out.println("sqlFile: " + sqlFile);

        // X-/Y-Koordinaten des geklickten Punktes berechnen
        double width = Double.valueOf((Integer)requestParam.get("WIDTH"));
        double height = Double.valueOf((Integer)requestParam.get("HEIGHT"));
        int x = Integer.valueOf((String)requestParam.get("X"));
        int y = Integer.valueOf((String)requestParam.get("Y"));
        
        String bbox = (String) requestParam.get("BBOX").toString();
        bbox = bbox.replace("SRSEnvelope[", "").replace("]", "");
        String xx = bbox.split(",")[0];
        String yy = bbox.split(",")[1];
        double x1 = Double.valueOf(xx.split(":")[0].trim());
        double x2 = Double.valueOf(xx.split(":")[1].trim());
        double y1 = Double.valueOf(yy.split(":")[0].trim());
        double y2 = Double.valueOf(yy.split(":")[1].trim());
        
        // TODO 
        // alle klassentypen ausgeben. Einiges ist string, anderes int?
        
        
        LOGGER.log(Level.FINE, "An FINE level log!");
        LOGGER.log(Level.INFO, "An INFO level log!");

        // SQL-Datei lesen
        String sql = null;
        Path sqlFilePath = Path.of(dataDir, SQL_FILE_DIR, sqlFile);
        try {
            sql = Files.readString(sqlFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        
        // Parameter in SQL-Befehl replacen
        System.out.println(sql);
        
                
        
        try {       
            Context context = new InitialContext();
            DataSource dataSource = (DataSource) context.lookup(jndiName);
            
//            Connection con = dataSource.getConnection();
//            Statement stmt = con.createStatement();
//            
//            ResultSet rs = stmt.executeQuery("select empid, name from Employee");

            // TODO muss generisch zu key/value mappen
            try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT 1");
                while(rs.next()) {
                    int i = rs.getInt(1);
                    System.out.println("**********" + i);
                }
             } catch (SQLException e) {
                e.printStackTrace();
                return null;
             }
        } catch (NamingException e) {
            System.out.println("Got naming exception, details are -> " + e.getMessage());
            return null;
        } 
        
        Map<String,Object> map1 = new HashMap<>();
        map1.put("t_id", "feature.UNO");
        map1.put("attr1", "value1_1");
        map1.put("attr2", "value1_2");
        list.add(map1);
        
        Map<String,Object> map2 = new HashMap<>();
        map2.put("t_id", "feature.DUE");
        map2.put("attr1", "value2_1");
        map2.put("attr2", "value2_2");
        list.add(map2);

        return list;
    }
}
