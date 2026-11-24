package com.mycompany.Model;

public class Supplier {

    private String supplierID;
    private String supplierName;
    private String categories;
    private String contactNumber;

    public String getSupplierID() { return supplierID; }
    public void setSupplierID(String supplierID) { this.supplierID = supplierID; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public String getCategories() { return categories; }
    public void setCategories(String categories) { this.categories = categories; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    // OLD NAMES: kept so older code still works (same field)
    public String getProduct() { return categories; }
    public void setProduct(String product) { this.categories = product; }
}