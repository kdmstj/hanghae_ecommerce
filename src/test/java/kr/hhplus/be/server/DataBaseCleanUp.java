package kr.hhplus.be.server;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.hibernate.internal.util.collections.CollectionHelper.listOf;

@Component
public class DataBaseCleanUp {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void execute() {
        List<String> tableNames = listOf(
                "coupon", "coupon_quantity", "user_coupon", "user_coupon_state"
                , "user_point", "point_history"
                , "product"
                , "orders", "order_coupon", "order_payment", "order_product"
        );

        for (String tableName : tableNames) {
            entityManager.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate();
        }
    }

}
