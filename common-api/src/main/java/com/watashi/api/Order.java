package com.watashi.api;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Setter
@Getter
@ToString
public class Order {
    Long orderId;
    String content;
}
