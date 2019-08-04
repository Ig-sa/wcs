package test.java.wcs;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.SQLException;
import main.java.wcs.*;

public class TestMain {

    @Test
    public void testEstablishConnection() {
        MySQLConnection.EstablishConnection();
        assertNotNull(MySQLConnection.MySQLConn);
        try {
                assertTrue(MySQLConnection.MySQLConn.isValid(10));
        } catch (SQLException e) {
                e.printStackTrace();
        }
    }

    @Test
    public void testLoadCreation() {
        Program myProgram = new Program();
        assertNotNull(myProgram.new Load(0));
    }


}