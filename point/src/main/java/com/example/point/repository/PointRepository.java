package com.example.point.repository;

import com.example.point.domain.entity.Point;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointRepository extends JpaRepository<Point, Long> {

    Optional<Point> findByUserId(Long userId);
}
