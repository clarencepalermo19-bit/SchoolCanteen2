package com.mycompany.Report;

import com.mycompany.canteeninventory.DBConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class ReportDAO {

    // ONE VALUE FROM A QUERY (COUNT, SUM...)
    private static String scalar(Connection con, String sql) throws SQLException {

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            return rs.next() ? rs.getString(1) : "0";
        }
    }

    // SUMMARY NUMBERS (label -> value)
    public static Map<String, String> getSummary() {

        Map<String, String> summary = new LinkedHashMap<>();

        try (Connection con = DBConnection.getConnection()) {

            summary.put("Categories",
                    scalar(con, "SELECT COUNT(*) FROM categories"));

            summary.put("Suppliers",
                    scalar(con, "SELECT COUNT(*) FROM suppliers"));

            summary.put("Products",
                    scalar(con, "SELECT COUNT(*) FROM products"));

            summary.put("Units in Stock",
                    scalar(con, "SELECT COALESCE(SUM(quantity), 0) FROM products"));

            summary.put("Stock Value (PHP)",
                    scalar(con, "SELECT FORMAT(COALESCE(SUM(unit_price * quantity), 0), 2) FROM products"));

            summary.put("Low Stock Items",
                    scalar(con, "SELECT COUNT(*) FROM products WHERE stock_status = 'low'"));

            summary.put("Stock In Records",
                    scalar(con, "SELECT COUNT(*) FROM stock_in"));

            summary.put("Units Received",
                    scalar(con, "SELECT COALESCE(SUM(quantity_added), 0) FROM stock_in"));

            summary.put("Stock Out Records",
                    scalar(con, "SELECT COUNT(*) FROM stock_out"));

            summary.put("Units Released",
                    scalar(con, "SELECT COALESCE(SUM(quantity_removed), 0) FROM stock_out"));

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Report Error: " + e.getMessage());
        }

        return summary;
    }

    // RUN A SELECT AND PUT THE RESULT IN A READ-ONLY TABLE MODEL
    private static TableModel query(String sql, String... headers) {

        DefaultTableModel model = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            int cols = rs.getMetaData().getColumnCount();

            while (rs.next()) {

                Object[] row = new Object[cols];

                for (int i = 0; i < cols; i++) {
                    row[i] = rs.getObject(i + 1);
                }

                model.addRow(row);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Report Error: " + e.getMessage());
        }

        return model;
    }

    // PRODUCTS (with category and supplier names)
    public static TableModel getProductsReport() {

        return query(
                "SELECT p.product_id, p.product_name, c.category_name, "
                + "COALESCE(s.supplier_name, 'No supplier'), "
                + "p.unit_price, p.quantity, p.stock_status "
                + "FROM products p "
                + "INNER JOIN categories c ON p.category_id = c.category_id "
                + "LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id "
                + "ORDER BY p.product_id",
                "Product ID", "Product Name", "Category", "Supplier",
                "Unit Price", "Quantity", "Stock Status");
    }

    // PRODUCTS THAT NEED RESTOCKING
    public static TableModel getLowStockReport() {

        return query(
                "SELECT p.product_id, p.product_name, c.category_name, "
                + "p.quantity, p.stock_status "
                + "FROM products p "
                + "INNER JOIN categories c ON p.category_id = c.category_id "
                + "WHERE p.stock_status = 'low' "
                + "ORDER BY p.quantity ASC",
                "Product ID", "Product Name", "Category", "Quantity", "Stock Status");
    }

    // CATEGORIES (with number of products and units)
    public static TableModel getCategoriesReport() {

        return query(
                "SELECT c.category_id, c.category_name, "
                + "COUNT(p.product_id), COALESCE(SUM(p.quantity), 0) "
                + "FROM categories c "
                + "LEFT JOIN products p ON c.category_id = p.category_id "
                + "GROUP BY c.category_id, c.category_name "
                + "ORDER BY c.category_id",
                "Category ID", "Category Name", "Products", "Units in Stock");
    }

    // SUPPLIERS (with number of products supplied)
    public static TableModel getSuppliersReport() {

        return query(
                "SELECT s.supplier_id, s.supplier_name, s.categories, "
                + "s.contact_number, COUNT(p.product_id) "
                + "FROM suppliers s "
                + "LEFT JOIN products p ON s.supplier_id = p.supplier_id "
                + "GROUP BY s.supplier_id, s.supplier_name, s.categories, s.contact_number "
                + "ORDER BY s.supplier_id",
                "Supplier ID", "Supplier Name", "Categories",
                "Contact Number", "Products Supplied");
    }

    // STOCK IN HISTORY
    public static TableModel getStockInReport() {

        return query(
                "SELECT si.stock_in_id, p.product_name, si.quantity_added, "
                + "DATE_FORMAT(si.stock_in_date, '%Y-%m-%d %H:%i:%s') "
                + "FROM stock_in si "
                + "INNER JOIN products p ON si.product_id = p.product_id "
                + "ORDER BY si.stock_in_date DESC",
                "Stock In ID", "Product", "Quantity Added", "Stock In Date");
    }

    // STOCK OUT HISTORY
    public static TableModel getStockOutReport() {

        return query(
                "SELECT so.stock_out_id, p.product_name, so.quantity_removed, "
                + "DATE_FORMAT(so.stock_out_date, '%Y-%m-%d %H:%i:%s') "
                + "FROM stock_out so "
                + "INNER JOIN products p ON so.product_id = p.product_id "
                + "ORDER BY so.stock_out_date DESC",
                "Stock Out ID", "Product", "Quantity Removed", "Stock Out Date");
    }
}