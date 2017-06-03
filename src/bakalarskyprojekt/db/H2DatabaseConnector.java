/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bakalarskyprojekt.db;


import bakalarskyprojekt.utils.Info;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * not sure if you actually need to create instance of it its kind of useless Might be needed connection pool
 *
 * @author tomas
 */
public class H2DatabaseConnector {

    private Connection connection;
    public Statement statement;
    public ResultSet resultSet;
    public ResultSetMetaData resultSetMd;
    public PreparedStatement prepStat;

    //not sure if here or on objects themself that need them
    public static final String[] PREP_STATEMENTS = new String[]{};

    public static final String DB_FILE_NAME = "bpdb";
    public static final String DB_DIR = "." + File.separator + "database";

    private String username = Info.DB_DEFAULT_USERNAME;
    private String password = Info.DB_DEFAULT_PASSWORD;

    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public H2DatabaseConnector() {
        try {
            openConnection();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    //TODO not sure if needed
//make one autoexec contructor
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public H2DatabaseConnector(boolean autoExec) {
        try {
            openConnection();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        if (autoExec) {
            try {
                execute();//ignore warning
            } catch (SQLException ex) {
                Logger.getLogger(H2DatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
            }
            closeConnection();
        }

    }

    public final void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
            }
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public final void openConnection() throws SQLException {
        connection = DriverManager.getConnection("jdbc:h2:file:" + DB_DIR + File.separator + DB_FILE_NAME + ";DATABASE_TO_UPPER=false;MULTI_THREADED=TRUE",
                username, password);
        statement = connection.createStatement();
    }

    //to be overriden
    public void execute() throws SQLException{
    }

    //to be overriden
    public void execute(String query) throws SQLException{
    }

    public <T> void execute(T param) throws SQLException{

    }

    public <T extends Iterable> T executeRetrieveIterable() throws SQLException{
        return null;
    }

    public <T> T executeRetrieve() throws SQLException{
        return null;
    }

    public <T> T executeRetrieve(T param) throws SQLException {
        return null;
    }

    public Connection getConnection() {
        return connection;
    }

    public Statement getStatement() {
        return statement;
    }

}
