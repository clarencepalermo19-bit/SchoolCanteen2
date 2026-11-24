package com.mycompany.StockIn;

import com.mycompany.Model.StockIn;
import com.mycompany.Product.ComboItem;
import com.mycompany.canteeninventory.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class StockInDAO {

    // STOCK LEVEL LIMITS (change these to suit your canteen)
    // quantity <= LOW_LIMIT  -> Low
    // quantity <= MEDIUM_LIMIT -> Medium
    // above that -> High
    private static final int LOW_LIMIT = 10;
    private static final int MEDIUM_LIMIT = 50;

    public static String getStockStatus(int qty) {
        if (qty <= LOW_LIMIT) {
            return "Low";
        } else if (qty <= MEDIUM_LIMIT) {
            return "Medium";
        }
        return "High";
    }

    // ADD (or subtract, if delta is negative) to the product's quantity
    // and refresh its stock_status. Runs inside the caller's transaction.
        // ADD (or subtract, if delta is negative) to the product's quantity.
    // stock_status is a generated column, so MySQL updates it automatically.
    private static String adjustStock(Connection conn, int productID, int delta)
            throws SQLException {

        try (PreparedStatement up = conn.prepareStatement(
                "UPDATE products SET quantity = quantity + ? WHERE product_id = ?")) {
            up.setInt(1, delta);
            up.setInt(2, productID);
            if (up.executeUpdate() == 0) {
                throw new SQLException("Product not found.");
            }
        }

        int qty;
        String status;

        try (PreparedStatement sel = conn.prepareStatement(
                "SELECT quantity, stock_status FROM products WHERE product_id = ?")) {
            sel.setInt(1, productID);
            try (ResultSet rs = sel.executeQuery()) {
                rs.next();
                qty = rs.getInt("quantity");
                status = rs.getString("stock_status");
            }
        }

        if (qty < 0) {
            throw new SQLException("Stock cannot go below zero.");
        }

        return "Current stock: " + qty + " (" + status + ")";
    }

    // CREATE
    public static boolean save(StockIn s) {

        try (Connection conn = DBConnection.getConnection()) {

            int productID = Integer.parseInt(s.getProductID());
            int qty = Integer.parseInt(s.getQuantityAdded());

            conn.setAutoCommit(false);

          try (PreparedStatement stmt = conn.prepareStatement(
        "INSERT INTO stock_in (product_id, quantity_added) VALUES (?, ?)")) {

                stmt.setInt(1, productID);
                stmt.setInt(2, qty);
                stmt.executeUpdate();

                String info = adjustStock(conn, productID, qty);

                conn.commit();
                JOptionPane.showMessageDialog(null, "Stock In Added!\n" + info);
                return true;

            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }

        } catch (SQLException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
            return false;
        }
    }

    // READ BY STOCK IN ID
    public static StockIn getByID(String stockInID) {

        StockIn s = null;

        String sql = "SELECT * FROM stock_in WHERE stock_in_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, Integer.parseInt(stockInID));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    s = new StockIn();
                    s.setStockInID(rs.getString("stock_in_id"));
                    s.setProductID(rs.getString("product_id"));
                    s.setQuantityAdded(rs.getString("quantity_added"));
                    s.setStockInDate(rs.getString("stock_in_date"));
                }
            }

        } catch (SQLException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }

        return s;
    }

    // UPDATE (also corrects the product stock)
    public static boolean update(StockIn s, String stockInID) {

        try (Connection conn = DBConnection.getConnection()) {

            int id = Integer.parseInt(stockInID);
            int newProduct = Integer.parseInt(s.getProductID());
            int newQty = Integer.parseInt(s.getQuantityAdded());

            conn.setAutoCommit(false);

            try {
                int oldProduct;
                int oldQty;

                try (PreparedStatement sel = conn.prepareStatement(
                        "SELECT product_id, quantity_added FROM stock_in "
                        + "WHERE stock_in_id = ? FOR UPDATE")) {
                    sel.setInt(1, id);
                    try (ResultSet rs = sel.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            JOptionPane.showMessageDialog(null, "Stock In record not found!");
                            return false;
                        }
                        oldProduct = rs.getInt("product_id");
                        oldQty = rs.getInt("quantity_added");
                    }
                }

                try (PreparedStatement up = conn.prepareStatement(
                        "UPDATE stock_in SET product_id = ?, quantity_added = ? "
                        + "WHERE stock_in_id = ?")) {
                    up.setInt(1, newProduct);
                    up.setInt(2, newQty);
                    up.setInt(3, id);
                    up.executeUpdate();
                }

                String info;
                if (oldProduct == newProduct) {
                    info = adjustStock(conn, newProduct, newQty - oldQty);
                } else {
                    adjustStock(conn, oldProduct, -oldQty);   // take back from old product
                    info = adjustStock(conn, newProduct, newQty); // add to new product
                }

                conn.commit();
                JOptionPane.showMessageDialog(null, "Stock In Updated!\n" + info);
                return true;

            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }

        } catch (SQLException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
            return false;
        }
    }

    // DELETE (also removes the quantity from the product stock)
    public static boolean delete(String stockInID) {

        try (Connection conn = DBConnection.getConnection()) {

            int id = Integer.parseInt(stockInID);

            conn.setAutoCommit(false);

            try {
                int productID;
                int qty;

                try (PreparedStatement sel = conn.prepareStatement(
                        "SELECT product_id, quantity_added FROM stock_in "
                        + "WHERE stock_in_id = ? FOR UPDATE")) {
                    sel.setInt(1, id);
                    try (ResultSet rs = sel.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            JOptionPane.showMessageDialog(null, "Stock In record not found!");
                            return false;
                        }
                        productID = rs.getInt("product_id");
                        qty = rs.getInt("quantity_added");
                    }
                }

                try (PreparedStatement del = conn.prepareStatement(
                        "DELETE FROM stock_in WHERE stock_in_id = ?")) {
                    del.setInt(1, id);
                    del.executeUpdate();
                }

                String info = adjustStock(conn, productID, -qty);

                conn.commit();
                JOptionPane.showMessageDialog(null, "Stock In Deleted!\n" + info);
                return true;

            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }

        } catch (SQLException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
            return false;
        }
    }

    // DISPLAY TABLE
    public static TableModel getTableData() {

        DefaultTableModel model = new DefaultTableModel();

        model.addColumn("Stock In ID");
        model.addColumn("Product ID");
        model.addColumn("Quantity Added");
        model.addColumn("Stock In Date");

        String sql = "SELECT * FROM stock_in ORDER BY stock_in_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("stock_in_id"),
                    rs.getInt("product_id"),
                    rs.getInt("quantity_added"),
                    rs.getTimestamp("stock_in_date")
                });
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }

        return model;
    }

    // LOAD PRODUCTS INTO COMBOBOX
    public static void loadProducts(JComboBox<ComboItem> combo) {

        String sql = "SELECT product_id, product_name FROM products ORDER BY product_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            combo.removeAllItems();

            // placeholder item (id 0 = nothing selected)
            combo.addItem(new ComboItem(0, "SELECT PRODUCT"));

            while (rs.next()) {
                combo.addItem(new ComboItem(
                        rs.getInt("product_id"),
                        rs.getString("product_name")));
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null,
                    "Unable to load products: " + ex.getMessage());
        }
    }
}