package com.topbloc.codechallenge.db;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.math.BigDecimal;
import java.sql.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DatabaseManager {
    private static final String jdbcPrefix = "jdbc:sqlite:";
    private static final String dbName = "challenge.db";
    private static String connectionString;
    private static Connection conn;

    static {
        File dbFile = new File(dbName);
        connectionString = jdbcPrefix + dbFile.getAbsolutePath();
    }

    public static void connect() {
        try {
            Connection connection = DriverManager.getConnection(connectionString);
            System.out.println("Connection to SQLite has been established.");
            conn = connection;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    // Schema function to reset the database if needed - do not change
    public static void resetDatabase() {
        try {
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        File dbFile = new File(dbName);
        if (dbFile.exists()) {
            dbFile.delete();
        }
        connectionString = jdbcPrefix + dbFile.getAbsolutePath();
        connect();
        applySchema();
        seedDatabase();
    }

    // Schema function to reset the database if needed - do not change
    private static void applySchema() {
        String itemsSql = "CREATE TABLE IF NOT EXISTS items (\n"
                + "id integer PRIMARY KEY,\n"
                + "name text NOT NULL UNIQUE\n"
                + ");";
        String inventorySql = "CREATE TABLE IF NOT EXISTS inventory (\n"
                + "id integer PRIMARY KEY,\n"
                + "item integer NOT NULL UNIQUE references items(id) ON DELETE CASCADE,\n"
                + "stock integer NOT NULL,\n"
                + "capacity integer NOT NULL\n"
                + ");";
        String distributorSql = "CREATE TABLE IF NOT EXISTS distributors (\n"
                + "id integer PRIMARY KEY,\n"
                + "name text NOT NULL UNIQUE\n"
                + ");";
        String distributorPricesSql = "CREATE TABLE IF NOT EXISTS distributor_prices (\n"
                + "id integer PRIMARY KEY,\n"
                + "distributor integer NOT NULL references distributors(id) ON DELETE CASCADE,\n"
                + "item integer NOT NULL references items(id) ON DELETE CASCADE,\n"
                + "cost float NOT NULL\n" +
                ");";

        try {
            System.out.println("Applying schema");
            conn.createStatement().execute(itemsSql);
            conn.createStatement().execute(inventorySql);
            conn.createStatement().execute(distributorSql);
            conn.createStatement().execute(distributorPricesSql);
            System.out.println("Schema applied");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Schema function to reset the database if needed - do not change
    private static void seedDatabase() {
        String itemsSql = "INSERT INTO items (id, name) VALUES (1, 'Licorice'), (2, 'Good & Plenty'),\n"
            + "(3, 'Smarties'), (4, 'Tootsie Rolls'), (5, 'Necco Wafers'), (6, 'Wax Cola Bottles'), (7, 'Circus Peanuts'), (8, 'Candy Corn'),\n"
            + "(9, 'Twix'), (10, 'Snickers'), (11, 'M&Ms'), (12, 'Skittles'), (13, 'Starburst'), (14, 'Butterfinger'), (15, 'Peach Rings'), (16, 'Gummy Bears'), (17, 'Sour Patch Kids')";
        String inventorySql = "INSERT INTO inventory (item, stock, capacity) VALUES\n"
                + "(1, 22, 25), (2, 4, 20), (3, 15, 25), (4, 30, 50), (5, 14, 15), (6, 8, 10), (7, 10, 10), (8, 30, 40), (9, 17, 70), (10, 43, 65),\n" +
                "(11, 32, 55), (12, 25, 45), (13, 8, 45), (14, 10, 60), (15, 20, 30), (16, 15, 35), (17, 14, 60)";
        String distributorSql = "INSERT INTO distributors (id, name) VALUES (1, 'Candy Corp'), (2, 'The Sweet Suite'), (3, 'Dentists Hate Us')";
        String distributorPricesSql = "INSERT INTO distributor_prices (distributor, item, cost) VALUES \n" +
                "(1, 1, 0.81), (1, 2, 0.46), (1, 3, 0.89), (1, 4, 0.45), (2, 2, 0.18), (2, 3, 0.54), (2, 4, 0.67), (2, 5, 0.25), (2, 6, 0.35), (2, 7, 0.23), (2, 8, 0.41), (2, 9, 0.54),\n" +
                "(2, 10, 0.25), (2, 11, 0.52), (2, 12, 0.07), (2, 13, 0.77), (2, 14, 0.93), (2, 15, 0.11), (2, 16, 0.42), (3, 10, 0.47), (3, 11, 0.84), (3, 12, 0.15), (3, 13, 0.07), (3, 14, 0.97),\n" +
                "(3, 15, 0.39), (3, 16, 0.91), (3, 17, 0.85)";

        try {
            System.out.println("Seeding database");
            conn.createStatement().execute(itemsSql);
            conn.createStatement().execute(inventorySql);
            conn.createStatement().execute(distributorSql);
            conn.createStatement().execute(distributorPricesSql);
            System.out.println("Database seeded");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // Helper methods to convert ResultSet to JSON - change if desired, but should not be required
    private static JSONArray convertResultSetToJson(ResultSet rs) throws SQLException{
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        List<String> colNames = IntStream.range(0, columns)
                .mapToObj(i -> {
                    try {
                        return md.getColumnName(i + 1);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .collect(Collectors.toList());

        JSONArray jsonArray = new JSONArray();
        while (rs.next()) {
            jsonArray.add(convertRowToJson(rs, colNames));
        }
        return jsonArray;
    }

    private static JSONObject convertRowToJson(ResultSet rs, List<String> colNames) throws SQLException {
        JSONObject obj = new JSONObject();
        for (String colName : colNames) {
            obj.put(colName, rs.getObject(colName));
        }
        return obj;
    }

    // Adam: Didn't wanna have to rip this try/catch for EVERY function interacting with DB.
    // Adam: Not to mention, validate params for potential SQL injection (enhancement from original)
    private static JSONArray queryResults(String query, Object... params) {
        // Prepare the statement in try (this automatically runs ps.close() at end
        try (var ps = conn.prepareStatement(query)) {
            // Basically go thru each ? in query and assign it value of param (in order)
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }

            // Then run the query, and return the results
            try (var rs = ps.executeQuery()) {
                return convertResultSetToJson(rs);
            }
        } catch (SQLException e) {
            // If an error ever happens in that process, log the error and return nothing
            System.out.println(e.getMessage());
            return null;
        }
    }

    // Adam: POST/create rows into given tables
    private static int createObject(String query, Object... params) {
        try (var ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) { // We need to return the ID of the obj created
            // Basically go thru each ? in query and assign it value of param (in order)
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }

            // Add record to DB (if it fails, exception will fire
            ps.executeUpdate();

            // Then return the ID of the last object created (Adam: I don't love this, but seems to be some limits with SQLite capabilities here)
            try (var rs = conn.createStatement().executeQuery("SELECT last_insert_rowid()")) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        } catch (SQLException e) {
            // If an error ever happens in that process, log the error...
            System.out.println(e.getMessage());
            return -1; // ...and return -1 as no ID of an actual object was created
        }
    }

    // Adam: PUT/update rows in given tables
    private static int updateObject(String query, Object... params) {
        try (var ps = conn.prepareStatement(query)) {
            // Basically go thru each ? in query and assign it value of param (in order)
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }

            // Execute the update - its return value is the number of rows affected (1 = successful, 0 = failed)
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return -1; // Return -1 if failure within SQL query
        }
    }

    // Adam: output a table in CSV
    private static String generateCSV(String query) {
        try (var ps = conn.prepareStatement(query)) {
            var rs = ps.executeQuery(); // execute the query, we'll ref the data to build the CSV

            // Initialize string to hold CSV
            var csvString = new StringBuilder();

            // Get the columns from the SQL query
            var md = rs.getMetaData();
            var cols = md.getColumnCount();

            // Create the header
            for (int i = 1; i <= cols; i++) {
                if (i > 1) csvString.append(','); // If it's not the first item, throw a comma before
                csvString.append(csvEscape(md.getColumnLabel(i))); // Grab the column label, establish as header
            }
            csvString.append('\n'); // line break for new row

            // Create the rows
            while (rs.next()) {
                for (int i = 1; i <= cols; i++) {
                    if (i > 1) csvString.append(','); // If it's not the first item, throw a comma before
                    csvString.append(csvEscape(rs.getString(i))); // Grab the column label, establish as header
                }
                csvString.append('\n'); // line break for new row
            }

            return csvString.toString();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null; // Return null for failure
        }
    }

    // Adam: Helper method for us to create csv values
    private static String csvEscape(String s) {
        // Return empty string on null values
        if (s == null) return "";

        boolean needsQuotes = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        // If the value contains a comma, quote wrap it
        if (needsQuotes) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        // Otherwise, just return the value
        return s;
    }

    /*
     ** Controller functions - add your routes here. getItems is provided as an example
     */

    // getItems: Return solely item fields
    public static JSONArray getItems() {
        String sql = "SELECT * FROM items";
        return queryResults(sql);
    }

    // getAllInventory: Return items + inventory fields (JOIN)
    public static JSONArray getAllInventory() {
        String sql = "SELECT i.id as item_id, i.name, inv.stock, inv.capacity " +
                "FROM items i JOIN inventory inv ON inv.item = i.id " +
                "ORDER BY i.id";
        return queryResults(sql);
    }

    // getOverstockedInventory: Return items + inventory where stock > capacity
    public static JSONArray getOverstockedInventory() {
        String sql = "SELECT i.id as item_id, i.name, inv.stock, inv.capacity " +
                "FROM items i JOIN inventory inv ON inv.item = i.id " +
                "WHERE inv.stock > inv.capacity " +
                "ORDER BY i.id";
        return queryResults(sql);
    }

    // getLowStockInventory: Return items + inventory where stock < capacity*0.35
    public static JSONArray getLowStockInventory() {
        String sql = "SELECT i.id as item_id, i.name, inv.stock, inv.capacity " +
                "FROM items i JOIN inventory inv ON inv.item = i.id " +
                "WHERE inv.stock < (inv.capacity * 0.35) " +
                "ORDER BY i.id";
        return queryResults(sql);
    }

    // getInventory: Return items + inventory for given Item ID
    public static JSONArray getInventory(Integer id) {
        String sql = "SELECT i.id as item_id, i.name, inv.stock, inv.capacity " +
                "FROM items i JOIN inventory inv ON inv.item = i.id " +
                "WHERE i.id = ?";
        return queryResults(sql, id);
    }

    // getAllDistributors: Return all distributors
    public static JSONArray getAllDistributors() {
        String sql = "SELECT * FROM distributors;";
        return queryResults(sql);
    }

    // getDistributor: Return distributor for given ID
    public static JSONArray getDistributor(Integer id) {
        String sql = "SELECT i.name as item_name, i.id as item_id, dp.cost " +
                "FROM distributors d " +
                "JOIN distributor_prices dp ON d.id = dp.distributor " +
                "JOIN items i ON i.id = dp.item " +
                "WHERE d.id = ?;";
        return queryResults(sql, id);
    }

    // getDistributorByItem: Return distributor for a given ITEM id
    public static JSONArray getDistributorsByItem(Integer id) {
        String sql = "SELECT d.name as distributor_name, d.id as distributor_id, dp.cost " +
                "FROM distributor_prices dp " +
                "JOIN items i ON dp.item = i.id " +
                "JOIN distributors d ON d.id = dp.distributor " +
                "WHERE dp.item = ?";
        return queryResults(sql, id);
    }

    // postItem: Create item (only name), primary key will auto be generated by SQLite
    public static int postItem(String name) {
        String sql = "INSERT INTO items (name) VALUES (?)";
        return createObject(sql, name);
    }

    // postInventory: Create inventory associated with item ID
    public static int postInventory(Integer item, Integer stock, Integer capacity) {
        String sql = "INSERT INTO inventory (item, stock, capacity) VALUES (?, ?, ?)";
        return createObject(sql, item, stock, capacity);
    }

    // postDistributor: Create distributor (only name)
    public static int postDistributor(String name) {
        String sql = "INSERT INTO distributors (name) VALUES (?)";
        return createObject(sql, name);
    }

    // postDistributorPrice: Create distributor_price associated with both distributor (ID) & item (ID)
    public static int postDistributorPrice(Integer distributor, Integer item, BigDecimal cost) {
        String sql = "INSERT INTO distributor_prices (distributor, item, cost) VALUES (?, ?, ?)";
        return createObject(sql, distributor, item, cost);
    }

    // putItem: Update item name
    public static int putItem(Integer id, String name) {
        String sql = "UPDATE items SET name = ? WHERE id = ?";
        return updateObject(sql, name, id);
    }

    // putDistributorPrice: Update distributor_prices cost value
    public static int putDistributorPrice(Integer distributorID, Integer itemID, BigDecimal cost) {
        String sql = "UPDATE distributor_prices SET cost = ? WHERE distributor = ? AND item = ?";
        return updateObject(sql, cost.doubleValue(), distributorID, itemID);
    }

    // getCheapestCost: Given an item ID, sort by distributor_prices ascending by cost, LIMIT 1 to return cheapest cost
    public static JSONArray getCheapestCost(Integer itemID) {
        String sql = "SELECT distributor, cost " +
                "FROM distributor_prices " +
                "WHERE item = ? " +
                "ORDER BY cost ASC " +
                "LIMIT 1;";
        return queryResults(sql, itemID);
    }

    // deleteItem: Given item ID, delete it, cascade to inventory items as well
    public static int deleteItem(Integer itemID) {
        String sql = "DELETE FROM items WHERE id = ?";
        return updateObject(sql, itemID);
    }

    // deleteDistributor: Given distirbutor ID, delete it, cascade to distributor_prices as well
    public static int deleteDistributor(Integer distributorID) {
        String sql = "DELETE FROM distributors WHERE id = ?";
        return updateObject(sql, distributorID);
    }

    // exportTableCSV: Given a table in the DB, output all the records
    public static String exportTableCSV(String tableName) {
        // We cannot parameterize table names, so let's do a switch
        String sql;
        switch (tableName) {
            case "items":
                sql = "SELECT * FROM items";
                break;
            case "inventory":
                sql = "SELECT * FROM inventory";
                break;
            case "distributors":
                sql = "SELECT * FROM distributors";
                break;
            case "distributor_prices":
                sql = "SELECT * FROM distributor_prices";
                break;
            default:
                sql = null;
        }

        // If valid table, return the csv, otherwise empty string
        if (sql == null) {
            return "";
        } else {
            return generateCSV(sql);
        }
    }
}
