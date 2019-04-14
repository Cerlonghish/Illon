package com.example.android.illon;
//classe prodotto (corrisponde alla riga della lista prodotti della schermata USER_LAYOUT
//la grafica di ogni riga è in PRODUCT_ITEM.xml
public class ProductRowItem {
    private String productName;
    private String sourcePic;
    private int price;

    public ProductRowItem(String productName, int price, String sourcePic) {
        this.price=price;
        this.productName=productName;
        this.sourcePic=sourcePic;
    }

    public int getPrice() {
        return price;
    }

    public String getProductName() {
        return productName;
    }

    public String getSourcePic() {
        return sourcePic;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setSourcePic(String sourcePic) {
        this.sourcePic = sourcePic;
    }


}
