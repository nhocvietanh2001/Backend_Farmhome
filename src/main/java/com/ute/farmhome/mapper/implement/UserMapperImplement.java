package com.ute.farmhome.mapper.implement;

import com.ute.farmhome.dto.UserCreateDTO;
import com.ute.farmhome.dto.UserShowDTO;
import com.ute.farmhome.entity.Role;
import com.ute.farmhome.entity.StatusUser;
import com.ute.farmhome.entity.User;
import com.ute.farmhome.exception.ResourceNotFound;
import com.ute.farmhome.mapper.LocationMapper;
import com.ute.farmhome.mapper.UserMapper;
import com.ute.farmhome.repository.LocationRepository;
import com.ute.farmhome.repository.RoleRepository;
import com.ute.farmhome.repository.StatusUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;

@Component
public class UserMapperImplement implements UserMapper {
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private StatusUserRepository statusUserRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private LocationMapper locationMapper;

    @Override
    public UserCreateDTO map(User user) {
        return null;
    }

    @Override
    public User map(UserCreateDTO userCreateDTO) {
        User user = new User();
        user.setAvatar(userCreateDTO.getAvatar());
        user.setId(userCreateDTO.getId());
        user.setUsername(userCreateDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userCreateDTO.getPassword()));
        user.setFirstName(userCreateDTO.getFirstName());
        user.setLastName(userCreateDTO.getLastName());
        user.setBirthDay(LocalDate.parse(userCreateDTO.getBirthDay()));
        user.setEmail(userCreateDTO.getEmail());
        user.setPhone(userCreateDTO.getPhone());
        user.setDescription(userCreateDTO.getDescription());
        //user.setLocation(locationMapper.map(userCreateDTO.getLocation()));
        user.setCreateDate(LocalDate.now());
//        StatusUser statusUser = statusUserRepository.findById(userCreateDTO.getStatus().getId())
//                .orElseThrow(() -> new ResourceNotFound("StatusUser", "id", String.valueOf(userCreateDTO.getStatus().getId())));
//        user.setStatus(statusUser);
        userCreateDTO.getRoles().forEach(role -> {
            Role findRole = roleRepository.findById(role.getId())
                    .orElseThrow(() -> new ResourceNotFound("Role", "id", String.valueOf(role.getId())));
            user.getRoles().add(findRole);
        });
        return user;
    }

    @Override
    public UserShowDTO mapToShow(User user) {
        if (user == null) {
            return null;
        }
        UserShowDTO dto = new UserShowDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setAvatar(user.getAvatar());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setBirthDay(user.getBirthDay());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setDescription(user.getDescription());
        dto.setLocation(user.getLocation());
        dto.setCreateDate(user.getCreateDate());
        dto.setStatus(user.getStatus());
        dto.setSuccess("true");
        dto.setMessage("");
        return dto;
    }

}
