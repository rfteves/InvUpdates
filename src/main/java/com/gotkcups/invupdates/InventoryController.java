/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.invupdates;

import com.gotkcups.data.Constants;
import com.gotkcups.data.MongoDBJDBC;
import com.gotkcups.data.RequestsHandler;
import com.gotkcups.io.Utilities;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicLong;
import javax.servlet.http.HttpServletRequest;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 *
 * @author ricardo
 */
@RestController
@RequestMapping("/")
public class InventoryController {
  private final static Logger log = LoggerFactory.getLogger(InventoryController.class);
  /*@RequestMapping(method = GET)
  public List<Object> list() {
    return null;
  }
  
  @RequestMapping(value = "/{id}", method = GET)
  public Object get(@PathVariable String id) {
    return null;
  }
  
  @RequestMapping(value = "/{id}", method = PUT)
  public ResponseEntity<?> put(@PathVariable String id, @RequestBody Object input) {
    return null;
  }
  
  @RequestMapping(value = "/{id}", method = POST)
  public ResponseEntity<?> post(@PathVariable String id, @RequestBody Object input) {
    return null;
  }
  
  @RequestMapping(value = "/{id}", method = DELETE)
  public ResponseEntity<Object> delete(@PathVariable String id) {
    return null;
  }*/
  @RequestMapping("/hello")
  public Hello greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
    return new Hello(counter.incrementAndGet(),
      String.format(template, name));
  }
  private static final String template = "Hello, %s!";
  private final AtomicLong counter = new AtomicLong();

  @RequestMapping("/{id}.product")
  public Document update(@PathVariable Long id) {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
      .getRequest();
    Document product = new Document();
    Document _id = new Document();
    _id.put(Constants.Product_Id, id);
    _id.put(Constants.Remote_Host, request.getRemoteHost());
    product.put(Constants._Id, _id);
    Calendar lastUpdate = MongoDBJDBC.getProductLastUpdate(product);
    MongoDBJDBC.updateProductIP(product);
    if (Utilities.isMoreThanFourHoursAgo(lastUpdate)) {
      RequestsHandler.register(id);
    }
    return product;
  }

  @Bean
  public FilterRegistrationBean corsFilter() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.addAllowedOrigin("*");
    config.addAllowedHeader("*");
    config.addAllowedMethod("OPTIONS");
    config.addAllowedMethod("HEAD");
    config.addAllowedMethod("GET");
    config.addAllowedMethod("PUT");
    config.addAllowedMethod("POST");
    config.addAllowedMethod("DELETE");
    config.addAllowedMethod("PATCH");
    source.registerCorsConfiguration("/**", config);
    // return new CorsFilter(source);
    final FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
    bean.setOrder(0);
    return bean;
  }

  static {
    System.getProperties().setProperty("mail.smtp.host", Utilities.getApplicationProperty("mail.smtp.host"));
    System.getProperties().setProperty("mail.username", Utilities.getApplicationProperty("mail.username"));
    System.getProperties().setProperty("mail.password", Utilities.getApplicationProperty("mail.password"));
    System.getProperties().setProperty("mail.smtp.port", Utilities.getApplicationProperty("mail.smtp.port"));
    System.getProperties().put("mail.smtp.auth", "true");
    System.getProperties().put("mail.smtp.starttls.enable", "true");
  }
}
