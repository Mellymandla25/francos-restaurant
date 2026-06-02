/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.francos.restaurant.repository;

import com.francos.restaurant.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    
    // Find all orders by status
    List<Order> findByStatus(String status);
    
    // Find orders between two dates
    List<Order> findByOrderTimeBetween(LocalDateTime start, LocalDateTime end);
    
    // Get total sales between two dates
    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.orderTime BETWEEN :start AND :end")
    Double getTotalSalesBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // Get count of orders between two dates
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderTime BETWEEN :start AND :end")
    Integer getOrderCountBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // Get total sales for all time
    @Query("SELECT SUM(o.totalPrice) FROM Order o")
    Double getTotalSalesAllTime();
    
    // Get total count for all time
    @Query("SELECT COUNT(o) FROM Order o")
    Integer getTotalCountAllTime();
}
