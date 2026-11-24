/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.canteeninventory;

/**
 *
 * @author Clarence
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;


public class DBConnection {
  
    private static Connection conn;
    
    private static Connection createConnection(){
        try{
            String url = "jdbc:mysql://127.0.0.1:3306/schoolcanteendb";
            Class.forName("com.mysql.jdbc.Driver"); //the driver of the database
            conn = DriverManager.getConnection(url, "root", "022006");
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
            System.err.print(ex);
            JOptionPane.showMessageDialog(null,"Database Error!","Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return conn;
        }
        
    public static Connection getConnection(){
        try {
            if (conn == null || conn.isClosed() ) {
                conn = createConnection();
            }
        } catch (SQLException ex) {            
             JOptionPane.showMessageDialog(null,"Database Error!","Error", JOptionPane.ERROR_MESSAGE);
             Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
             
        }
        return conn;
    }
        
        }
