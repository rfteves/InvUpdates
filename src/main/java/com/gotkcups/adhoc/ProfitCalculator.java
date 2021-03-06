/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.adhoc;

import com.gotkcups.data.Constants;
import com.gotkcups.data.JDocument;
import com.gotkcups.io.GateWay;
import com.gotkcups.page.DocumentProcessor;
import static com.gotkcups.page.DocumentProcessor.MARKUP_NON_TAXABLE;
import static com.gotkcups.page.DocumentProcessor.MARKUP_TAXABLE;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.bson.Document;

/**
 *
 * @author rfteves
 */
public class ProfitCalculator {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws Exception {
    list();
  }
  
  private static void list() throws IOException {
    int limit = 0;
    Map<String, String> params = new HashMap<>();
    params.put("fields", "id,title,product_type,variants");
    Set<Document> sorted = new TreeSet<>();
    Document resp = GateWay.getAllProducts("prod", params, 150, -1);
    List<Document> products = (List) resp.get("products");
    for (Document product : products) {
      if (!(product.getLong("id") == 9729878410L
        || product.getLong("id") == 15555012793338378l
        || product.getLong("id") == 10135855550303382l)) {
        //continue;
      }
      List<Document> variants = (List) product.get("variants");
      for (Document variant : variants) {
        if (variant.getLong(Constants.Id) != 11838139146l) {
          //continue;
        }
        if (!variant.getString(Constants.Sku).toLowerCase().endsWith("1074897c")) {
          //continue;
        }
        if (limit++ > 200) {
          //break;
        }
        Document d = new JDocument();
        variant.put(Constants.Title, product.getString(Constants.Title));
        d.putAll(variant);
        sorted.add(d);
      }
    }
    StringBuilder builder = new StringBuilder();
    builder.append("id");
    builder.append(SEPARATOR);
    builder.append("description");
    builder.append(SEPARATOR);
    builder.append("profit");
    builder.append(SEPARATOR);
    builder.append("label");
    System.out.println(builder.toString());
    for (Document variant : sorted) {
      Document metafield = GateWay.getProductMetafield("prod", variant.getLong(Constants.Product_Id), Constants.Inventory, Constants.Vendor);
      Document adwords = GateWay.getProductMetafieldsPloy(Constants.Production, variant.getLong(Constants.Product_Id));
      builder.setLength(0);
      builder.append("shopify_us_");
      builder.append(variant.getLong(Constants.Product_Id));
      builder.append("_");
      builder.append(variant.getLong(Constants.Id));
      builder.append(SEPARATOR);
      builder.append(variant.getString(Constants.Title).replaceAll(",", ""));
      builder.append(SEPARATOR);
      boolean taxable = !variant.getBoolean("taxable");
      double price = Double.parseDouble(variant.getString(Constants.Price));
      double cost = 0;
      if (taxable) {
        cost = price * (MARKUP_TAXABLE + AVERAGE_TAX_RATE);
      } else {
        cost = price * MARKUP_NON_TAXABLE;
      }
      double defaultshipping = 0;
      double discount = 0;
      double extraCost = 0;
      int minqty = 1;
      if (metafield != null) {
        String value = metafield.getString("value");
        Document values = Document.parse(value);
        Document vendor = (Document)values.get("vendor");
        if (vendor.containsKey(Constants.Default_Min_Quantity)) {
          minqty = vendor.getInteger(Constants.Default_Min_Quantity);
        }
        if (vendor.containsKey(Constants.DefaultShipping)) {
          defaultshipping = vendor.getDouble(Constants.DefaultShipping);
        }
        if (minqty >= 5 && price > 50) {
          discount = minqty * DocumentProcessor.BUNDLE_DISCOUNT;
        }
        if (vendor.containsKey(Constants.ExtraCost)) {
          extraCost = minqty * vendor.getDouble(Constants.ExtraCost);
        }
      } else {
        /*System.out.println("BBBBBBBBBBBBBBBBBBBBB");
        System.out.println("BBBBBBBBBBBBBBBBBBBBB");
        System.out.println("BBBBBBBBBBBBBBBBBBBBB");
        System.out.println("BBBBBBBBBBBBBBBBBBBBB");
        System.out.println(variant.toJson());
        System.exit(-1);*/
      }
      cost -= defaultshipping;
      cost -= extraCost;
      cost += discount;
      double profit = Math.round((price - cost) * 100) * 0.01;
      builder.append(BigDecimal.valueOf(profit).setScale(2, RoundingMode.HALF_UP));
      builder.append(SEPARATOR);
      builder.append(adwords.getString("custom_label_0"));
      builder.append(SEPARATOR);
      builder.append(defaultshipping);
      System.out.println(builder.toString());
    }
  }
  
  public static String SEPARATOR = ",";
  public static double AVERAGE_TAX_RATE = 0.08;
}
