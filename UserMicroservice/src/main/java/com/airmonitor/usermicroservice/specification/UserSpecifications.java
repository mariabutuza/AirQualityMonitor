package com.airmonitor.usermicroservice.specification;

import com.airmonitor.usermicroservice.entity.User;
import com.airmonitor.usermicroservice.entity.Role;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserSpecifications {

    public static Specification<User> withFilters(
            String search,
            String role,
            String emailDomain,
            LocalDateTime from,
            LocalDateTime to
    ) {
        return (Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String likePattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("fullName")), likePattern),
                        cb.like(cb.lower(root.get("email")), likePattern),
                        cb.like(cb.lower(root.get("role").as(String.class)), likePattern)
                ));
            }

            if (role != null && !role.isBlank() && !role.equalsIgnoreCase("ALL")) {
                try {
                    Role parsedRole = Role.valueOf(role.toUpperCase());
                    predicates.add(cb.equal(root.get("role"), parsedRole));
                } catch (IllegalArgumentException ignored) {}
            }

            if (emailDomain != null && !emailDomain.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("email")), "%" + emailDomain.toLowerCase()));
            }

            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
