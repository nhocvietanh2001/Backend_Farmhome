package com.ute.farmhome.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ute.farmhome.entity.Fruit;
import com.ute.farmhome.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class HistoryDTO {
    int id;
    FruitShowDTO fruit;
    UserShowDTO farmer;
    UserShowDTO merchant;
    Float price;
    Float amount;
    String date;
    Boolean transport;
    LocationDTO deliveryLocation;
    String success;
    String message;
}
