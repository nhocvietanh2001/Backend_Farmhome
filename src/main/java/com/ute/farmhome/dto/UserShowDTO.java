package com.ute.farmhome.dto;

import com.ute.farmhome.entity.Location;
import com.ute.farmhome.entity.Role;
import com.ute.farmhome.entity.StatusUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserShowDTO {
    private int id;
    private String username;
    private String avatar;
    private String firstName;
    private String lastName;
    private LocalDate birthDay;
    private String email;
    private String phone;
    private String description;
    private Location location;
    private LocalDate createDate;
    private StatusUser status;
    String success;
    String message;
}
