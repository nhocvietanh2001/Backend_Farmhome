package com.ute.farmhome.mapper.implement;

import com.ute.farmhome.dto.OrderDTO;
import com.ute.farmhome.entity.Order;
import com.ute.farmhome.entity.StatusProduct;
import com.ute.farmhome.exception.ResourceNotFound;
import com.ute.farmhome.mapper.FruitMapper;
import com.ute.farmhome.mapper.LocationMapper;
import com.ute.farmhome.mapper.OrderMapper;
import com.ute.farmhome.mapper.UserMapper;
import com.ute.farmhome.repository.StatusProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class OrderMapperImplement implements OrderMapper {
    @Autowired
    StatusProductRepository statusProductRepository;
    @Autowired
    FruitMapper fruitMapper;
    @Autowired
    UserMapper userMapper;
    @Autowired
    LocationMapper locationMapper;
    @Override
    public OrderDTO map(Order entity) {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(entity.getId());
        orderDTO.setFruit(fruitMapper.map(entity.getFruit()));
        orderDTO.setFarmer(userMapper.mapToShow(entity.getFarmer()));
        orderDTO.setMerchant(userMapper.mapToShow(entity.getMerchant()));
        orderDTO.setPrice(entity.getPrice());
        orderDTO.setDealPrice(entity.getDealPrice());
        orderDTO.setAmount(entity.getAmount());
        orderDTO.setDealAmount(entity.getDealAmount());
        orderDTO.setDate(String.valueOf(entity.getDate()));
        orderDTO.setTransport(entity.getTransport());
        orderDTO.setStatus(entity.getStatus());
        orderDTO.setDeclineReason(entity.getDeclineReason());
        if (entity.getDeliveryLocation() != null) {
            orderDTO.setDeliveryLocation(locationMapper.map(entity.getDeliveryLocation()));
        }
        orderDTO.setSuccess("true");
        orderDTO.setMessage("");
        return orderDTO;
    }

    @Override
    public Order map(OrderDTO orderDTO) {
        Order entity = new Order();
        entity.setId(orderDTO.getId());
        entity.setPrice(orderDTO.getPrice());
        entity.setDealPrice(orderDTO.getDealPrice());
        entity.setAmount(orderDTO.getAmount());
        entity.setDealAmount(orderDTO.getDealAmount());
        entity.setDate(LocalDate.now());
        entity.setTransport(orderDTO.getTransport());
        entity.setDeclineReason(orderDTO.getDeclineReason());
        return entity;
    }
}
