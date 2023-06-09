package com.ute.farmhome.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NewsDTO {
    private int id;
    String title;
    String content;
    String author;
    LocalDate date;
    MultipartFile imageBanner;
    MultipartFile imageContent;
    String category;
    String success;
    String message;
}
