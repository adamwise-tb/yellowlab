package com.topbloc.codechallenge;

import com.topbloc.codechallenge.db.DatabaseManager;

import static spark.Spark.*;
import spark.Response;
import org.json.simple.JSONArray;

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

        //TODO: Add your routes here. a couple of examples are below
        get("/items", (req, res) -> DatabaseManager.getItems());
        get("/version", (req, res) -> "TopBloc Code Challenge v1.0");

        // Adam: Inventory routes
        get("/inventory", (req, res) -> DatabaseManager.getAllInventory());
        get("/inventory/", (req, res) -> DatabaseManager.getAllInventory()); // For vanity sake
        get("/inventory/overstocked", (req, res) -> DatabaseManager.getOverstockedInventory());
        get("/inventory/low", (req, res) -> DatabaseManager.getLowStockInventory());
        get("/inventory/:itemID", (req, res) -> {
            var itemID = validateInt(req.params(":itemID")); // Validate int, or return 400
            var data = DatabaseManager.getInventory(itemID); // Grab the item...
            return jsonOr404(data, res); // ...and return it or if DNE, 404
        });

        // Adam: Distributor routes
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
}