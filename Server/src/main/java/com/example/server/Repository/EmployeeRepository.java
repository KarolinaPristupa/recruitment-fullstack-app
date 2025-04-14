package com.example.server.Repository;
import com.example.server.Models.Employee;
import com.example.server.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    @Query("SELECT e FROM Employee e JOIN FETCH e.user")
    List<Employee> findAllWithUser();

    Optional<Employee> findByUserEmail(String email);
    Optional<Employee> findByUser(User user);
}