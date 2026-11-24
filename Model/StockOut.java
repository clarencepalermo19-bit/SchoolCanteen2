/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.Model;

/**
 *
 * @author Clarence
 */
public class StockOut {
    
     private String stockOutID;
    private String productID;
    private String quantityRemoved;
    private String stockOutDate;

    public String getStockOutID() {
        return stockOutID;
    }

    public void setStockOutID(String stockOutID) {
        this.stockOutID = stockOutID;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String getQuantityRemoved() {
        return quantityRemoved;
    }

    public void setQuantityRemoved(String quantityRemoved) {
        this.quantityRemoved = quantityRemoved;
    }

    public String getStockOutDate() {
        return stockOutDate;
    }

    public void setStockOutDate(String stockOutDate) {
        this.stockOutDate = stockOutDate;
    }
    
}
