## 상품 재고 차감
### 1. 처리 흐름
상품 재고 차감 시에 다음과 같은 흐름으로 처리 됩니다.
1. 특정 상품 ID를 기반으로 Product 엔티티를 조회합니다.
2. 재고 수량을 차감한 값을 기준으로 업데이트합니다.

 ```sql
 SELECT p FROM product p WHERE p.id=1; -- (1)
 UPDATE product SET quantity = 1, updated_at = ... WHERE id = 1; -- (2)
 ```
   
### 2. 문제
- 재고가 10개인 상품에 대해 10개의 스레드가 동시에 각각 1개씩 차감 요청을 보내는 테스트를 실행했습니다.
- 예상 결과는 재고 수량이 0이지만 실제로는 9가 되었습니다.
```
expected: 0
 but was: 9
```

### 3. 원인 분석 : Lost Update
- 동시에 두 트랜잭션이 동일한 재고 수량(10)을 조회합니다.
- 각각 차감 후 동일한 결과(9)를 덮어쓰기 때문에 한쪽의 결과가 사라지는 Lost Update 가 발생했습니다.
```sql
[스레드 A]                 [스레드 B]
  SELECT quantity=10
                             SELECT quantity=10
  UPDATE quantity=9
                             UPDATE quantity=9
```

### 4. 해결 방법 및 선택 이유

**비관적 락 사용**
- 여러 사용자가 동시에 재고 차감 요청을 보낼 가능성이 높다고 판단했습니다. 낙관적 락을 사용하는 경우 시스템 내 재시도를 하는 경우 트랜잭션을 중복으로 열어서 성능성 문제가 발생하기 때문에 적절하지 않다고 생각했습니다. 
- 또한, 사용자 재시도를 하는 경우에도 재고가 충분함에도 '재고 없음' 오류가 발생하는 상황은 UX 관점에서 부정적이라고 판단했습니다. 
- 이에 비관적 락을 사용하여 확실하게 재고 차감을 보장하는 것이 좋을 것이라고 판단했습니다.

### 5. 실험 결과

**1. `PESSIMISTIC_WRITE(배타락)` 사용**

```sql
public void decreaseQuantity(List<OrderProductCommand> commands) {
	for (OrderProductCommand command : commands) {
		Product product = findForUpdate(command.productId()); // -- 배타락 적용
		product.decreaseQuantity(command.quantity());
	}
}

private Product findWithPessimisticLock(long id){
	return productRepository.findWithPessimisticLock(id)
		.orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
}

public interface ProductRepository extends JpaRepository<Product, Long> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select p from Product p where p.id = :id")
	Optional<Product> findfindWithPessimisticLock(long id);
}
```

```sql
SELECT ... FROM product WHERE id=1 FOR UPDATE;
UPDATE product SET quantity=9 WHERE id=1;
```

- 요청이 순차적으로 처리되며 정확하게 수량이 차감되었습니다.

```sql
[스레드 A]                                    [스레드 B]
SELECT quantity=10 FOR UPDATE - X락/S락 보유
UPDATE quantity=9 - X락/S락 해제
                                             SELECT quantity=9 - X락/S락 보유
                                             UPDATE quantity=8 - X락/S락 해제
```

**2. `PESSIMISTIC_READ(공유락)` 사용**

```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findWithPessimisticLock(long id);
}
```

