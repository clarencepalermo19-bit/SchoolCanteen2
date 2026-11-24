package com.mycompany.Controller;

import com.mycompany.Categories.CategoriesDAO;   //Kumokonekta sa database operations para sa Categories.
import com.mycompany.Model.Categories;           //Model na naglalaman ng category data.
import com.mycompany.Product.ProductDAO;         //Ginagamit para i-load ang categories sa Product dropdown at ang Product table.
import com.mycompany.View.SchoolCanteen1;
import java.awt.event.ActionEvent;               //Nagbibigay ng impormasyon kapag may button na pinindot.
import java.awt.event.ActionListener;            //Nakikinig sa button clicks.
import java.awt.event.MouseAdapter;             //Nakikinig sa mouse actions
import java.awt.event.MouseEvent;                //Nagbibigay ng impormasyon tungkol sa mouse click.
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;      //Ginagamit para baguhin ang laman ng JTable.

public class CategoriesController {

    SchoolCanteen1 sc;

    public CategoriesController(SchoolCanteen1 temp) {

        sc = temp;
        sc.jTextField8.setEditable(false);
        sc.allListeners(new AllActions());

        // TABLE ROW SELECTION
        sc.jTable2.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                int row = sc.jTable2.getSelectedRow();

                if (row != -1) {
                    sc.jTextField8.setText(sc.jTable2.getValueAt(row, 0).toString());
                    sc.jTextField9.setText(sc.jTable2.getValueAt(row, 1).toString());
                }
            }
        });

        loadTable();
        
            // RELOAD THE CATEGORY DROPDOWN AND TABLE IN THE PRODUCTS TAB
    }

    // GET DATA FROM TEXT FIELDS
    private Categories getCategoryFromFields() {

        String category_id = sc.jTextField8.getText();
        String category_name = sc.jTextField9.getText();

        Categories cat = new Categories();

        cat.setCategory_id(category_id);
        cat.setCategory_name(category_name);

        return cat;
    }

    // CLEAR TEXT FIELDS
    private void clearFields() {

        sc.jTextField8.setText("");
        sc.jTextField9.setText("");
        
        sc.jComboBox3.setSelectedIndex(0);
        
        sc.jTable2.clearSelection();
    }

    // READ / DISPLAY TABLE
    private void loadTable() {
        sc.jTable2.setModel(CategoriesDAO.getTableData());
    }
     private void reloadProductCategories() {
        ProductDAO.loadCategories(sc.jComboBox1);
        sc.jComboBox1.setSelectedIndex(-1);
        sc.jTable1.setModel(ProductDAO.getTableData());
          com.mycompany.Controller.SupplierController.reloadProductChoices(sc);
    }
    
    
    

    class AllActions implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

            // SAVE
            if (e.getSource() == sc.btnCreate1) {

    String categoryName = sc.jTextField9.getText().trim();

    if (categoryName.isEmpty()) {
        JOptionPane.showMessageDialog(sc, "Please enter category name!");
        return;
    }

    Categories cat = new Categories();
    cat.setCategory_name(categoryName);

    CategoriesDAO.save(cat);

    loadTable();
    clearFields();
    reloadProductCategories(); 
}
            // READ
            else if (e.getSource() == sc.btnRead1) {

                String category_id = sc.jTextField8.getText();

                if (category_id.trim().isEmpty()) {

                    loadTable();

                } else {

                    Categories cat = CategoriesDAO.getByID(category_id);

                    if (cat != null) {

                        DefaultTableModel model =
                                (DefaultTableModel) sc.jTable2.getModel();

                        model.setRowCount(0);

                        model.addRow(new Object[]{
                            cat.getCategory_id(),
                            cat.getCategory_name()
                        });

                    } else {
                        JOptionPane.showMessageDialog(null,
                                "Category ID not found!");
                    }
                }
            }

            // UPDATE
            else if (e.getSource() == sc.btnUpdate1) {

                if (sc.jTextField8.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(null,
                            "Please select a category from the table!");
                    return;
                }

                Categories cat = getCategoryFromFields();

                CategoriesDAO.update(cat, cat.getCategory_id());

                loadTable();
                clearFields();
                 reloadProductCategories();
            }

            // DELETE
            else if (e.getSource() == sc.btnDelete1) {

                String category_id = sc.jTextField8.getText();

                if (category_id.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(null,
                            "Please select a category to delete!");
                    return;
                }

                int confirm = JOptionPane.showConfirmDialog(
                        null,
                        "Are you sure you want to delete this category?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    CategoriesDAO.delete(category_id);
                    clearFields();
                    loadTable();
                     reloadProductCategories();
                }
            }

            // CLEAR
            else if (e.getSource() == sc.btnClear1) {
                clearFields();
            }
        }
    }
}

