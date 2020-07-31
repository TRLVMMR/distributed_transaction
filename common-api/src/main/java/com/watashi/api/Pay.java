package com.watashi.api;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@ToString
public class Pay {
    Integer id;
    Integer OrderId;
}