```
select p1_0.id,p1_0.created_at,p1_0.price_per_unit,p1_0.product_name,p1_0.quantity,p1_0.updated_at from product p1_0 where p1_0.id=1 for share;
select p1_0.id,p1_0.created_at,p1_0.price_per_unit,p1_0.product_name,p1_0.quantity,p1_0.updated_at from product p1_0 where p1_0.id=1 for share;
select p1_0.id,p1_0.created_at,p1_0.price_per_unit,p1_0.product_name,p1_0.quantity,p1_0.updated_at from product p1_0 where p1_0.id=1 for share;
select p1_0.id,p1_0.created_at,p1_0.price_per_unit,p1_0.product_name,p1_0.quantity,p1_0.updated_at from product p1_0 where p1_0.id=1 for share;
select p1_0.id,p1_0.created_at,p1_0.price_per_unit,p1_0.product_name,p1_0.quantity,p1_0.updated_at from product p1_0 where p1_0.id=1 for share;

update product set created_at='2025-08-07T01:42:20.000+0000',price_per_unit=1000,product_name='상품1',quantity=9,updated_at='2025-08-07T01:42:20.094+0000' where id=1;
update product set created_at='2025-08-07T01:42:20.000+0000',price_per_unit=1000,product_name='상품1',quantity=9,updated_at='2025-08-07T01:42:20.094+0000' where id=1;
2025-08-07T01:42:20.139Z ERROR 19120 --- [pool-2-thread-4] o.h.engine.jdbc.spi.SqlExceptionHelper   : Deadlock found when trying to get lock; try restarting transaction

```

- 여러 스레드가 동시에 공유락을 획득한 뒤 update 를 시도하면서 **DeadLock(교착 상태)** 발생하였습니다.
    - 모든 스레드가 동시에 `S락(공유락)`을 획득하였고, 이후 각각 UPDATE 수행 시 `X락(배타락)`필요합니다.
    - 하지만 상대 스레드가 `S락`을 보유중이기 때문에 `X락`을 획득하지 못하고 결국 서로가 서로의 락 해제를 기다리며 데드락 발생
- 반면 배타락은?
    - `X락`은 한 번에 하나의 트랜잭션만 획득 가능합니다.
    - 다른 트랜잭션은 대기 상태로 들어가기 때문에 교착 상태가 발생하지 않습니다.

---
## 유저 포인트 충전
### 1. 배경

유저가 포인트를 충전할 때 다음과 같은 절차로 처리됩니다.
1. 특정 포인트 ID를 기반으로 `UserPoint` 엔티티를 조회합니다.
2. 기존 포인트에 더한 값을 기준으로 업데이트합니다.
```sql
SELECT up FROM user_point up WHERE up.user_id=1; -- (1)
UPDATE user_point SET balance=10000 .. WHERE id=1; -- (2)
```

### 2. 문제
- 포인트가 0원인 잔액에 대해 10개의 스레드가 동시에 10000원씩 충전 요청을 보내는 테스트를 실행했습니다.
- 예상 결과는 100000원이지만 실제 결과는 10000원이 되어 Lost Update 가 발생했습니다.
```
expected: 100000
 but was: 10000
```

### 3. 원인 분석 : Lost Update
- 동시에 여러 트랜잭션이 동일한 잔고(0)를 조회합니다.
- 각각 동일한 값(10000원)을 업데이트하여 마지막 결과만 반영합니다.
```sql
[스레드 A]                 [스레드 B]
  SELECT balance=0
                             SELECT balance=0
  UPDATE balance=10000
                             UPDATE balance=10000
```

### 4. 해결 방법 및 선택 이유

**낙관적 락 사용**
- 사용자가 충전 요청을 동시에 하는 경우(따닥)는 동시성 문제로 보기 어렵다고 판단했습니다.
- 하나의 사용자가 두 개 이상의 창(탭)에서 동시에 충전 요청을 보낼 가능성은 희박하다고 보고 동시성 문제 발생 가능성을 낙관적으로 바라보았습니다. 그럼에도 불구하고 데이터 정합성을 지켜야 하기 때문에 낙관적 락을 적용하였습니다. 
- 충돌이 발생한 요청은 실패시키고, 사용자에게 재시도를 유도하는 방식이 더 적절할다고 판단했습니다.
- 두 개 이상의 탭이나 디바이스에서 동시에 충전 요청을 보내는 상황은 일반적인 사용 행태가 아니며,
  이를 시스템이 알아서 재시도하게 만들 경우 오히려 불필요한 부하나 예측 불가능한 동작을 초래할 수 있습니다.
  따라서 낙관적 락을 통해 데이터 정합성은 보장하면서도, 충돌 발생 시에는 사용자에게 명확한 피드백을 제공하고 스스로 재시도하도록 유도하는 전략을 선택했습니다.

