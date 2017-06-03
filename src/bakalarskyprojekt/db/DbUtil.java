/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bakalarskyprojekt.db;

import bakalarskyprojekt.model.BPTaskProps;
import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Static utility methods here for database communication and data retrieval
 *
 * @author Kofola
 */
public class DbUtil {

    private static final int DB_INDEX_TESTS_ID = 1;
    private static final int DB_INDEX_TESTS_NAME = 2;
    private static final int DB_INDEX_TESTS_PRICE_DATA = 3;
    private static final int DB_INDEX_TESTS_TRADE_DATA = 4;
    private static final int DB_INDEX_TESTS_TEST_FILE = 5;
    private static final int DB_INDEX_TESTS_APPLICATION_TYPE = 6;
    private static final int DB_INDEX_TESTS_PARAMETERS = 7;

    public static void createTables() throws SQLException {
        new H2DatabaseConnector() {

            @Override
            public void execute() throws SQLException {//NO NEED TO THROW PROLLY.

                getStatement().execute("CREATE TABLE IF NOT EXISTS tests("
                        + "test_id BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL,"
                        + "name VARCHAR(255) NOT NULL UNIQUE,"
                        + "priceDataFiles VARCHAR(2000) NOT NULL,"//STRING OF PATHS
                        + "tradeDataFiles VARCHAR(2000) NOT NULL,"//STRING OF PATHS
                        + "testFile VARCHAR(255) NOT NULL,"
                        + "applicationType INT NOT NULL,"
                        + "parameters VARCHAR(1000)"
                        + ");");

                closeConnection();
            }

        }.execute();
    }

    public static long storeTest(BPTaskProps test) throws SQLException {
        return new H2DatabaseConnector() {

            @Override
            public <T> T executeRetrieve() throws SQLException {

                System.out.println("DEBUG DB:Going to store test:" + test + "\n");
                prepStat = getConnection().prepareStatement("INSERT INTO tests("
                        + "name,"
                        + "priceDataFiles,"
                        + "tradeDataFiles,"
                        + "testFile,"
                        + "applicationType,"
                        + "parameters"
                        + ") VALUES(?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);

                prepStat.setString(1, test.getName());
                prepStat.setString(2, test.getPriceDataFilesString());//something wrong here
                prepStat.setString(3, test.getTradeDataFilesString());//something wrong here
                prepStat.setString(4, test.getTestFile().getPath());
                prepStat.setInt(5, test.getApplicationType());
                prepStat.setString(6, test.getParameters());

                prepStat.executeUpdate();
                resultSet = prepStat.getGeneratedKeys();

                //return id
                if (this.resultSet.next()) {
                    return (T) Long.valueOf(resultSet.getLong(1));
                }
                prepStat.close();
                return (T) new Long(-1);
            }

        }.executeRetrieve();

    }

    public static void updateTest(BPTaskProps test) throws SQLException {//update test thats already in db , has id then :)
        System.out.println("teeeeest");
        System.out.println("Gonna update :" + test + "\n");

        new H2DatabaseConnector() {

            @Override
            public void execute() throws SQLException {

                prepStat = getConnection().prepareStatement("UPDATE tests SET "
                        + "name=?,"
                        + "priceDataFiles=?,"
                        + "tradeDataFiles=?,"
                        + "testFile=?,"
                        + "applicationType=?,"
                        + "parameters=?"
                        + " WHERE test_id=?;");

                synchronized (test) {

                    prepStat.setString(1, test.getName());
                    prepStat.setString(2, test.getPriceDataFilesString());
                    prepStat.setString(3, test.getTradeDataFilesString());
                    prepStat.setString(4, test.getTestFile().getPath());
                    prepStat.setInt(5, test.getApplicationType());
                    prepStat.setString(6, test.getParameters());
                    prepStat.setLong(7, test.getId());
                }

                prepStat.execute();
                prepStat.close();
            }

        }.execute();

    }

    public static final void removeTest(BPTaskProps test) throws SQLException {

        new H2DatabaseConnector() {

            @Override
            public void execute() throws SQLException {
                PreparedStatement stmt = getConnection().prepareStatement("DELETE FROM tests WHERE test_id=?");
                stmt.setLong(1, test.getId());
                stmt.executeUpdate();
                stmt.close();

            }

        }.execute();

    }



    public static List<BPTaskProps> getTests() throws SQLException {
        return new H2DatabaseConnector() {

            List<BPTaskProps> ret = new ArrayList<>();

            @Override
            public <T extends Iterable> T executeRetrieveIterable() throws SQLException {
                PreparedStatement stmt = getConnection().prepareStatement("SELECT * FROM tests;");
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        BPTaskProps test;

                        long id = rs.getLong(1);

                        List<File> listPriceData = new ArrayList<>();
                        for (String path : rs.getString(DB_INDEX_TESTS_PRICE_DATA).split("\\*")) {
                            listPriceData.add(new File(path));
                        }
                        List<File> listTradeData = new ArrayList<>();
                        for (String path : rs.getString(DB_INDEX_TESTS_TRADE_DATA).split("\\*")) {
                            listTradeData.add(new File(path));
                        }

                        test = new BPTaskProps(
                                rs.getLong(DB_INDEX_TESTS_ID),
                                rs.getString(DB_INDEX_TESTS_NAME),
                                listPriceData,
                                listTradeData,
                                new File(rs.getString(DB_INDEX_TESTS_TEST_FILE)),
                                rs.getInt(DB_INDEX_TESTS_APPLICATION_TYPE),
                                rs.getString(DB_INDEX_TESTS_PARAMETERS)
                        );

//                            test = Weapon.createWeapon(rs.getString(DB_INDEX_WEAPON_WEAPON_NAME),
//                                    Util.jsonStringToObject(rs.getString(DB_INDEX_WEAPON_PATTERN), RecoilPattern.class), rs.getLong(DB_INDEX_WEAPON_ID));
                        ret.add(test);

                        //System.out.println("DEBUG getDailyStats DailyStat: " + ds);
                    }
                }
                closeConnection();
                return (T) ret;
            }

        }.executeRetrieveIterable();
    }

}
