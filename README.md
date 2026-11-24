<img width="819" height="515" alt="image" src="https://github.com/user-attachments/assets/e622fdec-f0b5-48dd-afec-f0cd01518657" />



CREATE DATABASE IF NOT EXISTS schoolcanteendb;
USE schoolcanteendb;

CREATE TABLE categories (
    category_id   INT(11) NOT NULL AUTO_INCREMENT,
    category_name VARCHAR(100) NOT NULL,
    PRIMARY KEY (category_id),
    UNIQUE KEY category_name (category_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE suppliers (
    supplier_id    INT(11) NOT NULL AUTO_INCREMENT,
    supplier_name  VARCHAR(100) NOT NULL,
    Categories        VARCHAR(50) DEFAULT NULL,
    contact_number VARCHAR(20) DEFAULT NULL,
    PRIMARY KEY (supplier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE products (
    product_id    INT(11) NOT NULL AUTO_INCREMENT,
    product_name  VARCHAR(100) NOT NULL,
    category_id   INT(11) NOT NULL,
    supplier_id   INT(11) DEFAULT NULL,
    unit_price    DECIMAL(10,2) NOT NULL,
    quantity      INT(11) NOT NULL DEFAULT 0,
    stock_status  VARCHAR(10) GENERATED ALWAYS AS (
        CASE
            WHEN quantity >= 20 THEN 'FULL'
            WHEN quantity >= 10 THEN 'MEDIUM'
              ELSE 'low'
        END
    ) STORED,
    PRIMARY KEY (product_id),
    KEY fk_products_category (category_id),
    KEY fk_products_supplier (supplier_id),
    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id) REFERENCES categories (category_id),
    CONSTRAINT fk_products_supplier
        FOREIGN KEY (supplier_id) REFERENCES suppliers (supplier_id),
    CHECK (unit_price >= 0),
    CHECK (quantity >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE stock_in (
    stock_in_id    INT(11) NOT NULL AUTO_INCREMENT,
    product_id     INT(11) NOT NULL,
    quantity_added INT(11) NOT NULL,
    stock_in_date  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (stock_in_id),
    KEY product_id (product_id),
    CONSTRAINT stock_in_ibfk_1
        FOREIGN KEY (product_id) REFERENCES products (product_id),
    CHECK (quantity_added > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
 
-- Stock In: ADD the quantity received to the product
DELIMITER $$
CREATE TRIGGER after_stock_in_insert
AFTER INSERT ON stock_in
FOR EACH ROW
BEGIN
    UPDATE products
    SET quantity = quantity + NEW.quantity_added
    WHERE product_id = NEW.product_id;
END$$
DELIMITER ;


CREATE TABLE stock_out (
    stock_out_id     INT(11) NOT NULL AUTO_INCREMENT,
    product_id       INT(11) NOT NULL,
    quantity_removed INT(11) NOT NULL,
    stock_out_date   DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (stock_out_id),
    KEY product_id (product_id),
    CONSTRAINT stock_out_ibfk_1
        FOREIGN KEY (product_id) REFERENCES products (product_id),
    CHECK (quantity_removed > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
 
-- Stock Out (1): BLOCK the transaction if there is not enough stock
DELIMITER $$
CREATE TRIGGER before_stock_out_insert
BEFORE INSERT ON stock_out
FOR EACH ROW
BEGIN
    DECLARE current_qty INT;
    SELECT quantity INTO current_qty
    FROM products
    WHERE product_id = NEW.product_id;
 
    IF current_qty < NEW.quantity_removed THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Not enough stock';
    END IF;
END$$
 
-- Stock Out (2): SUBTRACT the quantity released from the product
CREATE TRIGGER after_stock_out_insert
AFTER INSERT ON stock_out
FOR EACH ROW
BEGIN
    UPDATE products
    SET quantity = quantity - NEW.quantity_removed
    WHERE product_id = NEW.product_id;
END$$
DELIMITER ;





Project Title: School Canteen Inventory Management System
Team/Members: Clarence Palermo, Cleo Pison,  Marve Piosca, Andrea Pales, Richelle Martagon, Renil Jabil, Vince Buenafe
Section: 2B NT
Assigned Roles: Leader, Programmer, Backend, Frontend  - Clarence Palermo, Cleo Pison
               PowerPoint Presentation: Marve Piosca, Andrea Pales, Richelle Martagon, Renil Jabil, Vince Buenafe
               Documentation: Cleo Pison & Clarence Palermo
Actual Contribution:Programmer, Backend, Frontend  - Clarence Palermo, Cleo Pison
                    Documentation: Cleo Pison & Clarence Palermo
                    PowerPoint Presentation: Marve Piosca, Andrea Pales, Richelle Martagon, Renil Jabil, Vince Buenafe


Project Overview: Problem - Current quantities can be difficult to confirm. Updates may be late or incomplete, items may run out unnoticed, and manual reports take time. 

Intended User: Canteen Administrator, Canteen Staff, School Management

Objectives: Design and implement a School Canteen Inventory Management System using a MySQL relational database to organize product records and monitor stock received and released.

Features: Create schoolcanteendb for categories, suppliers, products, stock_in, and stock_out.
          Use primary keys, foreign keys, and constraints to maintain accurate, consistent data.
          Normalize the database up to Third Normal Form (3NF).
          Automatically update quantities through stock-in and stock-out triggers.
          Display FULL, MEDIUM, and low stock statuses and identify items for restocking.
          Provide CRUD functions, SQL queries, and inventory reports.
          
Scope:
• Manage categories, suppliers, and products(Snacks, Beverages, School Supplies).
• Record product price, quantity, and stock status
• Record stock-in and stock-out with date/time
• Home dashboard and low-stock alert
• SQL queries and inventory reports

Limitations:
• No point-of-sale or cashiering module
• No online ordering or payment
• No barcode scanning or expiry alerts
• Accuracy depends on correct staff entries
• Sample data are for demonstration and testing

Technology and Setup: Java Netbeans - Apache Netbeans IDE 22
                      MySQL Workbench 8.0 CE
                      JDBC driver  
                      Dependencies - AbsoluteLayout-SNAPSHOT.jar, mysql-connector-j-8.4.0.jar, protobuf-java-3.2.5.1.jar
                      Java Dependencies - JDK 22 (Default)

Installation and Execution: 1. Download Source Code
                            2. Open Github
                            3. Put the Selection name or Title
                            4. Select Public on Readme
                            5. Open Readme.md
                            6. Find the pencil to edit
                            7. Insert Documents and Documentation
                Cloning or Downloading: You can clone or download it in zip file
                Importing the .sql file: 
                            If you already have the SQL file:
                            1. Open MySQL Workbench.
                            2. Open your SQL Editor.
                            3. Click File → Open SQL Script.
                            4. Select your .sql file.
                            5. Click the lightning bolt  (Execute).
                            6. Wait for the script to finish executing.

<img width="819" height="515" alt="image" src="https://github.com/user-attachments/assets/e622fdec-f0b5-48dd-afec-f0cd01518657" />
      1. Table Description, Important Fields, and Keys
A. Products Table
Main Table
PK + FK

Purpose: Stores the main information about every product available in the school canteen.
Field	Data Type	Description
product_id	INT	Unique ID of each product.
product_name	VARCHAR(100)	Name of the product.
category_id	INT	Identifies the product's category.
supplier_id	INT	Identifies the product's supplier.
unit_price	DECIMAL(10,2)	Selling price per product.
quantity	INT	Current available stock.
stock_status	VARCHAR(10)	Indicates the current stock level.

Important keys:
- Primary Key (PK): product_id
- Foreign Key (FK): category_id references categories.category_id.
- Foreign Key (FK): supplier_id references suppliers.supplier_id.

B. Categories Table
Primary Key
Purpose: Stores the different categories used to organize products.
Field	Data Type	Description
category_id	INT	Unique ID of each category.
category_name	VARCHAR(100)	Name of the category.

Important keys:
- Primary Key: category_id
- No foreign key is shown.
Important:
Instead of repeatedly typing the category name in every product record, we store the category once and reference it using category_id.
For example, Biscuits, Bread, and Curls can all use category_id = 1, which represents Snacks.

C. Suppliers Table
Primary Key
Purpose: Stores information about the suppliers who provide products to the school canteen.
Field	Data Type	Description
supplier_id	INT	Unique ID of each supplier.
supplier_name	VARCHAR(100)	Name of the supplier.
categories	VARCHAR(50)	Category information associated with the supplier.
contact_number	VARCHAR(20)	Supplier's contact number.

Important keys:
- Primary Key: supplier_id
- No foreign key is shown.

Important design explanation:
Your categories field in the Suppliers table is a VARCHAR field. It stores category information as text.
It is NOT a foreign key because it does not reference categories.category_id.

School Canteen Inventory Management System – Database Design Explanation
Based on your ERD diagram, your database has 5 tables:
1. products
2. categories
3. suppliers
4. stock_in
5. stock_out
These tables work together to manage product information, categories, suppliers, incoming stock, and outgoing stock.
1. Table Description, Important Fields, and Keys
A. Products Table
(Main Table) (PK + FK)

Purpose: Stores the main information about every product available in the school canteen.
Field	Data Type	Description
product_id	INT	Unique ID of each product.
product_name	VARCHAR(100)	Name of the product.
category_id	INT	Identifies the product's category.
supplier_id	INT	Identifies the product's supplier.
unit_price	DECIMAL(10,2)	Selling price per product.
quantity	INT	Current available stock.
stock_status	VARCHAR(10)	Indicates the current stock level.


Important keys:
- Primary Key (PK): product_id
- Foreign Key (FK): category_id references categories.category_id.
- Foreign Key (FK): supplier_id references suppliers.supplier_id.
Example:
product_id	product_name	category_id	supplier_id	quantity
1	Biscuits	1	1	20
2	Juice	2	2	15


B. Categories Table
(Primary Key)

Purpose: Stores the different categories used to organize products.
Field	Data Type	Description
category_id	INT	Unique ID of each category.
category_name	VARCHAR(100)	Name of the category.


Important keys:
- Primary Key: category_id
- No foreign key is shown.
Example:
category_id	category_name
1	Snacks
2	Beverages
3	School Supplies


Why do we need this table?

Instead of repeatedly typing the category name in every product record, we store the category once and reference it using category_id.

For example, Biscuits, Bread, and Curls can all use category_id = 1, which represents Snacks.
C. Suppliers Table
(Primary Key)

Purpose: Stores information about the suppliers who provide products to the school canteen.
Field	Data Type	Description
supplier_id	INT	Unique ID of each supplier.
supplier_name	VARCHAR(100)	Name of the supplier.
categories	VARCHAR(50)	Category information associated with the supplier.
contact_number	VARCHAR(20)	Supplier's contact number.


Important keys:
- Primary Key: supplier_id
- No foreign key is shown.
Example:
supplier_id	supplier_name	categories
1	Food Distributor	Snacks
2	Beverage Supplier	Beverages
3	School Supplier	School Supplies


Important design explanation:
Your categories field in the Suppliers table is a VARCHAR field. It stores category information as text.
It is NOT a foreign key because it does not reference categories.category_id.

D. Stock_In Table
(Primary Key + Foreign Key)
Purpose: Records the products and quantities received by the school canteen from suppliers.
Field	Data Type	Description
stock_in_id	INT	Unique ID for each stock-in transaction.
product_id	INT	Identifies the product received.
quantity_added	INT	Number of items added to the inventory.
stock_in_date	TIMESTAMP	Date and time the stock was received.

Important keys:
- Primary Key: stock_in_id
- Foreign Key: product_id references products.product_id.

E. Stock_Out Table
(Primary Key + Foreign Key)

Purpose: Records the products and quantities removed from the inventory, such as sold or released items.
Field	Data Type	Description
stock_out_id	INT	Unique ID for each stock-out transaction.
product_id	INT	Identifies the product removed.
quantity_removed	INT	Number of items removed from inventory.
stock_out_date	DATETIME	Date and time the stock was removed.

Important keys:
- Primary Key: stock_out_id
- Foreign Key: product_id references products.product_id.

Summary:
    Our database consists of five tables: Products, Categories, Suppliers, Stock_In, and Stock_Out. The Products table serves as the main table because it stores the information about the products available in the school canteen. Categories and Suppliers provide additional information about each product. Meanwhile, Stock_In records the quantity of products received, while Stock_Out records the quantity of products removed. We use primary keys to uniquely identify each record and foreign keys to establish relationships between tables. Some fields, such as product name, quantity, unit price, and stock status, do not require separate relationships because they directly describe the product or transaction."

<img width="1366" height="768" alt="Screenshot 2026-11-23 135916" src="https://github.com/user-attachments/assets/427a266e-82eb-4840-a827-4c7d17029fa1" />
Image of Create  // ADD a New Product


<img width="1366" height="768" alt="Screenshot 2026-11-23 135948" src="https://github.com/user-attachments/assets/17da72bc-e096-49af-a05a-44a64e66aa68" />
Image of Read // Read each Product

<img width="1366" height="768" alt="Screenshot 2026-11-23 140142" src="https://github.com/user-attachments/assets/25576ec3-2b9f-4816-b5d6-a0d7cfca6ac8" />
<img width="1366" height="768" alt="Screenshot 2026-11-23 140156" src="https://github.com/user-attachments/assets/e02d4338-d7ad-4391-9211-f33f46fc3894" />
Image of Delete // Delete a Product

<img width="1366" height="768" alt="Screenshot 2026-11-23 140022" src="https://github.com/user-attachments/assets/49c0730d-9a7b-4f8b-989f-ea2f29502c90" />
Image for Update // Update a product

<img width="1366" height="768" alt="Screenshot 2026-11-23 140125" src="https://github.com/user-attachments/assets/1ec808ec-1d2b-4b59-9b08-e532133aea99" />
Image for Error // if they  not complete they will become error
















