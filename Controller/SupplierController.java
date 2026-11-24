package com.mycompany.Controller;

import com.mycompany.Model.Supplier;
import com.mycompany.Product.ProductDAO;
import com.mycompany.Supplier.SupplierDAO;
import com.mycompany.View.SchoolCanteen1;
import com.mycompany.canteeninventory.DBConnection;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class SupplierController {

    private final SchoolCanteen1 sc;
    private final JComboBox<Object> combo;

    private static final String DEFAULT_CATEGORY = "Select Categories";

    @SuppressWarnings("unchecked")
    public SupplierController(SchoolCanteen1 sc) {

        this.sc = sc;
        combo = (JComboBox<Object>) (JComboBox<?>) sc.jComboBox3;

        // SUPPLIER ID IS AUTOMATIC
        sc.jTextField10.setEditable(false);

        // CATEGORY DROPDOWN: SELECT ONLY
        combo.setEditable(false);
        reloadProductChoices(sc);

        // BUTTON LISTENERS
        AllActions actions = new AllActions();
        sc.btnCreate2.addActionListener(actions);
        sc.btnRead2.addActionListener(actions);
        sc.btnUpdate2.addActionListener(actions);
        sc.btnDelete2.addActionListener(actions);
        sc.btnClear2.addActionListener(actions);

        // TABLE ROW SELECTION: click a row -> fill the form
        sc.jTable6.getSelectionModel().addListSelectionListener(e -> {

            if (!e.getValueIsAdjusting()) {

                int row = sc.jTable6.getSelectedRow();

                if (row >= 0) {

                    sc.jTextField10.setText(sc.jTable6.getValueAt(row, 0).toString());
                    sc.jTextField11.setText(sc.jTable6.getValueAt(row, 1).toString());

                    Object category = sc.jTable6.getValueAt(row, 2);
                    setCategory(category == null ? "" : category.toString());

                    Object contact = sc.jTable6.getValueAt(row, 3);
                    sc.jTextField13.setText(contact == null ? "" : contact.toString());
                }
            }
        });

        // SHOW THE SUPPLIERS WHEN THE APP STARTS
        refreshTable();

        System.out.println("SupplierController ready");
    }

    // ============================================
    // CATEGORY DROPDOWN
    // ============================================

    // PUBLIC AND STATIC SO THE CATEGORIES CONTROLLER CAN CALL IT
    @SuppressWarnings("unchecked")
    public static void reloadProductChoices(SchoolCanteen1 sc) {

        JComboBox<Object> box = (JComboBox<Object>) (JComboBox<?>) sc.jComboBox3;

        box.removeAllItems();

        // DEFAULT TEXT (first item)
        box.addItem(DEFAULT_CATEGORY);

        String sql = "SELECT category_name FROM categories ORDER BY category_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                box.addItem(rs.getString("category_name"));
            }

        } catch (SQLException ex) {
            System.out.println("Category list error: " + ex.getMessage());
        }

        box.setSelectedIndex(0);   // start on "Select Categories"
    }

    // RETURNS "" WHEN THE DEFAULT TEXT IS SELECTED
    private String getCategory() {

        Object selected = combo.getSelectedItem();

        if (selected == null) {
            return "";
        }

        String value = selected.toString().trim();

        return value.equals(DEFAULT_CATEGORY) ? "" : value;
    }

    private void setCategory(String value) {

        for (int i = 0; i < combo.getItemCount(); i++) {
            if (String.valueOf(combo.getItemAt(i)).equalsIgnoreCase(value)) {
                combo.setSelectedIndex(i);
                return;
            }
        }

        // not found or empty: go back to the default text
        if (combo.getItemCount() > 0) {
            combo.setSelectedIndex(0);
        }
    }

    // ============================================
    // FORM HELPERS
    // ============================================

    private Supplier getSupplierFromFields() {

        Supplier s = new Supplier();

        s.setSupplierID(sc.jTextField10.getText().trim());
        s.setSupplierName(sc.jTextField11.getText().trim());
        s.setProduct(getCategory());
        s.setContactNumber(sc.jTextField13.getText().trim());

        return s;
    }

    // ============================================
    // VALIDATE SUPPLIER INFORMATION
    // ============================================

    private boolean validateFields() {

        String supplierName = sc.jTextField11.getText().trim();
        String category = getCategory();
        String contactNumber = sc.jTextField13.getText().trim();

        // CHECK SUPPLIER NAME
        if (supplierName.isEmpty()) {
            JOptionPane.showMessageDialog(sc, "Please enter Supplier Name.");
            sc.jTextField11.requestFocus();
            return false;
        }

        if (supplierName.length() > 100) {
            JOptionPane.showMessageDialog(sc,
                    "Supplier Name must not exceed 100 characters.");
            sc.jTextField11.requestFocus();
            return false;
        }

        // CHECK CATEGORY
        if (category.isEmpty()) {
            JOptionPane.showMessageDialog(sc, "Please select a Category.");
            combo.requestFocus();
            return false;
        }

        if (category.length() > 50) {
            JOptionPane.showMessageDialog(sc,
                    "Category must not exceed 50 characters.");
            combo.requestFocus();
            return false;
        }

        // CHECK CONTACT NUMBER
        if (contactNumber.isEmpty()) {
            JOptionPane.showMessageDialog(sc, "Please enter Contact Number.");
            sc.jTextField13.requestFocus();
            return false;
        }

        // MUST BE EXACTLY 11 DIGITS AND START WITH 09
        if (!contactNumber.matches("09[0-9]{9}")) {
            JOptionPane.showMessageDialog(sc,
                    "Invalid Contact Number!\n"
                    + "Please enter a valid 11-digit mobile number.\n"
                    + "Example: 09123456789");

            sc.jTextField13.requestFocus();
            return false;
        }

        return true;
    }

    // ============================================
    // REFRESH TABLE / CLEAR FIELDS
    // ============================================

    private void refreshTable() {
        sc.jTable6.setModel(SupplierDAO.getTableData());
    }

    private void clearFields() {

        sc.jTextField10.setText("");
        sc.jTextField11.setText("");

        if (combo.getItemCount() > 0) {
            combo.setSelectedIndex(0);
        }

        sc.jTextField13.setText("");

        sc.jTable6.clearSelection();
    }

    // RELOAD THE SUPPLIER DROPDOWN AND TABLE IN THE PRODUCTS TAB
    private void reloadProductSuppliers() {

        ProductDAO.loadSuppliers(sc.jComboBox2);

        if (sc.jComboBox2.getItemCount() > 0) {
            sc.jComboBox2.setSelectedIndex(0);
        }

        sc.jTable1.setModel(ProductDAO.getTableData());
    }

    // ============================================
    // BUTTON ACTIONS
    // ============================================

    class AllActions implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

            try {
                Object source = e.getSource();

                // CREATE
                if (source == sc.btnCreate2) {

                    if (!validateFields()) {
                        return;
                    }

                    if (SupplierDAO.save(getSupplierFromFields())) {

                        JOptionPane.showMessageDialog(sc,
                                "Supplier saved successfully!");

                        refreshTable();
                        clearFields();
                        reloadProductSuppliers();

                    } else {
                        JOptionPane.showMessageDialog(sc, "Unable to save supplier.");
                    }
                }

                // READ
                else if (source == sc.btnRead2) {

                    String id = sc.jTextField10.getText().trim();

                    if (id.isEmpty()) {

                        refreshTable();

                    } else {

                        Supplier s = SupplierDAO.getByID(id);

                        if (s != null) {

                            DefaultTableModel model =
                                    (DefaultTableModel) sc.jTable6.getModel();

                            model.setRowCount(0);

                            model.addRow(new Object[]{
                                s.getSupplierID(),
                                s.getSupplierName(),
                                s.getCategories(),
                                s.getContactNumber()
                            });

                            sc.jTextField11.setText(s.getSupplierName());
                            setCategory(s.getCategories() == null ? "" : s.getCategories());
                            sc.jTextField13.setText(
                                    s.getContactNumber() == null ? "" : s.getContactNumber());

                        } else {
                            JOptionPane.showMessageDialog(sc, "Supplier ID not found!");
                            clearFields();
                            refreshTable();
                        }
                    }
                }

                // UPDATE
                else if (source == sc.btnUpdate2) {

                    if (sc.jTextField10.getText().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(sc,
                                "Please select a supplier first.");
                        return;
                    }

                    if (!validateFields()) {
                        return;
                    }

                    if (SupplierDAO.update(getSupplierFromFields())) {

                        JOptionPane.showMessageDialog(sc,
                                "Supplier updated successfully!");

                        refreshTable();
                        clearFields();
                        reloadProductSuppliers();

                    } else {
                        JOptionPane.showMessageDialog(sc, "Unable to update supplier.");
                    }
                }

                // DELETE
                else if (source == sc.btnDelete2) {

                    String id = sc.jTextField10.getText().trim();

                    if (id.isEmpty()) {
                        JOptionPane.showMessageDialog(sc,
                                "Please select a supplier first.");
                        return;
                    }

                    // NEW: block delete if this supplier still has products
                    if (SupplierDAO.hasProducts(id)) {
                      JOptionPane.showMessageDialog(sc,
        "Delete the products of this supplier first.");
                        return;
                    }

                    int confirm = JOptionPane.showConfirmDialog(
                            sc,
                            "Are you sure you want to delete this supplier?",
                            "Confirm Delete",
                            JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {

                        if (SupplierDAO.delete(id)) {

                            JOptionPane.showMessageDialog(sc,
                                    "Supplier deleted successfully!");

                            refreshTable();
                            clearFields();
                            reloadProductSuppliers();

                        } else {
                            JOptionPane.showMessageDialog(sc,
                                    "Unable to delete supplier.");
                        }
                    }
                }

                // CLEAR
                else if (source == sc.btnClear2) {
                    clearFields();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(sc, "Error: " + ex.getMessage(),
                        "Supplier Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}