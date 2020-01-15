package it.plansoft.ecommerce.order;

import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RepositoryEventHandler
@Component
public class OrderRestEventHandler {

    @HandleBeforeCreate
    public void onOrderCreation(Order order) {
        String globalId = UUID.randomUUID().toString();
        order.setGlobalId(globalId);
    }

}
