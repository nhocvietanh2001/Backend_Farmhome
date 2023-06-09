package com.ute.farmhome.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PaginationDTO {
    private List<?> contents;
    private boolean isFirst;
    private boolean isLast;
    private long totalPages;
    private long totalItems;
    private long sizeCurrentItems;
    private int numberOfCurrentPage;
    String success;
    String message;
}
