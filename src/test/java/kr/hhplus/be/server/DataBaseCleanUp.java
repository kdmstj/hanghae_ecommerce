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

    private List<String> tableNames;


    @Transactional
    public void execute() {
        tableNames = listOf("coupon", "coupon_quantity", "user_coupon", "user_coupon_state");
        for (String tableName : tableNames) {
            entityManager.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate();
        }
    }

}
