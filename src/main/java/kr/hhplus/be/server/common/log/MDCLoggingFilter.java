package kr.hhplus.be.server.common.log;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)

public class MDCLoggingFilter extends OncePerRequestFilter {

    public static final String MDC_TRACE_ID = "trace_id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        final UUID uuid = UUID.randomUUID();

        try {
            MDC.put(MDC_TRACE_ID, uuid.toString());
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