### 5. 실험 결과

**`OPTIMISTIC(낙관적 락)` 사용**

```sql
@Transactional
    public UserPointResult charge(PointChargeCommand command) {
        UserPoint userPoint = findForUpdate(command.userId());

        userPoint.increaseBalance(command.amount());
        pointHistoryRepository.save(PointHistory.createChargeHistory(userPoint.getId(), command.amount()));

        return UserPointResult.from(userPoint);
    }

private UserPoint findWithOptimisticLock(long userId){
        return userPointRepository.findWithOptimisticLock(userId).orElseThrow(() ->
            new BusinessException(ErrorCode.USER_POINT_NOT_FOUND));
    }

public interface UserPointRepository extends JpaRepository<UserPoint, Long> {
    @Lock(LockModeType.OPTIMISTIC)
    @Query("select up from UserPoint up where up.userId = :userId")
    Optional<UserPoint> findWithOptimisticLock(long userId);
}
```

```sql
org.springframework.orm.ObjectOptimisticLockingFailureException: Row was updated or deleted by another transaction
```

- `ObjectOptimisticLockingFailureException` 은 이미 다른 트랜잭션에서 동일 엔티티를 수정할 경우 발생합니다.
  - 이는 엔티티에 설정된 `@Version` 값이 현재 트랜잭션이 조회헀을 때의 버전과 다르기 때문에 발생합니다. 즉, 낙관적 락이 감지한 충돌입니다. 
  - 엔티티에 있는 version 이 트랜잭션이 알고 있는 트랜잭션과 다르기 때문입니다.

---
## 유저 포인트 차감

### 1. 배경

유저가 포인트를 사용할 때 다음과 같은 절차로 처리됩니다.
1. 특정 포인트 ID를 기반으로 `UserPoint` 엔티티를 조회합니다.
2. 기존 포인트에 뺀 값을 기준으로 업데이트합니다.
```sql
select up from user_point up where up.user_id = 1; -- (1)
update user_point set balance=90000 .. where id = 1; -- (2)
```

### 2. 문제

포인트 100000원인 사용자 계정에 대해 10개의 스레드가 동시에 10000원씩 차감 요청을 보내는 테스트를 실행했습니다.\
예상 결과는 0원이지만 실제 결과는 90000원이 되어 Lost Update가 발생했습니다.
```
expected: 0
 but was: 90000
```

### 3. 원인 분석 : Lost Update
- 동시에 동일한 포인트를 조회하고, 각각 차감한 결과를 동일하게 저장하여 한 쪽의 결과가 유실되었습니다.
```sql
[스레드 A]                 [스레드 B]
  SELECT balance=100000
                             SELECT balance=100000
  UPDATE balance=90000
                             UPDATE balance=90000
```
### 4. 해결 방법 및 선택 이유
**비관적 락 사용**

- 포인트 차감은 사용자 재시도보다 시스템이 책임지고 정확하게 처리하는 것이 바람직하다고 판단했습니다.
- 특히 차감 실패 시 결제 실패 등 비즈니스에 미치는 영향이 크기 때문에, 비관적 락을 사용해 정합성을 강제하고 안전하게 처리하는 것이 적절하다고 보았습니다.

### 5. 실험 결과

**`PESSIMISTIC_WRITE(배타락)` 사용**

