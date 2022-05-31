package geektime.spring.data.myspringbucks.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CoffeeOrder {
    private Long coffee_order_id;
    private Long items_id;
}

