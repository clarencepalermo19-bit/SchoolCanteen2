package com.mycompany.Categories;

import com.mycompany.Model.Categories;
import com.mycompany.canteeninventory.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class CategoriesDAO {

    // SAVE
    public static void save(Categories cat) {
   

    String sql = "INSERT INTO categories (category_name) VALUES (?)";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, cat.getCategory_name());

        stmt.executeUpdate();

        JOptionPane.showMessageDialog(null, "Category Added!");

    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(null, ex.getMessage());
    }
}

     

    // READ BY CATEGORY ID
    public static Categories getByID(String category_id) {

        Categories cat = null;

        try {
            Connection conn = DBConnection.getConnection();

            String sql = "SELECT * FROM categories WHERE category_id=?";

            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, category_id);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                cat = new Categories();

                cat.setCategory_id(rs.getString("category_id"));
                cat.setCategory_name(rs.getString("category_name"));
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex);
        }

        return cat;
    }

    // UPDATE
    public static void update(Categories cat, String category_id) {

        try {
            Connection conn = DBConnection.getConnection();

            String sql = "UPDATE categories SET category_name=? WHERE category_id=?";

            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, cat.getCategory_name());
            stmt.setString(2, category_id);

            stmt.executeUpdate();

            JOptionPane.showMessageDialog(null, "Category Updated!");

            stmt.close();
            conn.close();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex);
        }
    }

    // DELETE
    public static void delete(String category_id) {

        try {
            Connection conn = DBConnection.getConnection();

            String sql = "DELETE FROM categories WHERE category_id=?";

            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, category_id);

            stmt.executeUpdate();

            JOptionPane.showMessageDialog(null, "Category Deleted!");

            stmt.close();
            conn.close();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex);
        }
    }

    // DISPLAY TABLE
    public static TableModel getTableData() {

        DefaultTableModel model = new DefaultTableModel();

        model.addColumn("Category ID");
        model.addColumn("Category Name");

       String sql = "SELECT * FROM categories ORDER BY category_id ASC";

        try {

            Connection conn = DBConnection.getConnection();

            PreparedStatement stmt = conn.prepareStatement(sql);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                model.addRow(new Object[]{
                    rs.getString("category_id"),
                    rs.getString("category_name")
                });
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex);
        }

        return model;
    }
}
