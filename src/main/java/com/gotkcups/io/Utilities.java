
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.io;

import com.gotkcups.data.Constants;
import com.gotkcups.data.KeurigSelect;
import com.gotkcups.data.RequestsHandler;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.bson.Document;

/**
 *
 * @author rfteves
 */
public class Utilities {

  public static void main(String[] s) throws Exception {
    String options = "<select id=\"package-variant-select\" class=\"selectpicker-boxcount\"> <option data-info=\"24 Count\" data-price=\"$13.99\" data-isDiscountPrice=\"false\" data-discount-price=\"\" data-ad-price=\"$10.49\" data-count=\"24\" data-stock=\"inStock\" data-onsale-value=\"\" data-purchasable=\"true\" data-default=\"true\" data-code=\"5000057852\" data-product-type=\"Kcup\" data-product-bmsmEligible=\"false\" data-product-bmsmPriceRows='' data-product-maxOrderQuantity=\"20\" title=\"24 Count\" data-content=\"<span class='count-info'>24 Count</span><span class='right'>$13.99</span>\"> 24 Count <!-- $13.99 --> </option> </select>";
    KeurigSelect select = (KeurigSelect) Utilities.objectify(options, new KeurigSelect());
  }

  private static Map<String, String> KEYS = new HashMap<String, String>();

  static {
    logger = Logger.getLogger(Utilities.class.getName());
    System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.client.protocol.ResponseProcessCookies", "fatal");
    Utilities.initKeys();
  }

  public static void initProductionKeys() {
    KEYS.put("prod", Utilities.getApplicationProperty("https.key"));
  }

  private static void initKeys() {
    StringBuilder sb = new StringBuilder("https://");
    sb.append(Utilities.getApplicationProperty("key.prod"));
    sb.append(":");
    sb.append(Utilities.getApplicationProperty("password.prod"));
    sb.append("@");
    sb.append(Utilities.getApplicationProperty("store.prod"));
    sb.append(".myshopify.com");
    KEYS.put("prod", sb.toString());
    sb = new StringBuilder("https://");
    sb.append(Utilities.getApplicationProperty("key.dev"));
    sb.append(":");
    sb.append(Utilities.getApplicationProperty("password.dev"));
    sb.append("@");
    sb.append(Utilities.getApplicationProperty("store.dev"));
    sb.append(".myshopify.com");
    KEYS.put("dev", sb.toString());
  }

  public static String insertSpace(String span) {
    while (true) {
      Matcher m = Pattern.compile(" [0-9a-zA-Z\\-]+=\"[0-9a-zA-Z\\-$.]*\"[0-9a-zA-Z\\-]+").matcher(span);
      if (m.find()) {
        int begin = m.start();
        int quotes = span.indexOf("\"", begin);
        quotes = span.indexOf("\"", quotes + 1);
        span = span.substring(0, quotes + 1) + " " + span.substring(quotes + 1);
      } else {
        break;
      }
    }
    return span;
  }

  public static Date parsePublishedDate(String date) throws ParseException {
    String modified = date.substring(0, date.lastIndexOf(":")) + date.substring(date.lastIndexOf(":") + 1);
    SimpleDateFormat publishedTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    return publishedTime.parse(modified);
  }

  public static String formatPublishedDate(Date date) throws ParseException {
    SimpleDateFormat publishedTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    String retval = publishedTime.format(date);
    return retval.substring(0, retval.length() - 2) + ":" + retval.substring(retval.length() - 2);
  }

