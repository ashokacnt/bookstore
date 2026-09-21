package com.cg.common;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import com.cg.order.OrderDtos;
import com.cg.order.OrderStatus;
import org.junit.jupiter.api.Test;

class IdempotencyServiceTest {
    @Test
    void sameKeyReturnsSameResponse() {
        var s = new IdempotencyService();
        var r = new OrderDtos.OrderResponse(1L, BigDecimal.TEN, OrderStatus.CREATED, LocalDateTime.now(), List.of());
        s.put("demo", "k", r);
        assertThat(s.get("demo", "k")).isSameAs(r);
    }

    @Test
    void keyCannotBeReusedByAnotherUser() {
        var s = new IdempotencyService();
        var r = new OrderDtos.OrderResponse(1L, BigDecimal.TEN, OrderStatus.CREATED, LocalDateTime.now(), List.of());
        s.put("demo", "k", r);
        assertThatThrownBy(() -> s.get("alice", "k")).isInstanceOf(ConflictException.class);
    }
}
