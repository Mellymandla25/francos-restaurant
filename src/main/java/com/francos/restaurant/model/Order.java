/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.francos.restaurant.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    private String orderNumber;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String notes;
    
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_number")
    private List<CartItem> items;
    
    private double totalPrice;
    private LocalDateTime orderTime;
    private String status;
    private LocalDateTime statusChangeTime;

    @Entity
    @Table(name = "order_items")
    public static class CartItem {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String name;
        private double price;

        public CartItem() {}
        public CartItem(String name, double price) {
            this.name = name;
            this.price = price;
        }

        public String getName() { return name; }
        public double getPrice() { return price; }
    }

    public Order(String orderNumber1, String fullName1, String phoneNumber1, String collectionTime, String email1, String notes1, List<CartItem> orderItems, double totalPrice1) {}
    public Order(String orderNumber, String fullName, String phoneNumber, String email, 
                 String notes, List<CartItem> items, double totalPrice) {
        this.orderNumber = orderNumber;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.notes = notes;
        this.items = items;
        this.collectionTime = collectionTime;
        this.totalPrice = totalPrice;
        this.orderTime = LocalDateTime.now();
        this.status = "PENDING";
        this.statusChangeTime = LocalDateTime.now();
    }

    // Getters and Setters
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public LocalDateTime getOrderTime() { return orderTime; }
    public void setOrderTime(LocalDateTime orderTime) { this.orderTime = orderTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { 
        this.status = status; 
        this.statusChangeTime = LocalDateTime.now(); 
    }

    public LocalDateTime getStatusChangeTime() { return statusChangeTime; }
    public void setStatusChangeTime(LocalDateTime statusChangeTime) { this.statusChangeTime = statusChangeTime; }

    public String getFormattedOrderTime() {
        java.time.format.DateTimeFormatter formatter = 
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return orderTime.format(formatter);
    }
    
    private String collectionTime;

    public String getCollectionTime() {
        return collectionTime;
    }

    public void setCollectionTime(String collectionTime) {
        this.collectionTime = collectionTime;
    }
}