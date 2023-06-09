package com.ute.farmhome.mapper.implement;

import com.ute.farmhome.dto.FruitDTO;
import com.ute.farmhome.dto.FruitShowDTO;
import com.ute.farmhome.entity.Fruit;
import com.ute.farmhome.exception.ResourceNotFound;
import com.ute.farmhome.mapper.FruitMapper;
import com.ute.farmhome.mapper.UserMapper;
import com.ute.farmhome.repository.CategoryRepository;
import com.ute.farmhome.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class FruitMapperImplement implements FruitMapper {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CategoryRepository categoryRepository;
    @Override
    public Fruit map(FruitDTO dto) {
        Fruit fruit = new Fruit();
        fruit.setId(dto.getId());
        fruit.setName(dto.getName());
        fruit.setWeight(dto.getWeight());
        fruit.setRemainingWeight(dto.getRemainingWeight());
        fruit.setUnit(dto.getUnit());
        fruit.setImages(dto.getImages());
        fruit.setDate(LocalDate.parse(dto.getDate()));
        fruit.setDescription(dto.getDescription());
        fruit.setFarmer(userRepository.findById(dto.getFarmer().getId())
                .orElseThrow(() -> new ResourceNotFound("User", "id", String.valueOf(dto.getFarmer().getId()))));
        fruit.setSeason(dto.getSeason());
        fruit.setPopular(dto.getPopular());
        //default value if request does not put category is 1 - "trái cây"
        fruit.setCategory(dto.getCategory() != null
                ? categoryRepository.findByCategory(dto.getCategory())
                : categoryRepository.findById(1)
                    .orElseThrow(() -> new ResourceNotFound("category", "id", String.valueOf(1))));
        return fruit;
    }

    @Override
    public FruitDTO map(Fruit fruit) {
        FruitDTO dto = new FruitDTO();
        dto.setId(fruit.getId());
        dto.setName(fruit.getName());
        dto.setWeight(fruit.getWeight());
        dto.setRemainingWeight(fruit.getRemainingWeight());
        dto.setUnit(fruit.getUnit());
        dto.setImages(fruit.getImages());
        dto.setDate(String.valueOf(fruit.getDate()));
        dto.setDescription(fruit.getDescription());
        dto.setFarmer(userMapper.mapToShow(fruit.getFarmer()));
        dto.setSeason(fruit.getSeason());
        dto.setPopular(fruit.getPopular());
        dto.setCategory(fruit.getCategory() != null ? fruit.getCategory().getCategory() : null);
        return dto;
    }

    @Override
    public FruitShowDTO mapToShow(Fruit fruit) {
        FruitShowDTO dto = new FruitShowDTO();
        dto.setId(fruit.getId());
        dto.setName(fruit.getName());
        dto.setWeight(fruit.getWeight());
        dto.setUnit(fruit.getUnit());
        dto.setRemainingWeight(fruit.getRemainingWeight());
        dto.setImages(fruit.getImages());
        dto.setDate(fruit.getDate());
        dto.setFarmer(userMapper.mapToShow(fruit.getFarmer()));
        dto.setDescription(fruit.getDescription());
        dto.setSeason(fruit.getSeason());
        dto.setPopular(fruit.getPopular());
        dto.setCategory(fruit.getCategory() != null ? fruit.getCategory().getCategory() : null);
        dto.setSuccess("true");
        dto.setMessage("");
        return dto;
    }
}
