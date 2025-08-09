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
                validateQueryExecution(newId); // Validate it was actually created, if not, return 500

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
                validateQueryExecution(newId); // Validate it was actually created, if not, return 500

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
                validateQueryExecution(newId); // Validate it was actually created, if not, return 500

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
                validateQueryExecution(newId); // Validate it was actually created, if not, return 500

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

        // Adam: PUT routes
        put ("/items/:itemID", (req, res) -> {
            try {
                var itemID = validateInt(req.params(":itemID")); // Validate int, or return 400

                // Parse the body of the request - we need the name param
                JSONObject body = (JSONObject) new JSONParser().parse(req.body());
                String name = body.get("name").toString();

                // Create the item (it returns ID)
                int rowsUpdated = DatabaseManager.putItem(itemID, name); // Insert & return new ID
                validateQueryExecution(rowsUpdated); // If an error happened in SQL, this'll be -1
                validateRowsUpdated(rowsUpdated); // Validate item was actually updated, if not, return 404 (DNE)

                // Return updated object
                res.status(200); // 200 for updated, we're returning the new obj
                return "{\"id\":" + itemID + ",\"name\":\"" + name + "\"}";
            } catch (ParseException e) {
                // If the user didn't input a string for name, or the body didn't have the name attr, return error
                halt(400, "Invalid JSON format");
                return null;
            }
        });
        put ("/distributors/:distributorID/item/:itemID", (req, res) -> {
            try {
                var distributorID = validateInt(req.params(":distributorID")); // Validate int, or return 400
                var itemID = validateInt(req.params(":itemID")); // Validate int, or return 400

                // Parse the body of the request - we need the cost param (this is only for editing the cost)
                JSONObject body = (JSONObject) new JSONParser().parse(req.body());
                BigDecimal cost = BigDecimal
                        .valueOf(((Number) body.get("cost")).doubleValue()) // Convert number to Double so we can use BigDecimal to round
                        .setScale(2, RoundingMode.HALF_UP); // round to 2 decimal places

                // Create the item (it returns ID)
                int rowsUpdated = DatabaseManager.putDistributorPrice(distributorID, itemID, cost); // Insert & return new ID
                validateQueryExecution(rowsUpdated); // If an error happened in SQL, this'll be -1
                validateRowsUpdated(rowsUpdated); // Validate item was actually updated, if not, return 404 (DNE)

                // Return updated object
                res.status(204); // 204 b/c we're returning nothing (I don't wanna go have to do SQL to grab the distributor_price item name and distributor name) but we want to signal that the item was successfully updated
                return "";
            } catch (ParseException e) {
                // If the user didn't input a string for name, or the body didn't have the name attr, return error
                halt(400, "Invalid JSON format");
                return null;
            }
        });

        // Adam: Unique POST request - we're not creating an obj here, but need POST for the request body
        post("/restock", (req, res) -> {
            try {
                // Parse the body of the request - we need the item (ID) and the quantity needed
                JSONObject body = (JSONObject) new JSONParser().parse(req.body());
                Integer item = ((Number) body.get("item")).intValue();
                Integer quantity = ((Number) body.get("quantity")).intValue();

                // We need to grab the cheapest cost from all distributor(_prices)
                JSONArray rows = DatabaseManager.getCheapestCost(item);
                // Double check the item did in fact exist
                if (rows.isEmpty()) {
                    halt(404, "Item does not exist, no pricing available.");
                }

                // Grab the object (it's the first one, index of 0, basically an array with 1 object)
                JSONObject object = (JSONObject) rows.get(0);

                // Extract the cost from object
                BigDecimal minCost = BigDecimal
                        .valueOf(((Number) object.get("cost")).doubleValue())
                        .setScale(2, RoundingMode.HALF_UP);

                // Multiply minCost by quantity
                BigDecimal totalCost = minCost.multiply(BigDecimal.valueOf(quantity)); // Need to "BigDecimal" quantity to set both to the same type of "number" for multiplication

                // Prep the JSON response
                var data = "{\"minCost\":" + minCost +
                        ",\"distributor\":" + object.get("distributor") +
                        ",\"item\":" + item +
                        ",\"quantity\":" + quantity +
                        ",\"totalCost\":" + totalCost +
                        "}";
                res.status(200); // 200 remember we're not creating anything

                // Return the data
                return data;
            } catch (ParseException e) {
                // If the user didn't input a string for name, or the body didn't have the name attr, return error
                halt(400, "Invalid JSON format");
                return null;
            }
        });

        // Adam: DELETE endpoints
        delete("/items/:itemID", (req, res) -> {
            int itemID = validateInt(req.params(":itemID")); // Validate int, or return 400
            int rowsDeleted = DatabaseManager.deleteItem(itemID); // Delete the item (and cascade to inventory)
            validateQueryExecution(rowsDeleted); // ensure rowsDeleted doesn't equal -1
            validateRowsUpdated(rowsDeleted);
            res.status(204); // If we got to this point, it was successful
            return "";
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

    // Adam: OSOT in validating obj actually created (POST)
    private static void validateQueryExecution(Integer id) {
        // If the ID returned is -1, then an error occured.
        if (id == -1) {
            halt(500, "Error creating object, request failed.");
        }
    }

    // Adam: OSOT in validating obj was actually updated (PUT)
    private static void validateRowsUpdated(Integer numberOfRowsUpdated) {
        if (numberOfRowsUpdated == 0) {
            halt(404, "Could not find object.");
        }
    }
}