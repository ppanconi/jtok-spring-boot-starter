package com.jtok.spring.order;

import com.jtok.spring.domainevent.DomainEvent;
import com.jtok.spring.domainevent.DomainEventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minidev.json.JSONObject;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(name = "Orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;
    @NotNull
    String globalId;
    @NotNull
    String customer;
    @NotNull
    OrderStatus status;
    @NotNull
    Currency currency;
    @NotNull
    BigDecimal granTotal;
    @Nullable
    String notes;



    /**
     * Clears all domain events currently held. Usually invoked by the infrastructure in place in Spring Data
     * repositories.
     */
    @AfterDomainEventPublication
    protected void clearDomainEvents() {
    }

    /**
     * All domain events currently captured by the aggregate.
     */
    @DomainEvents
    protected Collection<Object> domainEvents() {

        if (this.getStatus() == OrderStatus.CREATED_TO_APPROVE) { ;

            final Order obj = this;

            DomainEvent event = new DomainEvent(
                    this.getGlobalId(),
                    DomainEventType.ORDER_CREATED,
                    System.currentTimeMillis());

            event.setEventData(JSONObject.toJSONString(
                    new HashMap<String, Object>() {{
                        put("globalId", obj.getGlobalId());
                        put("customer", obj.getCustomer());
                        put("currency", obj.getCurrency().getCurrencyCode());
                        put("granTotal", obj.getGranTotal());
                    }}
            ));

            return Collections.singletonList(event);
        } else {
            return Collections.emptyList();
        }
    }

}
