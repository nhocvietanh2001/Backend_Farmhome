package com.ute.farmhome.dto;

import com.ute.farmhome.entity.Category;
import com.ute.farmhome.entity.FruitImage;
import com.ute.farmhome.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class FruitShowDTO {
    int id;
    String name;
    float weight;
    float remainingWeight;
    String unit;
    List<FruitImage> images = new ArrayList<>();
    String description;
    LocalDate date;
    UserShowDTO farmer;
    Boolean popular;
    String season;
    String suggestPrice;
    String category;
    String success;
    String message;
}
