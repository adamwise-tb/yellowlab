# Adam's Developer Notes
## Java
My strategy building this app was to maintain OSOT through private functions (error handling/codes, validating params, SQL query execution, preventing SQL injection) and leverage the...

- Main.java: A controller of sorts, hosting all ROUTES, categorized by method
- DatabaseManager: A model manager of sorts, hosting all SQL QUERIES to modify the underlying data

Given time constraints, the next place I'd take an app like this would be...

- Creating models for items, inventory, distributors, distributor_prices to host data manipulations (instead of all in DatabaseManager)
- Pull out route validations into a separate file for a cleaner codebase

## React
My strategy here was to leverage Bootstrap and React Router to create a semi-intuitive frontend for interacting with the API.

- Home: Click buttons to load both Items & Distributors
- Items: Can click into an item to...
  - View all distributors who sell it
  - Edit item name
  - Delete item
- Distributors: Can click into a distributor to...
  - View all items the distributor sells
  - Create a new distributor (green button appears on Home page when listing distributors)

Given time constraints, next place I'd take this part of the app would be...

- Improve API endpoints for specific items/distributors to include the item itself (ex: when pulling a distributor, include the name, and THEN an array of all their item prices) to allow for better data display in react app (didn't wanna pass names through state, especially with users editing values)
- Create buttons / pages for ALL endpoints (ex: a button that upon clicking, downloads the CSV for a given table)
- Improve CSS design (I didn't do anything crazy here - but wanted to show I have an understanding of Bootstrap to make nice interfaces)
- More OSOT with API calls, components, etc.

# API Notes
## GET routes
- GET /items: Displays all items (id, name)
- GET /inventory: Displays inventory of all items (item_id, name, stock, capacity)
- GET /inventory/overstocked: Displays inventory of all items where stock > capacity (item_id, name, stock, capacity)
- GET /inventory/low: Displays inventory of all items where stock is less than 35% of capacity (item_id, name, stock, capacity)
- GET /inventory/<itemID>: Displays inventory of specific item (item_id, name, stock, capacity)
- GET /distributors: Displays all distributors (id, name)
- GET /distributors/<distributorID>: Displays distributor_prices for all items that a specific distributor sells (item_id, item_name, cost)
- GET /distributors/item/<itemID>: Displays all distributors who sell a specific item (distributor_id, name, cost)

## POST routes
- POST /items: Create item (Body: name)
- POST /inventory: Create inventory for a given item (Body: item, stock, capacity)
- POST /distributors: Create a distributor (Body: name)
- POST /distributor_prices: Add an item to a given distributor's catalog (Body: distributor, item, cost)
- POST /restock: Given an item and desired quantity (Body: item, quantity), return the minCost of the item among all distributors and calculate the totalCost (quantity x minCost)

## PUT routes
- PUT /items/<itemID>: Update a specific item (Body: name)
- PUT /distributors/<distributorID>/item/<itemID>: Update the cost of a specific item for a specific distributor (Body: cost)

## DELETE routes
- DELETE /items/<itemID>: Delete a specific item (and cascade to their inventory)
- DELETE /distributors/<distributorID>: Delete a specific distributor (and cascade to their distributor_prices)