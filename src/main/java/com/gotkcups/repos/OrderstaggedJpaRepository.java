/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.repos;

import com.gotkcups.model.Orderstagged;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author rfteves
 */
public interface OrderstaggedJpaRepository extends JpaRepository<Orderstagged, Long> {

  @Query("SELECT o FROM Orderstagged o WHERE o.marketordernumber = :marketordernumber")
  public List<Orderstagged> findByMarketordernumber(@Param("marketordernumber") String marketordernumber);
}