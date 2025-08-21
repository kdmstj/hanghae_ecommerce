package kr.hhplus.be.server.product.infra;

import kr.hhplus.be.server.product.application.result.SalesProductResult;
import kr.hhplus.be.server.product.domain.repository.ProductDaySalesRepository;
import kr.hhplus.be.server.product.domain.value.ProductSalesIncrement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;


@Repository
@RequiredArgsConstructor
public class ProductDaySalesRedisRepository implements ProductDaySalesRepository {

    private final StringRedisTemplate redisTemplate;

    private static final String PRODUCT_RANKING_DAY_KEY_PREFIX = "product:ranking:day:";
    private static final String PRODUCT_RANKING_AGG_3DAY_KEY_PREFIX = "product:ranking:agg3:";

    private static final Duration TTL_DAY = Duration.ofDays(4);
    private static final Duration TTL_AGG = Duration.ofMinutes(70);


    @Override
    public void incrementProductSalesQuantity(LocalDateTime orderedAt, List<ProductSalesIncrement> products) {
        String key = PRODUCT_RANKING_DAY_KEY_PREFIX + orderedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        for (ProductSalesIncrement product : products) {
            redisTemplate.opsForZSet()
                    .incrementScore(key, String.valueOf(product.productId()), product.quantity());
        }
        redisTemplate.expire(key, TTL_DAY);
    }

    @Override
    public List<SalesProductResult> findTopProducts(int limit, int day) {

        LocalDateTime now = LocalDateTime.now();

        List<String> keys = IntStream.range(0, day)
                .mapToObj(i -> PRODUCT_RANKING_DAY_KEY_PREFIX + now.minusDays(i).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .toList();

        String aggregatedKey = PRODUCT_RANKING_AGG_3DAY_KEY_PREFIX;

        String source = keys.get(0);
        List<String> others = keys.size() > 1 ? keys.subList(1, keys.size()) : List.of();
        redisTemplate.opsForZSet().unionAndStore(source, others, aggregatedKey);
        redisTemplate.expire(aggregatedKey, TTL_AGG);

        Set<ZSetOperations.TypedTuple<String>> tuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(aggregatedKey, 0, Math.max(0, limit - 1));

        if (tuples.isEmpty()) return List.of();

        return tuples.stream()
                .map(t -> new SalesProductResult(Long.parseLong(t.getValue()), t.getScore().intValue()))
                .toList();
    }

    @Override
    public List<SalesProductResult> findProductSales(LocalDate day) {

        String key = PRODUCT_RANKING_DAY_KEY_PREFIX + day.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        Set<ZSetOperations.TypedTuple<String>> tuples =
                redisTemplate.opsForZSet().rangeWithScores(key, 0, -1);

        return tuples.stream()
                .map(t -> new SalesProductResult(Long.parseLong(t.getValue()), t.getScore().intValue()))
                .toList();
    }
}