```sql
@Transactional
    public UserPointResult use(Long orderId, PointUseCommand command){
        UserPoint userPoint = findWithPessimisticLock(command.userId()); //배타락 적용

        userPoint.decreaseBalance(command.amount());
        pointHistoryRepository.save(PointHistory.createUseHistory(orderId, userPoint.getId(), command.amount()));

        return UserPointResult.from(userPoint);
}

private UserPoint findWithPessimisticLock(long userId){
        return userPointRepository.findWithPessimisticLock(userId).orElseThrow(() ->
                new BusinessException(ErrorCode.USER_POINT_NOT_FOUND));
    }

public interface UserPointRepository extends JpaRepository<UserPoint, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select up from UserPoint up where up.userId = :userId")
    Optional<UserPoint> findForUpdate(long userId);
}
```

```sql
select up1_0.id,up1_0.balance,up1_0.updated_at,up1_0.user_id from user_point up1_0 where up1_0.user_id=1 for update;
update user_point set balance=90000,updated_at='2025-08-07T05:28:43.056+0000',user_id=1 where id=1;
```

- 요청이 순차적으로 처리되며 포인트가 정확하게 차감되었습니다.

---
## 선착순 쿠폰 발급

### 배경

여러 유저가 선착순 쿠폰을 발급받을 때 다음과 같은 절차로 처리됩니다.
1. 특정 쿠폰 ID를 기반으로 `Coupon Quantity` 엔티티를 조회합니다.
2. 쿠폰을 발급하고 수량을 증가시킵니다.

```sql
SELECT * FROM coupon WHERE id=1;
SELECT * FROM coupon_quantity WHERE coupon_id=1; -- (1)
INSERT INTO user_coupon ...; -- (2-1)
UPDATE coupon_quantity SET issued_quantity = ? WHERE id = 1; -- (2-2)
```

### 문제
- 10개의 스레드가 동시에 동일 쿠폰을 발급받는 요청을 보냈지만, 실제 발급 수량은 1개로 처리되었습니다.
```
expected: 10
 but was: 1
```

### 원인 분석 : Lost Update
- 동시에 여러 트랜잭션이 동일한 issued_quantity를 읽고, 각각 증가시키면서 마지막 값만 반영되었습니다.

```sql
[스레드 A]                      [스레드 B]
  SELECT issued_quantity = 0
                                SELECT issued_quantity = 0
  UPDATE issued_quantity = 1
                                UPDATE issued_quantity = 1
```

### 해결 방법 및 선택 이유

**비관적 락 선택**
- 선착순 쿠폰은 경쟁이 심하고 정해진 수량만큼 락을 걸면서도 보장하는 것이 맞다고 판단하여 선택하였습니다.


### 실험 결과

**`PESSIMISTIC_WRITE(배타락)` 사용**

```sql
@Transactional
    public UserCouponResult issue(long userId, long couponId) {

        if (userCouponRepository.existsByUserIdAndCouponId(userId, couponId)) {
            throw new BusinessException(ErrorCode.ALREADY_ISSUED_COUPON);
        }

        Coupon coupon = getCoupon(couponId);
        CouponQuantity couponQuantity = couponQuantityRepository.findForUpdate(couponId);

        coupon.validateIssuePeriod();
        couponQuantity.increaseIssuedQuantity();

        UserCoupon issuedCoupon = UserCoupon.create(userId, couponId, coupon.getIssuedEndedAt());

        return UserCouponResult.from(userCouponRepository.save(issuedCoupon));
    }
    
 public interface CouponQuantityRepository extends JpaRepository<CouponQuantity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select cq from CouponQuantity cq where couponId = :couponId")
    CouponQuantity findForUpdate(long couponId);
}

```

```sql
select c1_0.id,c1_0.created_at,c1_0.discount_type,c1_0.discount_value,c1_0.issued_ended_at,c1_0.issued_started_at,c1_0.name from coupon c1_0 where c1_0.id=?
update user_point set balance=90000,updated_at='2025-08-07T05:28:43.056+0000',user_id=1 where id=1;
```
- 요청이 순차적으로 처리되며 정확한 수량 증가가 이루어졌습니다.
- 중복 발급 없이 정확한 선착순 처리가 가능함을 확인하였습니다.