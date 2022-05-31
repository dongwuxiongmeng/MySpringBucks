# MySpringBucks
1. 数据库连接池：springboot 2.x 默认使用HikariCP，将其替换为Druid
2. 使用mybatis进行增删改查操作，并通过 Mybatis PageHelper实现翻页和批量查询
3. 通过Transactional实现事务管理
4. 将mybatis查询得到的结果存储到redis中，并从redis中获取结果

