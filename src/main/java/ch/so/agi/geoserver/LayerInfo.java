package ch.so.agi.geoserver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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
    private static final String TOLERANCE_IN_PIXEL_PARAM = "TOLERANCE_IN_PIXEL";
    private static final int TOLERANCE_IN_PIXEL = 4;

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
        System.out.println(requestParam.get("WIDTH").getClass());
        System.out.println(requestParam.get("HEIGHT").getClass());
        System.out.println(requestParam.get("X").getClass());
        System.out.println(requestParam.get("Y").getClass());
        
        System.out.println("dataDir: " + dataDir);
        System.out.println("sqlFile: " + sqlFile);

        //LOGGER.log(Level.FINE, "scale: " + String.valueOf(resolution));
        
        // X-/Y-Koordinaten des geklickten Punktes berechnen
        // Und Auflösung, um korrekt buffern zu können.
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
        double diffX = x2 - x1;
        double diffY = y2 - y1;
        
        double quotX = x / width;
        double quotY = y / height;
        
        double xcoord = x1 + diffX * quotX;
        double ycoord = y1 + diffY * quotY;
        
        double resolution = diffX / width;
        
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
        sql = sql
                .replace(":x", String.valueOf(xcoord))
                .replace(":y", String.valueOf(ycoord))
                .replace(":resolution", String.valueOf(resolution))
                .replace(":"+TOLERANCE_IN_PIXEL_PARAM, String.valueOf(TOLERANCE_IN_PIXEL));

        // SQL-Query ausführen
        try {       
            Context context = new InitialContext();
            DataSource dataSource = (DataSource) context.lookup(jndiName);
            
            try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(sql);
                ResultSetMetaData meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();
                while(rs.next()) {
                    Map<String,Object> map = new HashMap<>();

                    for (int column = 1; column <= columnCount; ++column) {
                        Object value = rs.getObject(column);
                        map.put(meta.getColumnName(column), value);
                    }
                    list.add(map);
                    
                }
             } catch (SQLException e) {
                e.printStackTrace();
                LOGGER.log(Level.SEVERE, e.getMessage());
                return null;
             }
        } catch (NamingException e) {
            LOGGER.log(Level.SEVERE, "Got naming exception, details are -> " + e.getMessage());
            return null;
        } 

        return list;
    }
}
