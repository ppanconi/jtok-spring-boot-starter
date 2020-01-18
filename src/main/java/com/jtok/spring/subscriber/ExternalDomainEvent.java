package com.jtok.spring.subscriber;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minidev.json.JSONObject;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExternalDomainEvent {

    private String id;

    private String key;

    private String name;

    JSONObject payload;

}