  public static <T> String xml(T object) {
    String retval = null;
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
      Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
      StringWriter sw = new StringWriter();
      jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      jaxbMarshaller.marshal(object, sw);
      retval = sw.toString();
    } catch (JAXBException ex) {
    } finally {
      return retval;
    }
  }

  private static Logger logger;

  public static String getApplicationProperty(String name) {
    File propertiesFile = new File("./application.properties");
    String userFile = propertiesFile.toURI().toString();
    URL url = null;
    Properties props = new Properties();
    String value = null;
    try {
      if (KEYS.isEmpty()) {
        logger.info("**************************************************************");
        logger.info(String.format("application.properties filename %s", propertiesFile.getCanonicalPath()));
        logger.info("**************************************************************");
        url = new URL(userFile);
        props.load(url.openStream());
        props.entrySet().stream().forEach(kv
          -> {
          KEYS.put((String) kv.getKey(), (String) kv.getValue());
        });
        value = KEYS.get(name);
      } else {
        value = KEYS.get(name);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      return value;
    }
  }

  public static <T> Object objectify(String xml, T object) {
    Object retval = null;
    try {
      xml = Utilities.insertSpace(xml);
      JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
      Unmarshaller jaxbMarshaller = jaxbContext.createUnmarshaller();
      StringWriter sw = new StringWriter();
      retval = jaxbMarshaller.unmarshal(new InputStreamReader(new ByteArrayInputStream(xml.getBytes())));
    } catch (JAXBException ex) {
      logger.log(Level.SEVERE, null, ex);
    } finally {
      return retval;
    }
  }

  private static String[] DATE_USA_LOCALE_PATTERNS = {
    "yyyy.MM.dd G 'at' HH:mm:ss z",
    "EEE, MMM d, ''yy",
    "h:mm a",
    "hh 'o''clock' a, zzzz",
    "K:mm a, z",
    "yyyyy.MMMMM.dd GGG hh:mm aaa",
    "EEE, d MMM yyyy HH:mm:ss Z",
    "yyMMddHHmmssZ",
    "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
    "yyyy-MM-dd'T'HH:mm-ssZ",
    "yyyy-MM-dd'T'HH:mm:ssXXX",
    "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
    "YYYY-'W'ww-u"};

  public static String normalizeNumbers(String value, String[] excludes) {
    value = Utilities.normalizeDecimal(value, excludes);
    value = Utilities.normalizeInteger(value, excludes);
    return value;
  }

  private static String normalizeInteger(String value, String[] excludes) {

    Matcher m = Pattern.compile("\"[\\-]{0,1}[0-9]{1,}\"").matcher(value);
    int start = 0;
    while (m.find()) {
      start = m.start();
      String str = m.group();
      Matcher r = Pattern.compile("[\\-]{0,1}[0-9]{1,}").matcher(str);
      if (r.find()) {
        boolean flag = false;
        for (String exclude : excludes) {
          int back = start - 2 - exclude.length();
          if (back > 0 && value.length() > back && value.substring(back).startsWith(exclude)) {
            flag = true;
            break;
          }
        }
        if (flag == false) {
          value = value.replaceAll(str, r.group());
        }

      }
    }
    return value;
  }

  private static String normalizeDecimal(String value, String[] excludes) {
    Matcher m = Pattern.compile("\"[\\-]{0,1}[0-9]{1,}\\.[0-9]{2}\"").matcher(value);
    int start = 0;
    while (m.find()) {
      start = m.start();
      String str = m.group();
      Matcher r = Pattern.compile("[\\-]{0,1}[0-9]{1,}\\.[0-9]{2}").matcher(str);
      if (r.find()) {
        boolean flag = false;
        for (String exclude : excludes) {
          int back = start - 2 - exclude.length();
          if (back > 0 && value.length() > back && value.substring(back).startsWith(exclude)) {
            flag = true;
            break;
          }
        }
        if (flag == false) {
          value = value.replaceAll(str, r.group());
        }
      }
    }
    return value;
  }

  public static void waitForStatus(Document vendor) {
    while (true) {
      if (!vendor.containsKey(Constants.Status)) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException ex) {
          logger.log(Level.SEVERE, null, ex);
        }
        System.out.println("waiting status " + vendor.getString(Constants.Sku));
      } else {
        break;
      }
    }
  }
  
  public static boolean isMoreThanFourHoursAgo(Calendar then) {
    if (then == null) return true;
    Calendar now = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
    now.add(Calendar.HOUR_OF_DAY, -4);
    return then.before(now);
  }
}
