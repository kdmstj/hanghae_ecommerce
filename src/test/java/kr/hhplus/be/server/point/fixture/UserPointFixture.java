package kr.hhplus.be.server.point.fixture;

import kr.hhplus.be.server.point.domain.entity.UserPoint;

import java.time.LocalDateTime;

public class UserPointFixture {

    public static UserPoint withBalance(int balance) {
        return UserPoint.builder()
                .id(1L)
                .userId(1L)
                .balance(balance)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static UserPoint withUserIdAndBalance(long userId, int balance){
        return UserPoint.builder()
                .userId(userId)
                .balance(balance)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
