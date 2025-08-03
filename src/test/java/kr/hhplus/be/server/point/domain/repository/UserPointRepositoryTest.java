package kr.hhplus.be.server.point.domain.repository;

import kr.hhplus.be.server.DataBaseCleanUp;
import kr.hhplus.be.server.point.fixture.UserPointFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
public class UserPointRepositoryTest {

    @Autowired
    private UserPointRepository userPointRepository;

    @Autowired
    private DataBaseCleanUp dataBaseCleanUp;

    @BeforeEach
    public void setUp(){
        dataBaseCleanUp.execute();
    }

    @Nested
    @DisplayName("findOneByUserId")
    class findOneByUserId {
        @Test
        @DisplayName("userId로 UserPoint 를 조회할 수 있다")
        void 조회(){
            // given
            long userId = 1L;
            userPointRepository.save(UserPointFixture.withUserIdAndBalance(userId, 10000));

            // when & then
            assertThat(userPointRepository.findOneByUserId(userId).get().getUserId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("userId 에 해당하는 UserPoint 가 없다면 null 을 반환한다.")
        void 조회_없음(){
            // when & then
            assertThat(userPointRepository.findOneByUserId(999L)).isEmpty();
        }
    }
}
