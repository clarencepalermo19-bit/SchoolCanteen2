package com.mycompany.Controller;

import com.mycompany.Model.StockOut;
import com.mycompany.Product.ComboItem;
import com.mycompany.Product.ProductDAO;
import com.mycompany.StockIn.StockInDAO;
import com.mycompany.StockOut.StockOutDAO;
import com.mycompany.View.SchoolCanteen1;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

public class StockOutController {

    private final SchoolCanteen1 sc;
    private final JComboBox<ComboItem> combo;

    private final DateTimeFormatter dateFormat =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @SuppressWarnings("unchecked")
    public StockOutController(SchoolCanteen1 temp) {

        sc = temp;

        // CONNECT TO PRODUCT COMBOBOX
        combo = (JComboBox<ComboItem>) (JComboBox<?>) sc.cmbProductOut;

        // MAKE FIELDS READ-ONLY
        sc.jTextField18.setEditable(false); // Stock Out ID
        sc.jTextField21.setEditable(false); // Stock Out Date
        sc.jTextField21.setFocusable(false);

        // LOAD PRODUCTS (same list as Stock In)
        StockInDAO.loadProducts(combo);
        combo.setEditable(false);

        if (combo.getItemCount() > 0) {
            combo.setSelectedIndex(0);
        }

        // SHOW CURRENT DATE WHEN A PRODUCT IS SELECTED
        combo.addActionListener(e -> {
            if (sc.jTextField18.getText().trim().isEmpty()) {
                if (getSelectedProductID().isEmpty()) {
                    sc.jTextField21.setText("");
                } else {
                    showCurrentDate();
                }
            }
        });

        // BUTTON LISTENERS
        AllActions actions = new AllActions();
        sc.btnCreate4.addActionListener(actions);
        sc.btnRead4.addActionListener(actions);
        sc.btnUpdate4.addActionListener(actions);
        sc.btnDelete4.addActionListener(actions);
        sc.btnClear4.addActionListener(actions);

        // TABLE ROW SELECTION
        sc.jTable8.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                int row = sc.jTable8.getSelectedRow();

                if (row != -1) {
                    sc.jTextField18.setText(sc.jTable8.getValueAt(row, 0).toString());
                    selectProductByID(sc.jTable8.getValueAt(row, 1).toString());
                    sc.jTextField20.setText(sc.jTable8.getValueAt(row, 2).toString());
                    sc.jTextField21.setText(formatDate(sc.jTable8.getValueAt(row, 3).toString()));
                }
            }
        });

        loadTable();
        clearFields();
    }

    private void showCurrentDate() {
        sc.jTextField21.setText(LocalDateTime.now().format(dateFormat));
    }

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

    // GET SELECTED PRODUCT ID ("" if SELECT PRODUCT)
    private String getSelectedProductID() {

        Object selected = combo.getSelectedItem();

        if (!(selected instanceof ComboItem)) {
            return "";
        }

        ComboItem item = (ComboItem) selected;

        if (item.getId() == 0) {
            return "";
        }

        return String.valueOf(item.getId());
    }

    // SELECT PRODUCT USING PRODUCT ID
    private void selectProductByID(String productID) {

        for (int i = 0; i < combo.getItemCount(); i++) {
            ComboItem item = combo.getItemAt(i);
            if (item != null && String.valueOf(item.getId()).equals(productID)) {
                combo.setSelectedIndex(i);
                return;
            }
        }

        if (combo.getItemCount() > 0) {
            combo.setSelectedIndex(0);
        }
    }

    // GET FORM DATA
    private StockOut getStockOutFromFields() {

        StockOut s = new StockOut();

        s.setStockOutID(sc.jTextField18.getText().trim());
        s.setProductID(getSelectedProductID());
        s.setQuantityRemoved(sc.jTextField20.getText().trim());

        return s;
    }

    // VALIDATE INPUTS
    private boolean validateFields() {

        String productID = getSelectedProductID();
        String quantity = sc.jTextField20.getText().trim();

        if (productID.isEmpty()) {
            JOptionPane.showMessageDialog(sc, "Please select a product!");
            return false;
        }

        if (quantity.isEmpty()) {
            JOptionPane.showMessageDialog(sc, "Please enter Quantity Remove!");
            return false;
        }

        try {
            if (Integer.parseInt(quantity) <= 0) {
                JOptionPane.showMessageDialog(sc, "Quantity must be greater than zero!");
                return false;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(sc, "Quantity must contain whole numbers only!");
            return false;
        }

        return true;
    }

    // CLEAR FORM
    private void clearFields() {

        sc.jTextField18.setText("");

        if (combo.getItemCount() > 0) {
            combo.setSelectedIndex(0);
        }

        sc.jTextField20.setText("");
        sc.jTextField21.setText("");
        sc.jTable8.clearSelection();
    }

    // LOAD TABLE
    private void loadTable() {
        sc.jTable8.setModel(StockOutDAO.getTableData());
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
                if (src == sc.btnCreate4) {

                    if (!validateFields()) {
                        return;
                    }

                    if (StockOutDAO.save(getStockOutFromFields())) {
                        loadTable();
                        clearFields();
                        refreshProductTable();
                    }
                }

                // READ
                else if (src == sc.btnRead4) {

                    String id = sc.jTextField18.getText().trim();

                    if (id.isEmpty()) {

                        loadTable();                  // nothing selected: show all

                    } else {

                        StockOut s = StockOutDAO.getByID(id);

                        if (s != null) {

                            // TABLE: show only this record
                            javax.swing.table.DefaultTableModel model =
                                    (javax.swing.table.DefaultTableModel) sc.jTable8.getModel();

                            model.setRowCount(0);

                            model.addRow(new Object[]{
                                s.getStockOutID(),
                                s.getProductID(),
                                s.getQuantityRemoved(),
                                formatDate(s.getStockOutDate())
                            });

                            // FORM: show the saved data
                            sc.jTextField18.setText(s.getStockOutID());
                            selectProductByID(s.getProductID());
                            sc.jTextField20.setText(s.getQuantityRemoved());
                            sc.jTextField21.setText(formatDate(s.getStockOutDate()));

                        } else {

                            JOptionPane.showMessageDialog(sc, "Stock Out ID not found!");
                            clearFields();
                            loadTable();
                        }
                    }
                }

                // UPDATE
                else if (src == sc.btnUpdate4) {

                    String id = sc.jTextField18.getText().trim();

                    if (id.isEmpty()) {
                        JOptionPane.showMessageDialog(sc,
                                "Please select a Stock Out record from the table!");
                        return;
                    }

                    if (!validateFields()) {
                        return;
                    }

                    if (StockOutDAO.update(getStockOutFromFields(), id)) {
                        loadTable();
                        clearFields();
                        refreshProductTable();
                    }
                }

                // DELETE
                else if (src == sc.btnDelete4) {

                    String id = sc.jTextField18.getText().trim();

                    if (id.isEmpty()) {
                        JOptionPane.showMessageDialog(sc,
                                "Please select a Stock Out record from the table!");
                        return;
                    }

                    int confirm = JOptionPane.showConfirmDialog(sc,
                            "Are you sure you want to delete this record?\n"
                            + "The quantity will be returned to the product stock.",
                            "Confirm Delete",
                            JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        if (StockOutDAO.delete(id)) {
                            loadTable();
                            clearFields();
                            refreshProductTable();
                        }
                    }
                }

                // CLEAR
                else if (src == sc.btnClear4) {
                    clearFields();
                    loadTable();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(sc, "Error: " + ex.getMessage(),
                        "Stock Out Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}