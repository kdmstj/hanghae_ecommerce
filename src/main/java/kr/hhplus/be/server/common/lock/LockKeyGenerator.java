package kr.hhplus.be.server.common.lock;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LockKeyGenerator {

    private static final ExpressionParser PARSER = new SpelExpressionParser();

    public static List<String> generateKeys(
            String[] keySpELs,
            String[] parameterNames,
            Object[] args
    ) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        List<String> keys = new ArrayList<>();

        for (int i = 0; i <parameterNames.length; i++){
            context.setVariable(parameterNames[i], args[i]);
        }

        for(String keySpEL: keySpELs){
            Expression expression = PARSER.parseExpression(keySpEL);
            Object value = expression.getValue(context);

            if(value instanceof Iterable<?> it){
                List<String> list = new ArrayList<>();
                for (Object v : it) {
                    list.add(Objects.toString(v));
                }
                list.sort(String::compareTo);
                keys.addAll(list);
            } else{
                keys.add(Objects.toString(value));
            }
        }

        return keys;
    }

}
