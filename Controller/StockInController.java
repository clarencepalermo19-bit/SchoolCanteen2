package com.mycompany.Controller;

import com.mycompany.Model.StockIn;
import com.mycompany.Product.ComboItem;
import com.mycompany.Product.ProductDAO;
import com.mycompany.StockIn.StockInDAO;
import com.mycompany.View.SchoolCanteen1;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

public class StockInController {

    private final SchoolCanteen1 sc;
    private final JComboBox<ComboItem> combo;

    private final DateTimeFormatter dateFormat =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @SuppressWarnings("unchecked")
    public StockInController(SchoolCanteen1 temp) {

        sc = temp;

        // CONNECT TO PRODUCT COMBOBOX
        combo = (JComboBox<ComboItem>) (JComboBox<?>) sc.cmbProduct;

        // MAKE FIELDS READ-ONLY
        sc.jTextField15.setEditable(false); // Stock In ID
        sc.jTextField17.setEditable(false); // Stock In Date
        sc.jTextField17.setFocusable(false);

        // LOAD PRODUCTS ONLY ONCE
        StockInDAO.loadProducts(combo);

        // COMBOBOX SETTINGS
        combo.setEditable(false);

        if (combo.getItemCount() > 0) {
            combo.setSelectedIndex(0);
        }

        // UPDATE DATE WHEN A PRODUCT IS SELECTED
        combo.addActionListener(e -> {
            if (getSelectedProductID().isEmpty()) {
                sc.jTextField17.setText("");
            } else {
                showCurrentDate();
            }
        });

        // REGISTER BUTTON LISTENERS
        AllActions actions = new AllActions();

        sc.btnCreate3.addActionListener(actions);
        sc.btnRead3.addActionListener(actions);
        sc.btnUpdate3.addActionListener(actions);
        sc.btnDelete3.addActionListener(actions);
        sc.btnClear3.addActionListener(actions);

        // TABLE ROW SELECTION
        sc.jTable7.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                int row = sc.jTable7.getSelectedRow();

                if (row != -1) {

                    // Stock In ID
                    sc.jTextField15.setText(
                            sc.jTable7.getValueAt(row, 0).toString()
                    );

                    // Product ID
                    selectProductByID(
                            sc.jTable7.getValueAt(row, 1).toString()
                    );

                    // Quantity Added
                    sc.jTextField16.setText(
                            sc.jTable7.getValueAt(row, 2).toString()
                    );

                    // Stock In Date
                    String date = sc.jTable7.getValueAt(row, 3).toString();
                    sc.jTextField17.setText(formatDate(date));
                }
            }
        });

        // DISPLAY STOCK IN RECORDS
        loadTable();

        // START WITH A CLEAN FORM
        clearFields();
    }

    // DISPLAY CURRENT DATE AND TIME
    private void showCurrentDate() {
        sc.jTextField17.setText(
                LocalDateTime.now().format(dateFormat)
        );
    }

    // FORMAT DATE
    private String formatDate(String date) {

        if (date == null) {
            return "";
        }

        date = date.trim();

        if (date.length() > 19) {
            date = date.substring(0, 19);
        }

        return date;
    }

    // GET SELECTED PRODUCT ID
    private String getSelectedProductID() {

        Object selected = combo.getSelectedItem();

        if (!(selected instanceof ComboItem)) {
            return "";
        }

        ComboItem item = (ComboItem) selected;

        // ID 0 means SELECT PRODUCT
        if (item.getId() == 0) {
            return "";
        }

        return String.valueOf(item.getId());
    }

    // SELECT PRODUCT USING PRODUCT ID
    private void selectProductByID(String productID) {

        if (productID == null || productID.trim().isEmpty()) {
            if (combo.getItemCount() > 0) {
                combo.setSelectedIndex(0);
            }
            return;
        }

        for (int i = 0; i < combo.getItemCount(); i++) {

            ComboItem item = combo.getItemAt(i);

            if (item != null
                    && String.valueOf(item.getId()).equals(productID)) {

                combo.setSelectedIndex(i);
                return;
            }
        }

        if (combo.getItemCount() > 0) {
            combo.setSelectedIndex(0);
        }
    }

    // GET FORM DATA
    private StockIn getStockInFromFields() {

        StockIn s = new StockIn();

        s.setStockInID(sc.jTextField15.getText().trim());
        s.setProductID(getSelectedProductID());
        s.setQuantityAdded(sc.jTextField16.getText().trim());

        return s;
    }

    // VALIDATE INPUTS
    private boolean validateFields() {

        String productID = getSelectedProductID();
        String quantity = sc.jTextField16.getText().trim();

        if (productID.isEmpty()) {
            JOptionPane.showMessageDialog(sc, "Please select a product!");
            return false;
        }

        if (quantity.isEmpty()) {
            JOptionPane.showMessageDialog(sc, "Please enter Quantity Added!");
            return false;
        }

        try {
            int qty = Integer.parseInt(quantity);

            if (qty <= 0) {
                JOptionPane.showMessageDialog(sc,
                        "Quantity must be greater than zero!");
                return false;
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(sc,
                    "Quantity must contain whole numbers only!");
            return false;
        }

        return true;
    }

    // CLEAR FORM
    private void clearFields() {

        sc.jTextField15.setText("");

        if (combo.getItemCount() > 0) {
            combo.setSelectedIndex(0);
        }

        sc.jTextField16.setText("");

        // No product selected, so clear the date preview
        sc.jTextField17.setText("");

        sc.jTable7.clearSelection();
    }

    // LOAD TABLE DATA
    private void loadTable() {
        sc.jTable7.setModel(StockInDAO.getTableData());
    }

    // RELOAD THE PRODUCTS TAB SO THE NEW QUANTITY AND STATUS SHOW AT ONCE
    private void refreshProductTable() {
        sc.jTable1.setModel(ProductDAO.getTableData());
    }

    // BUTTON ACTIONS
    class AllActions implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

            try {
                Object src = e.getSource();

                // CREATE
                if (src == sc.btnCreate3) {

                    if (!validateFields()) {
                        return;
                    }

                    if (StockInDAO.save(getStockInFromFields())) {

                        loadTable();
                        clearFields();
                        refreshProductTable();
                    }
                }

                // READ
                else if (src == sc.btnRead3) {

                    String id = sc.jTextField15.getText().trim();

                    if (id.isEmpty()) {

                        loadTable();                  // nothing selected: show all

                    } else {

                        StockIn s = StockInDAO.getByID(id);

                        if (s != null) {

                            // TABLE: show only this record
                            javax.swing.table.DefaultTableModel model =
                                    (javax.swing.table.DefaultTableModel) sc.jTable7.getModel();

                            model.setRowCount(0);

                            model.addRow(new Object[]{
                                s.getStockInID(),
                                s.getProductID(),
                                s.getQuantityAdded(),
                                formatDate(s.getStockInDate())
                            });

                            // FORM: show the saved data
                            sc.jTextField15.setText(s.getStockInID());
                            selectProductByID(s.getProductID());
                            sc.jTextField16.setText(s.getQuantityAdded());
                            sc.jTextField17.setText(formatDate(s.getStockInDate()));

                        } else {

                            JOptionPane.showMessageDialog(sc, "Stock In ID not found!");
                            clearFields();
                            loadTable();
                        }
                    }
                }

                // UPDATE
                else if (src == sc.btnUpdate3) {

                    String id = sc.jTextField15.getText().trim();

                    if (id.isEmpty()) {
                        JOptionPane.showMessageDialog(sc,
                                "Please select a Stock In record first!");
                        return;
                    }

                    if (!validateFields()) {
                        return;
                    }

                    if (StockInDAO.update(getStockInFromFields(), id)) {

                        loadTable();
                        clearFields();
                        refreshProductTable();
                    }
                }

                // DELETE
                else if (src == sc.btnDelete3) {

                    String id = sc.jTextField15.getText().trim();

                    if (id.isEmpty()) {
                        JOptionPane.showMessageDialog(sc,
                                "Please select a Stock In record first!");
                        return;
                    }

                    int confirm = JOptionPane.showConfirmDialog(
                            sc,
                            "Are you sure you want to delete this record?\n"
                            + "The quantity will also be deducted from product stock.",
                            "Confirm Delete",
                            JOptionPane.YES_NO_OPTION
                    );

                    if (confirm == JOptionPane.YES_OPTION) {

                        if (StockInDAO.delete(id)) {

                            loadTable();
                            clearFields();
                            refreshProductTable();
                        }
                    }
                }

                // CLEAR
                else if (src == sc.btnClear3) {

                    clearFields();
                    loadTable();
                }

            } catch (Exception ex) {

                ex.printStackTrace();

                JOptionPane.showMessageDialog(sc,
                        "Error: " + ex.getMessage(),
                        "Stock In Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}