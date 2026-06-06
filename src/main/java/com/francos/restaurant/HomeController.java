/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.francos.restaurant;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.francos.restaurant.repository.OrderRepository;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

import com.francos.restaurant.model.Order;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


@Controller
public class HomeController {
    
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private OrderRepository orderRepository;

    // Cart data
    private static List<Order.CartItem> cartItems = new ArrayList<>();
    private static double totalPrice = 0.0;

    // Order storage (in-memory - for demo only)
    private static List<Order> allOrders = new ArrayList<>();

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/menu")
    public String menu(Model model) {
        model.addAttribute("cartCount", cartItems.size());
        return "menu";
    }

    @PostMapping("/addToCart")
    public String addToCart(@RequestParam String itemName, @RequestParam String itemPrice) {
        double price = Double.parseDouble(itemPrice);
        cartItems.add(new Order.CartItem(itemName, price));
        totalPrice += price;
        return "redirect:/menu";
    }

    @GetMapping("/specials")
    public String specials(Model model) {
        model.addAttribute("cartCount", cartItems.size());
        return "specials";
    }

    @GetMapping("/cart")
    public String cart(Model model) {
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("cartCount", cartItems.size());
        return "cart";
    }

    @GetMapping("/checkout")
    public String checkout(Model model) {
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("cartCount", cartItems.size());
        return "checkout";
    }

    @PostMapping("/removeFromCart")
    public String removeFromCart(@RequestParam int index) {
        if (index >= 0 && index < cartItems.size()) {
            Order.CartItem removed = cartItems.remove(index);
            totalPrice -= removed.getPrice();
        }
        return "redirect:/cart";
    }

    @GetMapping("/clearCart")
    public String clearCart() {
        cartItems.clear();
        totalPrice = 0.0;
        return "redirect:/menu";
    }

    @PostMapping("/placeOrder")
    public String placeOrder(
        @RequestParam String fullName,
        @RequestParam String phoneNumber,
        @RequestParam(required = false) String email,
        @RequestParam(required = false) String notes,
        @RequestParam String collectionTime,
        Model model) 
    {
        // Generate order number
        String orderNumber = "FR-" + System.currentTimeMillis() % 1000000;
        
        // Create a copy of cart items for the order
        List<Order.CartItem> orderItems = new ArrayList<>();
        for (Order.CartItem item : cartItems) {
            orderItems.add(new Order.CartItem(item.getName(), item.getPrice()));
        }
        
        // Create and save the order
        Order newOrder = new Order(orderNumber, fullName, phoneNumber, email, notes, orderItems, totalPrice);
        allOrders.add(newOrder);
        // ====== SEND INSTANT STAFF ALERT ======
        try {
            SimpleMailMessage staffAlert = new SimpleMailMessage();
            staffAlert.setTo("restaurant@francos.co.za"); // ← Change to restaurant email
            staffAlert.setSubject("🚨 NEW ORDER: " + orderNumber);
            staffAlert.setText(
                "New order received!\n\n" +
                "Order Number: " + orderNumber + "\n" +
                "Customer: " + fullName + "\n" +
                "Phone: " + phoneNumber + "\n" +
                "Collection Time: " + collectionTime + "\n" +
                "Items: " + cartItems.size() + " items\n" +
                "Total: R" + totalPrice + "\n" +
                (notes != null && !notes.isEmpty() ? "Notes: " + notes : "")
            );
            mailSender.send(staffAlert);
            System.out.println("✅ Staff alert sent");
        } catch (Exception e) {
            System.out.println("❌ Staff alert failed: " + e.getMessage());
}
        // Clear cart
        cartItems.clear();
        totalPrice = 0.0;
        
        model.addAttribute("orderNumber", orderNumber);
        model.addAttribute("cartCount", 0);
        
        return "order-confirmation";
    }

    @GetMapping("/order-confirmation")
    public String orderConfirmation(Model model) {
        model.addAttribute("cartCount", 0);
        return "order-confirmation";
    }

    // ====== NEW: ORDER MANAGEMENT FOR RESTAURANT ======
    
    @GetMapping("/orders")
    public String viewOrders(Model model) {
        cleanOldOrders();
        model.addAttribute("orders", allOrders);
        model.addAttribute("cartCount", cartItems.size());
        return "orders";
    }

