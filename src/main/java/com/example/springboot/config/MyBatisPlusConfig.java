package com.example.springboot.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.example.springboot.dto.AdminPersonalInfoDTO;
import com.example.springboot.entity.Admin;
import com.example.springboot.entity.Department;
import com.example.springboot.enums.CollegeEnum;
import com.example.springboot.enums.GradeEnum;
import com.example.springboot.excption.BusinessErrorException;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import javax.sql.DataSource;

@Configuration
@MapperScan("com.example.springboot.mapper") // 仅配置一次Mapper扫描
public class MyBatisPlusConfig {

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean sessionFactory = new MybatisSqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);

        // 注意：如果没创建CollegeEnum和GradeEnum，直接删除下面的枚举处理器配置！
         sessionFactory.setTypeHandlers(new MybatisEnumTypeHandler[]{
             new MybatisEnumTypeHandler<>(CollegeEnum.class),
             new MybatisEnumTypeHandler<>(GradeEnum.class)
         });

        // 不需要XML映射文件，删除MapperLocations配置
        return sessionFactory.getObject();
    }

    // 分页插件（正常保留）
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.H2)); // 你的数据库类型（H2/MySQL等）
        return interceptor;
    }

}