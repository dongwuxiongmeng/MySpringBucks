package geektime.spring.data.myspringbucks.mapper;

import geektime.spring.data.myspringbucks.model.Order;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

@Mapper
public interface OrderMapper {
    @Insert("insert into t_order (customer, state, create_time, update_time)"
            + "values (#{customer}, #{state}, now(), now())")
    @Options(useGeneratedKeys = true)
    int save(Order order);

    @Select("select * from t_order where id = #{id}")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "create_time", property = "createTime"),
            // map-underscore-to-camel-case = true 可以实现一样的效果
            // @Result(column = "update_time", property = "updateTime"),
    })
    Order findById(@Param("id") Long id);

    @Select("select * from t_order where customer = #{customer}")
    @Results({
            @Result(column = "create_time", property = "createTime"),
    })
    Order findByCustomer(@Param("customer") String customer);

    @Update({
            "update T_ORDER",
            "set CREATE_TIME = #{createTime,jdbcType=TIMESTAMP},",
            "UPDATE_TIME = #{updateTime,jdbcType=TIMESTAMP},",
            "CUSTOMER = #{customer,jdbcType=VARCHAR},",
            "STATE = #{state,jdbcType=INTEGER}",
            "where ID = #{id,jdbcType=BIGINT}"
    })
    int updateOrderStatusById(Order record);


    @Delete({
            "delete from T_ORDER",
            "where ID = #{id,jdbcType=BIGINT}"
    })
    int deleteOrderById(Long id);



    @Select("select * from t_order order by id")
    List<Order> findAllOrderWithRowBounds(RowBounds rowBounds);

    @Select("select * from t_coffee order by id")
    List<Order> findAllOrderWithParam(@Param("pageNum") int pageNum,
                                      @Param("pageSize") int pageSize);
}