    @PostMapping("/update-order-status")
    public String updateOrderStatus(@RequestParam String orderNumber, @RequestParam String status) {
        for (Order order : allOrders) {
            if (order.getOrderNumber().equals(orderNumber)) {
                order.setStatus(status);
                break;
            }
        }
        return "redirect:/orders";
    }
    @GetMapping("/order-items")
    public String orderItems(@RequestParam String orderNumber, Model model) {
        for (Order order : allOrders) {
            if (order.getOrderNumber().equals(orderNumber)) {
                model.addAttribute("order", order);
                model.addAttribute("cartCount", cartItems.size());
                return "order-items";
            }
        }
        return "redirect:/orders";
    }
    private void cleanOldOrders(){
        LocalDateTime now = LocalDateTime.now();
        List<Order> ordersToRemove = new ArrayList<>();
        
        for (Order order : allOrders){
            if(order.getStatus().equals("COLLECTED") || order.getStatus().equals("CANCELLED")){
                LocalDateTime statusTime= order.getStatusChangeTime();
                if (statusTime.plusMinutes(5).isBefore(now)){
                    ordersToRemove.add(order);
                }
            }
        }
        allOrders.removeAll(ordersToRemove);
        if(!ordersToRemove.isEmpty()){
            System.out.println("Cleaned "+ ordersToRemove.size()+ " orders from display");
        }
    }
    @PostMapping
    public String cleanOrders(){
        cleanOldOrders();
        return "redirect:/orders";
    }
    
    @GetMapping("/view-order")
    public String viewOrder(@RequestParam String orderNumber, Model model) {
        for (Order order : allOrders) {
            if (order.getOrderNumber().equals(orderNumber)) {
                model.addAttribute("order", order);
                model.addAttribute("cartCount", cartItems.size());
                return "my-order-details";
            }
        }
        return "redirect:/";
    }

    @GetMapping("/staff-sales")
    public String dashboard(@RequestParam(required = false) String pass, Model model) {
        
        if (pass == null || !pass.equals("francos123")) {
            return "redirect:/";
        }
        
        LocalDateTime now = LocalDateTime.now();
    
        // Daily (today from midnight)
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        Double dailySales = orderRepository.getTotalSalesBetween(startOfDay, now);
        Integer dailyOrders = orderRepository.getOrderCountBetween(startOfDay, now);
    
        // Weekly (last 7 days)
        LocalDateTime startOfWeek = now.minusDays(7);
        Double weeklySales = orderRepository.getTotalSalesBetween(startOfWeek, now);
        Integer weeklyOrders = orderRepository.getOrderCountBetween(startOfWeek, now);
    
        // Monthly (last 30 days)
        LocalDateTime startOfMonth = now.minusDays(30);
        Double monthlySales = orderRepository.getTotalSalesBetween(startOfMonth, now);
        Integer monthlyOrders = orderRepository.getOrderCountBetween(startOfMonth, now);
    
        // Total (all time)
        Double totalSales = orderRepository.getTotalSalesAllTime();
        Integer totalOrders = orderRepository.getTotalCountAllTime();
    
        // Handle null values
        dailySales = dailySales != null ? dailySales : 0.0;
        weeklySales = weeklySales != null ? weeklySales : 0.0;
        monthlySales = monthlySales != null ? monthlySales : 0.0;
        totalSales = totalSales != null ? totalSales : 0.0;
        dailyOrders = dailyOrders != null ? dailyOrders : 0;
        weeklyOrders = weeklyOrders != null ? weeklyOrders : 0;
        monthlyOrders = monthlyOrders != null ? monthlyOrders : 0;
        totalOrders = totalOrders != null ? totalOrders : 0;
        
        
        model.addAttribute("dailySales", dailySales);
        model.addAttribute("weeklySales", weeklySales);
        model.addAttribute("monthlySales", monthlySales);
        model.addAttribute("totalSales", totalSales);
        model.addAttribute("dailyOrders", dailyOrders);
        model.addAttribute("weeklyOrders", weeklyOrders);
        model.addAttribute("monthlyOrders", monthlyOrders);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("cartCount", cartItems.size());
    
        return "dashboard";
    }
    
    @GetMapping("/my-orders")
    public String myOrders(@RequestParam(required = false) String phone, Model model) {
        List<Order> customerOrders = new ArrayList<>();
    
        if (phone != null && !phone.isEmpty()) {
            for (Order order : allOrders) {
                if (order.getPhoneNumber().equals(phone)) {
                    customerOrders.add(order);
                }
            }
        }
    
        model.addAttribute("orders", customerOrders);
        model.addAttribute("cartCount", cartItems.size());
    
        // ← CHANGE THIS FROM "my-orders" TO "my-order-details"
        return "my-order-details";
    }
    
    @GetMapping("/export-orders")
    public void exportOrders(HttpServletResponse response) {
        try {
            // Create Excel workbook
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Orders");

            // Create header row
            Row header = sheet.createRow(0);
            String[] columns = {"Order Number", "Customer", "Phone", "Total", "Date", "Status"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
            }

            // Add order data
            int rowNum = 1;
            for (Order order : allOrders) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(order.getOrderNumber());
                row.createCell(1).setCellValue(order.getFullName());
                row.createCell(2).setCellValue(order.getPhoneNumber());
                row.createCell(3).setCellValue(order.getTotalPrice());
                row.createCell(4).setCellValue(order.getFormattedOrderTime());
                row.createCell(5).setCellValue(order.getStatus());
            }

            // Set response headers
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=orders.xlsx");

            // Write to response
            workbook.write(response.getOutputStream());
            workbook.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}