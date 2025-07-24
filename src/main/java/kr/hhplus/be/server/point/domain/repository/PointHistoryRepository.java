package kr.hhplus.be.server.point.domain.repository;

import kr.hhplus.be.server.point.domain.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
}
