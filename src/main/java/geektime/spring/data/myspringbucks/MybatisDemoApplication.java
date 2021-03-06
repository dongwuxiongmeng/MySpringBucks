package geektime.spring.data.myspringbucks;

import com.github.pagehelper.PageInfo;
import geektime.spring.data.myspringbucks.mapper.CoffeeMapper;
import geektime.spring.data.myspringbucks.mapper.OrderMapper;
import geektime.spring.data.myspringbucks.model.OrderStatus;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import geektime.spring.data.myspringbucks.model.Coffee;
import geektime.spring.data.myspringbucks.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.RowBounds;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;


@SpringBootApplication
@Slf4j
@MapperScan("geektime.spring.data.mybatisdemo.mapper")

public class MybatisDemoApplication implements ApplicationRunner {
	@Autowired
	private DataSource dataSource;
	@Autowired
	private CoffeeMapper coffeeMapper;
	@Autowired
	private OrderMapper orderMapper;

	@Autowired
	private JedisPool jedisPool;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Bean
	@ConfigurationProperties("redis")
	public JedisPoolConfig jedisPoolConfig() {
		return new JedisPoolConfig();
	}

	@Bean(destroyMethod = "close")
	public JedisPool jedisPool(@Value("${redis.host}") String host) {
		return new JedisPool(jedisPoolConfig(), host);
	}

	public static void main(String[] args) {
		SpringApplication.run(MybatisDemoApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		log.info("========??????Druid???????????????==========");
		log.info(dataSource.toString());

		log.info("========????????????????????????1===========");
		coffeeMapper.findAllWithRowBounds(new RowBounds(1, 3))
				.forEach(c -> log.info("Page(1) Coffee {}", c));
		coffeeMapper.findAllWithRowBounds(new RowBounds(2, 3))
				.forEach(c -> log.info("Page(2) Coffee {}", c));

		log.info("=======????????????????????????2===========");
		coffeeMapper.findAllWithParam(1, 3)
				.forEach(c -> log.info("Page(1) Coffee {}", c));
		List<Coffee> list = coffeeMapper.findAllWithParam(2, 3);
		PageInfo page = new PageInfo(list);
		log.info("PageInfo: {}", page);

		log.info("=========????????????2???=========");
		Order d = Order.builder().customer("zhangs").state(OrderStatus.INIT.ordinal()).build();
		int count = orderMapper.save(d);
		log.info("Save {} order: {}", count, d);

		d = Order.builder().customer("lis").state(OrderStatus.INIT.ordinal()).build();
		count = orderMapper.save(d);
		log.info("Save {} order: {}", count, d);

		log.info("=========??????id??????????????????????????????=========");
		d.setState(OrderStatus.PAID.ordinal());
		orderMapper.updateOrderStatusById(d);
		d = orderMapper.findById(2L);
		log.info("Find order By Id: {}", d);

		log.info("=========????????????=========");
		log.info("=========??????ID????????????=========");
		d = orderMapper.findById(1L);
		log.info("Find order By Id: {}", d);
		log.info("=========??????????????????????????????=========");
		d = orderMapper.findByCustomer("lis");
		log.info("Find order By Customer: {}", d);
		log.info("=========??????????????????=========");
		orderMapper.findAllOrderWithRowBounds(new RowBounds(1,0))
				.forEach(c -> log.info("Page(1) order {}",c));
		log.info("=========??????ID????????????????????????????????????????????????????????????=========");
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
				orderMapper.deleteOrderById(1L);;
				log.info("=========??????????????????????????????????????????????????????=========");
				orderMapper.findAllOrderWithRowBounds(new RowBounds(1,0))
						.forEach(c -> log.info("delete order {}",c));
				transactionStatus.setRollbackOnly();
			}
		});
		log.info("=========??????????????????????????????????????????????????????=========");
		orderMapper.findAllOrderWithRowBounds(new RowBounds(1,0))
				.forEach(c -> log.info("rollback order {}",c));

		log.info("=========????????????????????????redis????????????redis??????????????????????????????=========");
		try (Jedis jedis = jedisPool.getResource()) {
			coffeeMapper.findAllWithRowBounds(new RowBounds(1,0)).forEach(c -> {
				jedis.hset("springbucks-menu",
						c.getName(),
						Long.toString(c.getPrice().getAmountMinorLong()));
			});

			Map<String, String> menu = jedis.hgetAll("springbucks-menu");
			log.info("Menu: {}", menu);

			String price = jedis.hget("springbucks-menu", "espresso");
			log.info("espresso - {}",
					Money.ofMinor(CurrencyUnit.of("CNY"), Long.parseLong(price)));
		}

	}
}

