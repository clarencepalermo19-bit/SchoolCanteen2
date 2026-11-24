 /*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.Model;

/**
 *
 * @author Clarence
 */
public class StockIn {
    
    private String stockInID;
    private String productID;
    private String quantityAdded;
    private String stockInDate;

    public String getStockInID() {
        return stockInID;
    }

    public void setStockInID(String stockInID) {
        this.stockInID = stockInID;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String getQuantityAdded() {
        return quantityAdded;
    }

    public void setQuantityAdded(String quantityAdded) {
        this.quantityAdded = quantityAdded;
    }

    public String getStockInDate() {
        return stockInDate;
    }

    public void setStockInDate(String stockInDate) {
        this.stockInDate = stockInDate;
    }
    
}
