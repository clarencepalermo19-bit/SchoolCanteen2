package com.mycompany.StockOut;

import com.mycompany.Model.StockOut;
import com.mycompany.canteeninventory.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class StockOutDAO {

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
            throw new SQLException("Not enough stock! Available: " + (qty - delta));
        }

        return "Current stock: " + qty + " (" + status + ")";
    }

    // CREATE
    public static boolean save(StockOut s) {

        try (Connection conn = DBConnection.getConnection()) {

            int productID = Integer.parseInt(s.getProductID());
            int qty = Integer.parseInt(s.getQuantityRemoved());

            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO stock_out (product_id, quantity_removed, stock_out_date) "
                    + "VALUES (?, ?, CURRENT_TIMESTAMP)")) {

                stmt.setInt(1, productID);
                stmt.setInt(2, qty);
                stmt.executeUpdate();

                String info = adjustStock(conn, productID, -qty);

                conn.commit();
                JOptionPane.showMessageDialog(null, "Stock Out Added!\n" + info);
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

    // READ BY STOCK OUT ID
    public static StockOut getByID(String stockOutID) {

        StockOut s = null;

        String sql = "SELECT * FROM stock_out WHERE stock_out_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, Integer.parseInt(stockOutID));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    s = new StockOut();
                    s.setStockOutID(rs.getString("stock_out_id"));
                    s.setProductID(rs.getString("product_id"));
                    s.setQuantityRemoved(rs.getString("quantity_removed"));
                    s.setStockOutDate(rs.getString("stock_out_date"));
                }
            }

        } catch (SQLException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }

        return s;
    }

    // UPDATE (also corrects the product stock)
    public static boolean update(StockOut s, String stockOutID) {

        try (Connection conn = DBConnection.getConnection()) {

            int id = Integer.parseInt(stockOutID);
            int newProduct = Integer.parseInt(s.getProductID());
            int newQty = Integer.parseInt(s.getQuantityRemoved());

            conn.setAutoCommit(false);

            try {
                int oldProduct;
                int oldQty;

                try (PreparedStatement sel = conn.prepareStatement(
                        "SELECT product_id, quantity_removed FROM stock_out "
                        + "WHERE stock_out_id = ? FOR UPDATE")) {
                    sel.setInt(1, id);
                    try (ResultSet rs = sel.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            JOptionPane.showMessageDialog(null, "Stock Out record not found!");
                            return false;
                        }
                        oldProduct = rs.getInt("product_id");
                        oldQty = rs.getInt("quantity_removed");
                    }
                }

                try (PreparedStatement up = conn.prepareStatement(
                        "UPDATE stock_out SET product_id = ?, quantity_removed = ? "
                        + "WHERE stock_out_id = ?")) {
                    up.setInt(1, newProduct);
                    up.setInt(2, newQty);
                    up.setInt(3, id);
                    up.executeUpdate();
                }

                String info;
                if (oldProduct == newProduct) {
                    info = adjustStock(conn, newProduct, oldQty - newQty);
                } else {
                    adjustStock(conn, oldProduct, oldQty);      // give back to old product
                    info = adjustStock(conn, newProduct, -newQty); // remove from new product
                }

                conn.commit();
                JOptionPane.showMessageDialog(null, "Stock Out Updated!\n" + info);
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

    // DELETE (gives the quantity back to the product)
    public static boolean delete(String stockOutID) {

        try (Connection conn = DBConnection.getConnection()) {

            int id = Integer.parseInt(stockOutID);

            conn.setAutoCommit(false);

            try {
                int productID;
                int qty;

                try (PreparedStatement sel = conn.prepareStatement(
                        "SELECT product_id, quantity_removed FROM stock_out "
                        + "WHERE stock_out_id = ? FOR UPDATE")) {
                    sel.setInt(1, id);
                    try (ResultSet rs = sel.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            JOptionPane.showMessageDialog(null, "Stock Out record not found!");
                            return false;
                        }
                        productID = rs.getInt("product_id");
                        qty = rs.getInt("quantity_removed");
                    }
                }

                try (PreparedStatement del = conn.prepareStatement(
                        "DELETE FROM stock_out WHERE stock_out_id = ?")) {
                    del.setInt(1, id);
                    del.executeUpdate();
                }

                String info = adjustStock(conn, productID, qty);

                conn.commit();
                JOptionPane.showMessageDialog(null, "Stock Out Deleted!\n" + info);
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

        model.addColumn("Stock Out ID");
        model.addColumn("Product ID");
        model.addColumn("Quantity Removed");
        model.addColumn("Stock Out Date");

        String sql = "SELECT * FROM stock_out ORDER BY stock_out_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("stock_out_id"),
                    rs.getInt("product_id"),
                    rs.getInt("quantity_removed"),
                    rs.getTimestamp("stock_out_date")
                });
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }

        return model;
    }
}