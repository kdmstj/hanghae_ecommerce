package kr.hhplus.be.server.point.domain.repository;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.point.domain.entity.UserPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserPointRepository extends JpaRepository<UserPoint, Long> {
    Optional<UserPoint> findOneByUserId(long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select up from UserPoint up where up.userId = :userId")
    Optional<UserPoint> findWithPessimisticLock(long userId);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("select up from UserPoint up where up.userId = :userId")
    Optional<UserPoint> findWithOptimisticLock(long userId);
}
