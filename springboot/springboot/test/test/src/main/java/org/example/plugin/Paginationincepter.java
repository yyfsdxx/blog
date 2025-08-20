package org.example.plugin;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.executor.Executor;
import org.springframework.web.servlet.handler.MappedInterceptor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;


/**
 * @author yufengyang
 * @Package org.example.plugin
 * @date 2025/8/16 22:14
 * @school hnist
 */
@Intercepts({
        @Signature(type = Executor.class , method = "query",args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class})
})

public class Paginationincepter implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        PageParam page= PaginationContext.getPageHolder();
        if(page == null){
            return invocation.proceed();
        }
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameterObject = args[1];
        RowBounds rowBounds = (RowBounds) args[2];
        ResultHandler<?> resultHandler = (ResultHandler<?>) args[3];
        CacheKey cacheKey;
        BoundSql boundSql;
        Executor executor = (Executor) invocation.getTarget();
        if(args.length==4){
             boundSql = ms.getBoundSql(parameterObject);
             cacheKey = executor.createCacheKey(ms, parameterObject, rowBounds, boundSql);
        }else {
            boundSql = (BoundSql) args[4];
            cacheKey = (CacheKey) args[5];
        }
        String originSql = boundSql.getSql().trim();// trim()方法为去除前后空格
        String countSql = "select count(*) from("+ stripOderby(originSql)+") as total_query";
        long total = queryCount(executor,ms,parameterObject,boundSql,countSql);
        PaginationContext.setTotalHolder(total);

        String pagesql=originSql + "LIMIT"+page.offset()+","+page.getPageSize();
        BoundSql newBoundsql = new BoundSql(ms.getConfiguration(),pagesql,boundSql.getParameterMappings(),parameterObject);

        for(ParameterMapping pm : boundSql.getParameterMappings()){
            String prop = pm.getProperty();
            if(boundSql.hasAdditionalParameter(prop)){
                newBoundsql.setAdditionalParameter(prop,boundSql.getAdditionalParameter(prop));
            }
        }
        // 虽然已经有新的boundsql，但是mappstament其他属性仍然需要，所以这里需要copy一下
        MappedStatement newMs = copyMappedStatement(ms,new BoundSqlSource(newBoundsql));





        return null;
    }

    private MappedStatement copyMappedStatement(MappedStatement ms, BoundSqlSource boundSqlSource) {
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), ms.getId(),boundSqlSource,ms.getSqlCommandType())
                .resource(ms.getResource())
                .fetchSize(ms.getFetchSize())
                .statementType(ms.getStatementType())
                .keyGenerator(ms.getKeyGenerator())
                .timeout(ms.getTimeout())
                .parameterMap(ms.getParameterMap())
                .resultMaps(ms.getResultMaps())
                .resultSetType(ms.getResultSetType())
                .cache(ms.getCache())
                .flushCacheRequired(ms.isFlushCacheRequired())
                .useCache(ms.isUseCache());
        if(ms.getKeyProperties()!=null&&ms.getKeyProperties().length>0)
            builder.keyProperty(String.join(",", ms.getKeyProperties()));

        return builder.build();



    }

    private String stripOderby(String sql){
        String lower = sql.toLowerCase(Locale.ROOT);
        int idx = lower.lastIndexOf("order by");
        return (idx > -1)?sql.substring(0,idx) : sql;
    }
    private long queryCount(Executor executor, MappedStatement ms, Object param, BoundSql boundSql, String countSql) throws SQLException {
        Configuration configuration = ms.getConfiguration();
        try (
            Connection connection = executor.getTransaction().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(countSql)){
            ParameterHandler ph = configuration.newParameterHandler(ms, param, boundSql);
            ph.setParameters(preparedStatement);
            try (ResultSet resultSet = preparedStatement.executeQuery()){
                return resultSet.next() ? resultSet.getLong(1) : 0L;
            }

        }
    }
    private static  class  BoundSqlSource implements SqlSource{
        private final BoundSql boundSql;

        public BoundSqlSource(BoundSql boundSql) {
            this.boundSql = boundSql;
        }

        @Override
        public BoundSql getBoundSql(Object parameterObject) {
            return boundSql;
        }
    }

    }










