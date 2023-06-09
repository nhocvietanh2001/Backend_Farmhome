package com.ute.farmhome.repository;

import com.ute.farmhome.dto.UserShowDTO;
import com.ute.farmhome.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {
    @Query("SELECT u FROM User u WHERE u.username = :username")
    Optional<User> findByUsername(String username);
    @Query("SELECT u FROM User u")
    Page<User> findAllUserPaging(Pageable pageable);
    @Query("SELECT case WHEN count(u)> 0 then true else false end FROM User u WHERE u.username = :username")
    boolean existByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = 'MERCHANT'")
    Page<User> getAllMerchant(Pageable pageable);
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = 'FARMER'")
    Page<User> getAllFarmer(Pageable pageable);
    @Query("SELECT u FROM User u JOIN u.roles r WHERE u.username LIKE %:username% AND r.name = 'MERCHANT'")
    Page<User> searchMerchantContaining(String username, Pageable pageable);
    @Query("SELECT u FROM User u JOIN u.roles r WHERE u.username LIKE %:username% AND r.name = 'FARMER'")
    Page<User> searchFarmerContaining(String username, Pageable pageable);
    @Query("SELECT u.firstName, u.lastName, sum(h.price * h.amount) FROM User u, History h WHERE u.id = h.merchant GROUP BY u.id")
    List<Object[]> statisticMerchant();
    @Query("SELECT u.firstName, u.lastName, sum(h.price * h.amount) FROM User u, History h WHERE u.id = h.farmer GROUP BY u.id")
    List<Object[]> statisticFarmer();
    @Query("SELECT count(r.id) FROM User u JOIN u.roles r GROUP BY r.id ORDER BY r.id")
    Object[] getCountUser();
    @Query("SELECT count(u.username) FROM User u WHERE MONTH(u.createDate) = :month")
    Long getNewUserThisMonth(int month);
}
