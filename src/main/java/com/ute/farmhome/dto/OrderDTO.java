package com.ute.farmhome.dto;

import com.ute.farmhome.entity.StatusProduct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderDTO {
    int id;
    FruitDTO fruit;
    UserShowDTO farmer;
    UserShowDTO merchant;
    Float price;
    Float dealPrice;
    Float amount;
    Float dealAmount;
    String date;
    Boolean transport;
    StatusProduct status;
    LocationDTO deliveryLocation;
    String declineReason;
    String success;
    String message;
}
