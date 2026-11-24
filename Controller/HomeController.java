package com.mycompany.Controller;

import com.mycompany.canteeninventory.DBConnection;
import com.mycompany.View.SchoolCanteen1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.table.DefaultTableModel;

public class HomeController {

    // Tab positions in jTabbedPane1
    private static final int TAB_HOME = 0;
    private static final int TAB_PRODUCTS = 3;
    private static final int TAB_STOCK_IN = 4;
    private static final int TAB_STOCK_OUT = 5;

    private final SchoolCanteen1 sc;

    public HomeController(SchoolCanteen1 temp) {

        sc = temp;

        // REFRESH EVERY TIME THE HOME TAB IS OPENED
        sc.jTabbedPane1.addChangeListener(e -> {
            if (sc.jTabbedPane1.getSelectedIndex() == TAB_HOME) {
                refresh();
            }
        });

        // QUICK ACTION BUTTONS
        sc.btnQuickStockIn.addActionListener(
                e -> sc.jTabbedPane1.setSelectedIndex(TAB_STOCK_IN));

        sc.btnQuickStockOut.addActionListener(
                e -> sc.jTabbedPane1.setSelectedIndex(TAB_STOCK_OUT));

        sc.btnQuickProducts.addActionListener(
                e -> sc.jTabbedPane1.setSelectedIndex(TAB_PRODUCTS));

        refresh();
    }

    // UPDATE ALL CARDS AND THE LOW STOCK TABLE
    public void refresh() {

        sc.lblTotalProducts.setText(
                String.valueOf(count("SELECT COUNT(*) FROM products")));

        sc.lblTotalCategories.setText(
                String.valueOf(count("SELECT COUNT(*) FROM categories")));

        sc.lblTotalSuppliers.setText(
                String.valueOf(count("SELECT COUNT(*) FROM suppliers")));

        sc.lblLowStock.setText(
                String.valueOf(count(
                        "SELECT COUNT(*) FROM products WHERE stock_status = 'low'")));

        sc.tblLowStock.setModel(getLowStockModel());
    }

    // RUN A COUNT QUERY
    private int count(String sql) {

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException ex) {
            System.out.println("Home count error: " + ex.getMessage());
        }

        return 0;
    }

    // LOW STOCK TABLE (lowest quantity first, not editable)
    private DefaultTableModel getLowStockModel() {

        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        model.addColumn("Product");
        model.addColumn("Quantity");
        model.addColumn("Status");

        String sql = "SELECT product_name, quantity, stock_status " //statement or query or req 
                   + "FROM products WHERE stock_status = 'low' "
                   + "ORDER BY quantity ASC, product_name";

        try (Connection conn = DBConnection.getConnection();  
             PreparedStatement stmt = conn.prepareStatement(sql); // stmt gina convert or prep ang sql nga variable into a query 
             ResultSet rs = stmt.executeQuery()) {  //rs ma execute sang query

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("product_name"),
                    rs.getInt("quantity"),
                    rs.getString("stock_status")
                });
            }

        } catch (SQLException ex) {
            System.out.println("Home low stock error: " + ex.getMessage());
        }

        return model;
    }
}