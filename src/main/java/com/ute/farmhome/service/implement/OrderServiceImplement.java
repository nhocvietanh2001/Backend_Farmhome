package com.ute.farmhome.service.implement;

import com.ute.farmhome.dto.HistoryDTO;
import com.ute.farmhome.dto.NotificationNote;
import com.ute.farmhome.dto.OrderDTO;
import com.ute.farmhome.dto.PaginationDTO;
import com.ute.farmhome.entity.*;
import com.ute.farmhome.exception.ExceedAmount;
import com.ute.farmhome.exception.ResourceNotFound;
import com.ute.farmhome.mapper.LocationMapper;
import com.ute.farmhome.mapper.OrderMapper;
import com.ute.farmhome.repository.OrderRepository;
import com.ute.farmhome.service.*;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderServiceImplement implements OrderService {
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    FruitService fruitService;
    @Autowired
    HistoryService historyService;
    @Autowired
    UserService userService;
    @Autowired
    UserLoginService userLoginService;
    @Autowired
    StatusService statusService;
    @Autowired
    LocationService locationService;
    @Autowired
    FirebaseMessagingService messagingService;
    @Autowired
    NotificationHistoryService notificationHistoryService;
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    LocationMapper locationMapper;
    @Override
    public OrderDTO createOrder(OrderDTO orderDTO) {
        Order order = orderMapper.map(orderDTO);
        Fruit fruit = fruitService.findFruitById(orderDTO.getFruit().getId());
        order.setFruit(fruit);
        User farmer = userService.findById(orderDTO.getFarmer().getId());
        order.setFarmer(farmer);
        User merchant = userService.findById(orderDTO.getMerchant().getId());
        order.setMerchant(merchant);
        StatusProduct statusPending = statusService.getPendingStatusProduct();
        order.setStatus(statusPending);
        if (orderDTO.getDeliveryLocation() != null) {
            if (orderDTO.getDeliveryLocation().getId() != 0) {
                Location location = locationService.findById(orderDTO.getDeliveryLocation().getId());
                order.setDeliveryLocation(location);
            }
            if (orderDTO.getDeliveryLocation().getWard() != null) {
                order.setDeliveryLocation(locationService.bindData(orderDTO.getDeliveryLocation()));

            }
        }
        return orderMapper.map(orderRepository.save(order));
    }

    @Override
    public PaginationDTO getByMerchantId(int id, int no, int limit) {
        Pageable pageable = PageRequest.of(no, limit);
        List<?> listOrder = orderRepository.findByMerchantId(id, pageable).stream().map(item -> orderMapper.map(item)).toList();
        Page<Order> page = orderRepository.findByMerchantId(id, pageable);
        return new PaginationDTO(listOrder, page.isFirst(), page.isLast(), page.getTotalPages(), page.getTotalElements(), page.getSize(), page.getNumber(),"true", "");
    }

    @Override
    public PaginationDTO getByFarmerId(int id, int no, int limit) {
        Pageable pageable = PageRequest.of(no, limit);
        List<?> listOrder = orderRepository.findByFarmerId(id, pageable).stream().map(item -> orderMapper.map(item)).toList();
        Page<Order> page = orderRepository.findByFarmerId(id, pageable);
        return new PaginationDTO(listOrder, page.isFirst(), page.isLast(), page.getTotalPages(), page.getTotalElements(), page.getSize(), page.getNumber(),"true", "");
    }

    @Override
    public OrderDTO changePrice(OrderDTO orderDTO) {
        Order order = orderRepository.findById(orderDTO.getId())
                .orElseThrow(() -> new ResourceNotFound("Order", "id", String.valueOf(orderDTO.getId())));
        Fruit fruit = fruitService.findFruitById(order.getFruit().getId());
        order.setDealPrice(orderDTO.getDealPrice());
        order.setDealAmount(orderDTO.getDealAmount());
        StatusProduct statusDealing = statusService.getDealingStatusProduct();
        order.setStatus(statusDealing);
        //save notification history
        NotificationNote notificationNote = new NotificationNote("Your order has changed price",
                "Order with the product name '" + fruit.getName() + "' has changed price!",
                fruit.getImages().get(0).getUrl(),
                "order",
                order.getId());
        notificationHistoryService.save(notificationNote, order.getMerchant());
        //notify user the price changed
        userLoginService.findByUserId(order.getMerchant().getId()).ifPresent(userLogin -> {
            try {
                messagingService.sendNotification(notificationNote,
                        userLogin.getDeviceId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return orderMapper.map(orderRepository.save(order));
    }

    @Override
    public OrderDTO resendOrder(OrderDTO orderDTO) {
        Order order = orderRepository.findById(orderDTO.getId())
                .orElseThrow(() -> new ResourceNotFound("Order", "id", String.valueOf(orderDTO.getId())));
        if(order.getDealPrice() != null) {
            order.setPrice(order.getDealPrice());
            order.setDealPrice(null);
        }
        if(order.getDealAmount() != null) {
            order.setAmount(order.getDealAmount());
            order.setDealAmount(null);
        }
        StatusProduct statusPending = statusService.getPendingStatusProduct();
        order.setStatus(statusPending);
        return orderMapper.map(orderRepository.save(order));
    }

    @Override
    public HistoryDTO acceptOrder(int id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("Order", "id", String.valueOf(id)));
        Fruit fruit = fruitService.findFruitById(order.getFruit().getId());
        if (fruit.getRemainingWeight() == fruit.getWeight()) {
            fruit.setRemainingWeight(fruit.getWeight() - order.getAmount());
            fruitService.save(fruit);
        } else {
            if (fruit.getRemainingWeight() < order.getAmount()) {
                throw new ExceedAmount();
            } else {
                fruit.setRemainingWeight(fruit.getRemainingWeight() - order.getAmount());
                fruitService.save(fruit);
            }
        }
        HistoryDTO historyDTOSaved = historyService.createHistoryFromOrder(order);
        if (historyDTOSaved != null) {
            orderRepository.deleteById(order.getId());
        }
        //save notification history
        NotificationNote notificationNote = new NotificationNote("Your order has been accepted",
                "Order with the product name '" + fruit.getName() + "' has been accepted!",
                fruit.getImages().get(0).getUrl(),
                "history",
                historyDTOSaved.getId());
        notificationHistoryService.save(notificationNote, order.getMerchant());
        //notify user order has been accepted if user login with phone and have notification registration token
        userLoginService.findByUserId(order.getMerchant().getId()).ifPresent(userLogin -> {
            try {
                messagingService.sendNotification(notificationNote,
                        userLogin.getDeviceId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return historyDTOSaved;
    }
    private void calculateRemaining(int id) {
        Fruit fruit = fruitService.findFruitById(id);
        List<Order> orders = getByFruitId(id);
        float totalWeight = 0;
        for (Order order : orders) {
            totalWeight += order.getAmount();
        }
        fruit.setRemainingWeight(fruit.getWeight() - totalWeight);
        fruitService.save(fruit);
    }
    @Override
    public OrderDTO getOrderDtoById(int id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("Order", "id", String.valueOf(id)));
        return orderMapper.map(order);
    }
    @Override
    public void deleteOrder(int id, String reason) {
        Order order = getById(id);
        Fruit fruit = fruitService.findFruitById(order.getFruit().getId());

        order.setStatus(statusService.getCanceledStatusProduct());

        NotificationNote notificationNote;
        if (reason != null) {
            notificationNote = new NotificationNote("Your order has been declined",
                    "Order with the product name '" + fruit.getName() + "' has been declined with the reason: '" + reason + "'",
                    null,
                    "order",
                    id);
            notificationHistoryService.save(notificationNote, order.getMerchant());
            order.setDeclineReason(reason);
        } else {
            notificationNote = new NotificationNote("Your order has been declined",
                    "Order with the product name '" + fruit.getName() + "' has been declined!",
                    null,
                    "order",
                    id);
            notificationHistoryService.save(notificationNote, order.getMerchant());
        }

        userLoginService.findByUserId(order.getMerchant().getId()).ifPresent(userLogin -> {
            try {
                messagingService.sendNotification(notificationNote, userLogin.getDeviceId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        orderRepository.save(order);
    }

    private Order getById(int id) {
        return orderRepository.findById(id).
                orElseThrow(() -> new ResourceNotFound("Order", "id", String.valueOf(id)));
    }

    @Override
    public List<Order> getByFruitId(int id) {
        return orderRepository.findByFruitId(id);
    }

    @Override
    public List<OrderDTO> getListByFarmerOrMerchantUserId(int id, int no, int limit) {
        Pageable pageable = PageRequest.of(no, limit);
        return orderRepository.findByFarmerOrMerchantId(id, pageable).stream().map(item -> orderMapper.map(item)).toList();
    }
}
