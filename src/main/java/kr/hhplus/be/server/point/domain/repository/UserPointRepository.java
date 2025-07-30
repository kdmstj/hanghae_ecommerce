package kr.hhplus.be.server.point.domain.repository;

import kr.hhplus.be.server.point.domain.entity.UserPoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPointRepository extends JpaRepository<UserPoint, Long> {
    Optional<UserPoint> findOneByUserId(long userId);
}
