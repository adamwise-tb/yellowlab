package com.topbloc.codechallenge;

import com.topbloc.codechallenge.db.DatabaseManager;

import static spark.Spark.*;
import spark.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Main {
    public static void main(String[] args) {
        DatabaseManager.connect();
        // Don't change this - required for GET and POST requests with the header 'content-type'
        options("/*",
                (req, res) -> {
                    res.header("Access-Control-Allow-Headers", "content-type");
                    res.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
                    return "OK";
                });

        // Don't change - if required you can reset your database by hitting this endpoint at localhost:4567/reset
        get("/reset", (req, res) -> {
            DatabaseManager.resetDatabase();
            return "OK";
        });

        // Adam: Default all responses to JSON
        before((req, res) -> res.type("application/json"));

        //TODO: Add your routes here. a couple of examples are below
        get("/items", (req, res) -> DatabaseManager.getItems());
        get("/version", (req, res) -> "TopBloc Code Challenge v1.0");

        // Adam: GET Inventory routes
        get("/inventory", (req, res) -> DatabaseManager.getAllInventory());
        get("/inventory/", (req, res) -> DatabaseManager.getAllInventory()); // For vanity sake
        get("/inventory/overstocked", (req, res) -> DatabaseManager.getOverstockedInventory());
        get("/inventory/low", (req, res) -> DatabaseManager.getLowStockInventory());
        get("/inventory/:itemID", (req, res) -> {
            var itemID = validateInt(req.params(":itemID")); // Validate int, or return 400
            var data = DatabaseManager.getInventory(itemID); // Grab the item...
            return jsonOr404(data, res); // ...and return it or if DNE, 404
        });

        // Adam: GET Distributor routes
        get("/distributors", (req, res) -> DatabaseManager.getAllDistributors());
        get("/distributors/", (req, res) -> DatabaseManager.getAllDistributors()); // vanity
        get("/distributors/:distributorID", (req, res) -> {
            var distributorID = validateInt(req.params(":distributorID")); // Validate int, or return 400
            var data = DatabaseManager.getDistributor(distributorID); // Grab the item...
            return jsonOr404(data, res); // ...and return it or if DNE, 404
        });
        get("/distributors/item/:itemID", (req, res) -> {
            var itemID = validateInt(req.params(":itemID")); // Validate int, or return 400
            var data = DatabaseManager.getDistributorsByItem(itemID); // Grab the item...
            return jsonOr404(data, res); // ...and return it or if DNE, 404
        });

        // Adam: POST routes
        post("/items", (req, res) -> {
            try {
                // Parse the body of the request - we need the name param
                JSONObject body = (JSONObject) new JSONParser().parse(req.body());
                String name = body.get("name").toString();

                // Create the item (it returns ID)
                int newId = DatabaseManager.postItem(name); // Insert & return new ID
                validateCreate(newId); // Validate it was actually created, if not, return 500

                // Prep the JSON response
                var data = "{\"id\":" + newId + ",\"name\":\"" + name + "\"}";
                res.status(201); // 201 for created

                // Return the data
                return data;
            } catch (ParseException e) {
                // If the user didn't input a string for name, or the body didn't have the name attr, return error
                halt(400, "Invalid JSON format");
                return null;
            }
        });
        post("/inventory", (req, res) -> {
            try {
                // Parse the body of the request - we need the item (ID), stock, capacity
                JSONObject body = (JSONObject) new JSONParser().parse(req.body());
                Integer item = ((Number) body.get("item")).intValue();
                Integer stock = ((Number) body.get("stock")).intValue();
                Integer capacity = ((Number) body.get("capacity")).intValue();

                // Create the item (it returns ID)
                int newId = DatabaseManager.postInventory(item, stock, capacity); // Insert & return new ID
                validateCreate(newId); // Validate it was actually created, if not, return 500

                // Prep the JSON response
                var data = "{\"id\":" + newId +
                        ",\"item\":" + item +
                        ",\"stock\":" + stock +
                        ",\"capacity\":" + capacity +
                        "}";
                res.status(201); // 201 for created

                // Return the data
                return data;
            } catch (ParseException e) {
                // If the user didn't input a string for name, or the body didn't have the name attr, return error
                halt(400, "Invalid JSON format");
                return null;
            }
        });
        post("/distributors", (req, res) -> {
            try {
                // Parse the body of the request - we need the name param
                JSONObject body = (JSONObject) new JSONParser().parse(req.body());
                String name = body.get("name").toString();

                // Create the item (it returns ID)
                int newId = DatabaseManager.postDistributor(name); // Insert & return new ID
                validateCreate(newId); // Validate it was actually created, if not, return 500

                // Prep the JSON response
                var data = "{\"id\":" + newId + ",\"name\":\"" + name + "\"}";
                res.status(201); // 201 for created

                // Return the data
                return data;
            } catch (ParseException e) {
                // If the user didn't input a string for name, or the body didn't have the name attr, return error
                halt(400, "Invalid JSON format");
                return null;
            }
        });
        post("/distributor_prices", (req, res) -> {
            try {
                // Parse the body of the request - we need the distributor (ID), item (ID), cost
                JSONObject body = (JSONObject) new JSONParser().parse(req.body());
                Integer distributor = ((Number) body.get("distributor")).intValue();
                Integer item = ((Number) body.get("item")).intValue();
                // Float cost = ((Number) body.get("cost")).floatValue(); // Floats actually cause issues - floating-point precision
                BigDecimal cost = BigDecimal
                        .valueOf(((Number) body.get("cost")).doubleValue()) // Convert number to Double so we can use BigDecimal to round
                        .setScale(2, RoundingMode.HALF_UP); // round to 2 decimal places

                // Create the item (it returns ID)
                int newId = DatabaseManager.postDistributorPrice(distributor, item, cost); // Insert & return new ID
                validateCreate(newId); // Validate it was actually created, if not, return 500

                // Prep the JSON response
                var data = "{\"id\":" + newId +
                        ",\"distributor\":" + distributor +
                        ",\"item\":" + item +
                        ",\"cost\":" + cost +
                        "}";
                res.status(201); // 201 for created

                // Return the data
                return data;
            } catch (ParseException e) {
                // If the user didn't input a string for name, or the body didn't have the name attr, return error
                halt(400, "Invalid JSON format");
                return null;
            }
        });
    }

    // Adam: This helps maintain OSOT with ensuring param IDs are ACTUALLY IDs
    private static int validateInt(String param) {
        // Basically, here we wanna validate that the itemID param is ACTUALLY an integer (prevent SQL injection)
        if (!param.matches("\\d+")) {
            halt(400, "itemID must be an integer");
        }
        return Integer.parseInt(param);
    }

    // Adam: OSOT for returning array or 404 if DNE
    private static Object jsonOr404(JSONArray data, Response res) {
        if (data.isEmpty()) {
            res.status(404);
            return "[]";
        } else {
            return data;
        }
    }

    // Adam: OSOT in validating obj actually created/updated
    private static void validateCreate(Integer id) {
        // If the ID returned is -1, then an error occured.
        if (id == -1) {
            halt(500, "Error creating object, request failed.");
        }
    }
}