/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.canteeninventory;

import com.mycompany.View.SchoolCanteen1;
import com.mycompany.Controller.CategoriesController;
import com.mycompany.Controller.ProductController;
import com.mycompany.Controller.StockInController;
import com.mycompany.Controller.StockOutController;
import com.mycompany.Controller.SupplierController;

/**
 *
 * @author Clarence
 */

public class CanteenInventory {

    public static void main(String[] args) {
        
        
       
        SchoolCanteen1 sc = new  SchoolCanteen1();
        new ProductController(sc);
        new CategoriesController(sc);
        new SupplierController(sc);
        new StockInController(sc);
        new StockOutController(sc);
       sc.setVisible(true);
    }
}
