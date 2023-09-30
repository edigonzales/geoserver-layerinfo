package ch.so.agi.geoserver;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;

public class LayerInfoTest {
    
    @BeforeAll
    public static void setUp() throws Exception {
        try {
            // Create initial context
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
            System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");
            InitialContext ic = new InitialContext();

            ic.createSubcontext("java:");
            ic.createSubcontext("java:comp");
            ic.createSubcontext("java:comp/env");
            ic.createSubcontext("java:comp/env/jdbc");

            PGSimpleDataSource ds = new PGSimpleDataSource();
            ds.setServerName("localhost");
            ds.setPortNumber(54322);
            //ds.setUrl(null);
            ds.setDatabaseName("pub");   
            ds.setUser("ddluser");       
            ds.setPassword("ddluser");   

            ic.bind("java:comp/env/jdbc/pub", ds);
        } catch (NamingException ex) {
            ex.printStackTrace();
            System.err.println(ex.getMessage());
        }
    }
    
    @Test
    public void dummy() {        
        Map<String,Object> requestMap = new HashMap<>();
        requestMap.put("WIDTH", 101);
        requestMap.put("HEIGHT", 101);
        requestMap.put("X", "50");
        requestMap.put("Y", "50");
        requestMap.put("BBOX", "SRSEnvelope[2597362.88220624 : 2597370.412965569, 1225907.3337739531 : 1225914.8645332821]");
        
        List<Map<String,Object>> responseList = LayerInfo.executeSql("java:comp/env/jdbc/pub", requestMap, "src/test/data/", "ch.so.agi.av.fixpunkte.sql");
        System.out.println(responseList);
        
        assertNotNull(responseList);
    }
    
}
