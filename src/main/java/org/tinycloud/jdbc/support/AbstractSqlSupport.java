package org.tinycloud.jdbc.support;

import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.tinycloud.jdbc.criteria.Criteria;
import org.tinycloud.jdbc.criteria.LambdaCriteria;
import org.tinycloud.jdbc.exception.JdbcException;
import org.tinycloud.jdbc.page.IPageHandle;
import org.tinycloud.jdbc.page.Page;
import org.tinycloud.jdbc.sql.SqlGenerator;
import org.tinycloud.jdbc.sql.SqlProvider;

import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;


/**
 * jdbc抽象类，给出默认的支持
 *
 * @author liuxingyu01
 * @since 2022-03-11-16:49
 **/
public abstract class AbstractSqlSupport<T, ID> implements ISqlSupport<T, ID>, IObjectSupport<T, ID> {

    protected abstract JdbcTemplate getJdbcTemplate();

    protected abstract IPageHandle getPageHandle();


    /**
     * 泛型
     */
    private final Class<T> entityClass;

    /**
     * bean转换器
     */
    private final RowMapper<T> rowMapper;

    @SuppressWarnings("unchecked")
    public AbstractSqlSupport() {
        ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
        entityClass = (Class<T>) type.getActualTypeArguments()[0];
        rowMapper = BeanPropertyRowMapper.newInstance(entityClass);
    }


    /**
     * 执行查询sql，有查询条件
     *
     * @param sql    要执行的SQL
     * @param params 要绑定到查询的参数 ，可以不传
     * @return 查询结果
     */
    @Override
    public List<T> select(String sql, Object... params) {
        List<T> resultList;
        if (params != null && params.length > 0) {
            resultList = getJdbcTemplate().query(sql, params, rowMapper);
        } else {
            // BeanPropertyRowMapper是自动映射实体类的
            resultList = getJdbcTemplate().query(sql, rowMapper);
        }
        return resultList;
    }

    /**
     * 执行查询sql，有查询条件
     *
     * @param sql    要执行的SQL
     * @param clazz  实体类
     * @param params 要绑定到查询的参数 ，可以不传
     * @param <F>    泛型
     * @return 查询结果
     */
    @Override
    public <F> List<F> select(String sql, Class<F> clazz, Object... params) {
        List<F> resultList;
        if (params != null && params.length > 0) {
            resultList = getJdbcTemplate().query(sql, params, new BeanPropertyRowMapper<>(clazz));
        } else {
            // BeanPropertyRowMapper是自动映射实体类的
            resultList = getJdbcTemplate().query(sql, new BeanPropertyRowMapper<>(clazz));
        }
        return resultList;
    }

    /**
     * 执行查询sql，有查询条件，（固定返回List<Map<String, Object>>）
     *
     * @param sql    要执行的sql
     * @param params 要绑定到查询的参数
     * @return Map<String, Object>
     */
    @Override
    public List<Map<String, Object>> selectMap(String sql, Object... params) {
        return getJdbcTemplate().queryForList(sql, params);
    }

    /**
     * 执行查询sql，有查询条件，结果返回第一条（固定返回Map<String, Object>）
     *
     * @param sql    要执行的sql
     * @param params 要绑定到查询的参数
     * @return Map<String, Object>
     */
    @Override
    public Map<String, Object> selectOneMap(String sql, final Object... params) {
        List<Map<String, Object>> resultList = getJdbcTemplate().queryForList(sql, params);
        if (!CollectionUtils.isEmpty(resultList)) {
            return resultList.get(0);
        }
        return null;
    }

    /**
     * 查询一个值（经常用于查count）
     *
     * @param sql    要执行的SQL查询
     * @param clazz  实体类
     * @param params 要绑定到查询的参数
     * @param <F>    泛型
     * @return T
     */
    @Override
    public <F> F selectOneColumn(String sql, Class<F> clazz, Object... params) {
        F result;
        if (params == null || params.length == 0) {
            result = getJdbcTemplate().queryForObject(sql, clazz);
        } else {
            result = getJdbcTemplate().queryForObject(sql, params, clazz);
        }
        return result;
    }

    /**
     * 分页查询
     *
     * @param sql  要执行的SQL查询
     * @param page 分页参数
     * @return T
     */
    @Override
    public Page<T> paginate(String sql, Page<T> page) {
        if (page == null || page.getPageNum() == null || page.getPageSize() == null) {
            throw new JdbcException("paginate page cannot be null");
        }
        if (page.getPageNum() <= 0) {
            throw new JdbcException("当前页数必须大于1");
        }
        if (page.getPageSize() <= 0) {
            throw new JdbcException("每页大小必须大于1");
        }
        String selectSql = getPageHandle().handlerPagingSQL(sql, page.getPageNum(), page.getPageSize());
        String countSql = getPageHandle().handlerCountSQL(sql);
        // 查询数据列表
        List<T> resultList = getJdbcTemplate().query(selectSql, rowMapper);
        // 查询总共数量
        int totalSize = getJdbcTemplate().queryForObject(countSql, Integer.class);
        page.setRecords(resultList);
        page.setTotal(totalSize);
        return page;
    }

    /**
     * 分页查询（带参数）
     *
     * @param sql    要执行的SQL
     * @param page   分页参数
     * @param params ？参数
     * @return Page<T>
     */
    @Override
    public Page<T> paginate(String sql, Page<T> page, final Object... params) {
        if (page == null || page.getPageNum() == null || page.getPageSize() == null) {
            throw new JdbcException("paginate page cannot be null");
        }
        if (page.getPageNum() <= 0) {
            throw new JdbcException("当前页数必须大于1");
        }
        if (page.getPageSize() <= 0) {
            throw new JdbcException("每页大小必须大于1");
        }
        String selectSql = getPageHandle().handlerPagingSQL(sql, page.getPageNum(), page.getPageSize());
        String countSql = getPageHandle().handlerCountSQL(sql);
        // 查询数据列表
        List<T> resultList = getJdbcTemplate().query(selectSql, params, rowMapper);
        // 查询总共数量
        int totalSize = getJdbcTemplate().queryForObject(countSql, params, Integer.class);
        page.setRecords(resultList);
        page.setTotal(totalSize);
        return page;
    }

    /**
     * 分页查询（带参数）
     *
     * @param sql    要执行的SQL
     * @param clazz  实体类型
     * @param page   分页参数
     * @param params ？参数
     * @return Page<F>
     */
    @Override
    public <F> Page<F> paginate(String sql, Class<F> clazz, Page<F> page, final Object... params) {
        if (page == null || page.getPageNum() == null || page.getPageSize() == null) {
            throw new JdbcException("paginate page cannot be null");
        }
        if (page.getPageNum() <= 0) {
            throw new JdbcException("当前页数必须大于1");
        }
        if (page.getPageSize() <= 0) {
            throw new JdbcException("每页大小必须大于1");
        }
        String selectSql = getPageHandle().handlerPagingSQL(sql, page.getPageNum(), page.getPageSize());
        String countSql = getPageHandle().handlerCountSQL(sql);
        // 查询数据列表
        List<F> resultList = getJdbcTemplate().query(selectSql, params, new BeanPropertyRowMapper<>(clazz));
        // 查询总共数量
        int totalSize = getJdbcTemplate().queryForObject(countSql, params, Integer.class);
        page.setRecords(resultList);
        page.setTotal(totalSize);
        return page;
    }


    /**
     * 执行删除，插入，更新操作
     *
     * @param sql    要执行的SQL
     * @param params 要绑定到SQL的参数
     * @return 成功的条数
     */
    @Override
    public int execute(String sql, final Object... params) {
        int num = 0;
        if (params == null || params.length == 0) {
            num = getJdbcTemplate().update(sql);
        } else {
            num = getJdbcTemplate().update(sql, params);
        }
        return num;
    }

    @Override
    public int insert(String sql, final Object... params) {
        int num = 0;
        if (params == null || params.length == 0) {
            num = getJdbcTemplate().update(sql);
        } else {
            num = getJdbcTemplate().update(sql, params);
        }
        return num;
    }

    @Override
    public int update(String sql, final Object... params) {
        int num = 0;
        if (params == null || params.length == 0) {
            num = getJdbcTemplate().update(sql);
        } else {
            num = getJdbcTemplate().update(sql, params);
        }
        return num;
    }

    @Override
    public int delete(String sql, final Object... params) {
        int num = 0;
        if (params == null || params.length == 0) {
            num = getJdbcTemplate().update(sql);
        } else {
            num = getJdbcTemplate().update(sql, params);
        }
        return num;
    }

    /**
     * 使用 in 进行批量操作，比如批量启用，批量禁用，批量删除等 -- 更灵活的就需要自己写了
     *
     * @param sql    示例： update s_url_map set del_flag = '1' where id in (:idList)
     * @param idList 一般为 List<String> 或 List<Integer>
     * @return 执行的结果条数
     */
    @Override
    public int batchOpera(String sql, List<Object> idList) {
        if (idList == null || idList.size() == 0) {
            throw new JdbcException("batchOpera idList cannot be null or empty");
        }
        NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(getJdbcTemplate());
        Map<String, Object> param = new HashMap<>();
        param.put("idList", idList);
        return namedJdbcTemplate.update(sql, param);
    }
    // ---------------------------------ISqlSupport结束---------------------------------


    // ---------------------------------IObjectSupport开始---------------------------------
    @Override
    public T selectById(ID id) {
        if (id == null) {
            throw new JdbcException("selectById id cannot be null");
        }
        SqlProvider sqlProvider = SqlGenerator.selectByIdSql(id, entityClass);
        List<T> list = getJdbcTemplate().query(sqlProvider.getSql(), sqlProvider.getParameters().toArray(), rowMapper);
        if (!CollectionUtils.isEmpty(list)) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public List<T> selectByIds(List<ID> ids) {
        SqlProvider sqlProvider = SqlGenerator.selectByIdsSql(entityClass);
        NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(getJdbcTemplate());
        Map<String, Object> param = new HashMap<>();
        param.put("idList", ids);
        return namedJdbcTemplate.query(sqlProvider.getSql(), param, rowMapper);
    }

    @Override
    public List<T> select(T entity) {
        if (entity == null) {
            throw new JdbcException("select entity cannot be null");
        }
        SqlProvider sqlProvider = SqlGenerator.selectSql(entity);
        return getJdbcTemplate().query(sqlProvider.getSql(), sqlProvider.getParameters().toArray(), rowMapper);
    }

    @Override
    public List<T> select(Criteria criteria) {
        if (criteria == null) {
            throw new JdbcException("criteria cannot be null");
        }
        SqlProvider sqlProvider = SqlGenerator.selectCriteriaSql(criteria, entityClass);
        return getJdbcTemplate().query(sqlProvider.getSql(), sqlProvider.getParameters().toArray(), rowMapper);
    }

    @Override
    public List<T> select(LambdaCriteria lambdaCriteria) {
        if (lambdaCriteria == null) {
            throw new JdbcException("lambdaCriteria cannot be null");
        }
        SqlProvider sqlProvider = SqlGenerator.selectLambdaCriteriaSql(lambdaCriteria, entityClass);
        return getJdbcTemplate().query(sqlProvider.getSql(), sqlProvider.getParameters().toArray(), rowMapper);
    }

    @Override
    public Page<T> paginate(T entity, Page<T> page) {
        if (entity == null) {
            throw new JdbcException("paginate entity cannot be null");
        }
        if (page == null || page.getPageNum() == null || page.getPageSize() == null) {
            throw new JdbcException("paginate page cannot be null");
        }
        if (page.getPageNum() <= 0) {
            throw new JdbcException("当前页数必须大于1");
        }
        if (page.getPageSize() <= 0) {
            throw new JdbcException("每页大小必须大于1");
        }
        SqlProvider sqlProvider = SqlGenerator.selectSql(entity);
        String selectSql = getPageHandle().handlerPagingSQL(sqlProvider.getSql(), page.getPageNum(), page.getPageSize());
        String countSql = getPageHandle().handlerCountSQL(sqlProvider.getSql());
        // 查询数据列表
        List<T> resultList = getJdbcTemplate().query(selectSql, sqlProvider.getParameters().toArray(), rowMapper);
        // 查询总共数量
        int totalSize = getJdbcTemplate().queryForObject(countSql, sqlProvider.getParameters().toArray(), Integer.class);
        page.setRecords(resultList);
        page.setTotal(totalSize);
        return page;
    }

    @Override
    public Page<T> paginate(Criteria criteria, Page<T> page) {
        if (criteria == null) {
            throw new JdbcException("paginate criteria cannot be null");
        }
        if (page == null || page.getPageNum() == null || page.getPageSize() == null) {
            throw new JdbcException("paginate page cannot be null");
        }
        if (page.getPageNum() <= 0) {
            throw new JdbcException("当前页数必须大于1");
        }
        if (page.getPageSize() <= 0) {
            throw new JdbcException("每页大小必须大于1");
        }
        SqlProvider sqlProvider = SqlGenerator.selectCriteriaSql(criteria, entityClass);
        String selectSql = getPageHandle().handlerPagingSQL(sqlProvider.getSql(), page.getPageNum(), page.getPageSize());
        String countSql = getPageHandle().handlerCountSQL(sqlProvider.getSql());
        // 查询数据列表
        List<T> resultList = getJdbcTemplate().query(selectSql, sqlProvider.getParameters().toArray(), rowMapper);
        // 查询总共数量
        int totalSize = getJdbcTemplate().queryForObject(countSql, sqlProvider.getParameters().toArray(), Integer.class);
        page.setRecords(resultList);
        page.setTotal(totalSize);
        return page;
    }

    @Override
    public Page<T> paginate(LambdaCriteria lambdaCriteria, Page<T> page) {
        if (lambdaCriteria == null) {
            throw new JdbcException("paginate lambdaCriteria cannot be null");
        }
        if (page == null || page.getPageNum() == null || page.getPageSize() == null) {
            throw new JdbcException("paginate page cannot be null");
        }
        if (page.getPageNum() <= 0) {
            throw new JdbcException("当前页数必须大于1");
        }
        if (page.getPageSize() <= 0) {
            throw new JdbcException("每页大小必须大于1");
        }
        SqlProvider sqlProvider = SqlGenerator.selectLambdaCriteriaSql(lambdaCriteria, entityClass);
        String selectSql = getPageHandle().handlerPagingSQL(sqlProvider.getSql(), page.getPageNum(), page.getPageSize());
        String countSql = getPageHandle().handlerCountSQL(sqlProvider.getSql());
        // 查询数据列表
        List<T> resultList = getJdbcTemplate().query(selectSql, sqlProvider.getParameters().toArray(), rowMapper);
        // 查询总共数量
        int totalSize = getJdbcTemplate().queryForObject(countSql, sqlProvider.getParameters().toArray(), Integer.class);
        page.setRecords(resultList);
        page.setTotal(totalSize);
        return page;
    }

    @Override
    public Long selectCount(Criteria criteria) {
        if (criteria == null) {
            throw new JdbcException("criteria cannot be null");
        }
        SqlProvider sqlProvider = SqlGenerator.selectCountCriteriaSql(criteria, entityClass);
        return getJdbcTemplate().queryForObject(sqlProvider.getSql(), sqlProvider.getParameters().toArray(), Long.class);
    }

    @Override
    public Long selectCount(LambdaCriteria lambdaCriteria) {
        if (lambdaCriteria == null) {
            throw new JdbcException("lambdaCriteria cannot be null");
        }
        SqlProvider sqlProvider = SqlGenerator.selectCountLambdaCriteriaSql(lambdaCriteria, entityClass);
        return getJdbcTemplate().queryForObject(sqlProvider.getSql(), sqlProvider.getParameters().toArray(), Long.class);
    }

    @Override
    public int insert(T entity) {
        return insert(entity, true);
    }

    @Override
    public int insert(T entity, boolean ignoreNulls) {
        if (entity == null) {
            throw new JdbcException("insert entity cannot be null");
        }
        SqlProvider sqlProvider = SqlGenerator.insertSql(entity, ignoreNulls);
        if (sqlProvider.getParameters() == null || sqlProvider.getParameters().isEmpty()) {
            throw new JdbcException("insert parameters cannot be null");
        }
        return execute(sqlProvider.getSql(), sqlProvider.getParameters().toArray());
    }

    @Override
    public Long insertReturnAutoIncrement(T entity) {
        if (entity == null) {
            throw new JdbcException("insertReturnAutoIncrement entity cannot be null");
        }
        SqlProvider sqlProvider = SqlGenerator.insertSql(entity, true);
        if (sqlProvider.getParameters() == null || sqlProvider.getParameters().isEmpty()) {
            throw new JdbcException("insertReturnAutoIncrement parameters cannot be null");
        }
        KeyHolder keyHolder = new GeneratedKeyHolder();
        final Object[] params = sqlProvider.getParameters().toArray();
        if (params == null || params.length == 0) {
            getJdbcTemplate().update(new PreparedStatementCreator() {
                public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                    PreparedStatement ps = con.prepareStatement(sqlProvider.getSql(),
                            PreparedStatement.RETURN_GENERATED_KEYS);
                    return ps;
                }
            }, keyHolder);
        } else {
            getJdbcTemplate().update(new PreparedStatementCreator() {
                public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                    PreparedStatement ps = con.prepareStatement(sqlProvider.getSql(),
                            PreparedStatement.RETURN_GENERATED_KEYS);
                    for (int i = 0; i < params.length; i++)
                        ps.setObject(i + 1, params[i]);
                    return ps;
                }
            }, keyHolder);
        }
        if (keyHolder.getKey() != null) {
            return keyHolder.getKey().longValue();
        } else {
            throw new JdbcException("insertReturnAutoIncrement please check whether it is an autoincrement primary key");
        }
    }

    @Override
    public int updateById(T entity) {
        return updateById(entity, true);
    }

    @Override
    public int updateById(T entity, boolean ignoreNulls) {
        if (entity == null) {
            throw new JdbcException("update entity cannot be null");
        }
        SqlProvider sqlProvider = SqlGenerator.updateByIdSql(entity, ignoreNulls);
        if (sqlProvider.getParameters() == null || sqlProvider.getParameters().isEmpty()) {
            throw new JdbcException("update parameters cannot be null");
        }
        return execute(sqlProvider.getSql(), sqlProvider.getParameters().toArray());
    }

    @Override
    public int update(T entity, Criteria criteria) {
        return update(entity, true, criteria);
    }

    @Override
    public int update(T entity, LambdaCriteria lambdaCriteria) {
        return update(entity, true, lambdaCriteria);
    }

    @Override
    public int update(T entity, boolean ignoreNulls, Criteria criteria) {
        if (entity == null) {
            throw new JdbcException("update entity cannot be null");
        }
        if (criteria == null) {
            throw new JdbcException("criteria cannot be null");
        }
        SqlProvider sqlProvider = SqlGenerator.updateByCriteriaSql(entity, ignoreNulls, criteria);
        if (sqlProvider.getParameters() == null || sqlProvider.getParameters().isEmpty()) {
            throw new JdbcException("update parameters cannot be null");
        }
        return execute(sqlProvider.getSql(), sqlProvider.getParameters().toArray());
    }

    @Override
    public int update(T entity, boolean ignoreNulls, LambdaCriteria criteria) {
        if (entity == null) {
            throw new JdbcException("update entity cannot be null");
        }
        if (criteria == null) {
            throw new JdbcException("criteria cannot be null");
        }
        SqlProvider sqlProvider = SqlGenerator.updateByLambdaCriteriaSql(entity, ignoreNulls, criteria);
        if (sqlProvider.getParameters() == null || sqlProvider.getParameters().isEmpty()) {
            throw new JdbcException("update parameters cannot be null");
        }
        return execute(sqlProvider.getSql(), sqlProvider.getParameters().toArray());
    }

    @Override
    public int delete(T entity) {
        if (entity == null) {
            throw new JdbcException("delete entity cannot be null");
        }
        SqlProvider sqlProvider = SqlGenerator.deleteSql(entity);
        if (sqlProvider.getParameters() == null || sqlProvider.getParameters().isEmpty()) {
            throw new JdbcException("delete parameters cannot be null");
        }
        return execute(sqlProvider.getSql(), sqlProvider.getParameters().toArray());
    }

    @Override
    public int delete(LambdaCriteria criteria) {
        if (criteria == null) {
            throw new JdbcException("delete lambdaCriteria cannot be null");
        }
        SqlProvider sqlProvider = SqlGenerator.deleteLambdaCriteriaSql(criteria, entityClass);
        return execute(sqlProvider.getSql(), sqlProvider.getParameters().toArray());
    }

    @Override
    public int delete(Criteria criteria) {
        if (criteria == null) {
            throw new JdbcException("delete criteria cannot be null");
        }
        SqlProvider sqlProvider = SqlGenerator.deleteCriteriaSql(criteria, entityClass);
        return execute(sqlProvider.getSql(), sqlProvider.getParameters().toArray());
    }

    @Override
    public int deleteById(ID id) {
        if (id == null) {
            throw new JdbcException("deleteById id cannot be null");
        }
        SqlProvider sqlProvider = SqlGenerator.deleteByIdSql(id, entityClass);
        if (sqlProvider.getParameters() == null || sqlProvider.getParameters().isEmpty()) {
            throw new JdbcException("deleteById parameters cannot be null");
        }
        return execute(sqlProvider.getSql(), sqlProvider.getParameters().toArray());
    }

    @Override
    public int deleteByIds(List<ID> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new JdbcException("deleteByIds ids cannot be null or empty");
        }
        SqlProvider sqlProvider = SqlGenerator.deleteByIdsSql(entityClass);
        NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(getJdbcTemplate());
        Map<String, Object> param = new HashMap<>();
        param.put("idList", ids);
        return namedJdbcTemplate.update(sqlProvider.getSql(), param);
    }

    @Override
    public int[] batchUpdate(Collection<T> collection) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new JdbcException("batchUpdate collection cannot be null or empty");
        }
        int[] row = null;
        List<Object[]> batchArgs = new ArrayList<>();
        String sql = "";
        for (T t : collection) {
            SqlProvider sqlProvider = SqlGenerator.updateByIdSql(t, true);
            if (StringUtils.isEmpty(sql)) {
                sql = sqlProvider.getSql();
            }
            batchArgs.add(sqlProvider.getParameters().toArray());
        }
        if (CollectionUtils.isEmpty(batchArgs)) {
            throw new JdbcException("batchUpdate batchArgs cannot be null");
        }
        row = getJdbcTemplate().batchUpdate(sql, batchArgs);
        return row;
    }


    @Override
    public int[] batchInsert(Collection<T> collection) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new JdbcException("batchInsert collection cannot be null or empty");
        }
        int[] row = null;
        List<Object[]> batchArgs = new ArrayList<>();
        String sql = "";
        for (T t : collection) {
            SqlProvider sqlProvider = SqlGenerator.insertSql(t, true);
            if (StringUtils.isEmpty(sql)) {
                sql = sqlProvider.getSql();
            }
            batchArgs.add(sqlProvider.getParameters().toArray());
        }
        if (CollectionUtils.isEmpty(batchArgs)) {
            throw new JdbcException("batchInsert batchArgs cannot be null");
        }
        row = getJdbcTemplate().batchUpdate(sql, batchArgs);
        return row;
    }


    @Override
    public int[] batchDelete(Collection<T> collection) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new JdbcException("batchDelete collection cannot be null or empty");
        }
        int[] row = null;
        List<Object[]> batchArgs = new ArrayList<>();
        String sql = "";
        for (T t : collection) {
            SqlProvider sqlProvider = SqlGenerator.deleteSql(t);
            if (StringUtils.isEmpty(sql)) {
                sql = sqlProvider.getSql();
            }
            batchArgs.add(sqlProvider.getParameters().toArray());
        }
        if (CollectionUtils.isEmpty(batchArgs)) {
            throw new JdbcException("batchDelete batchArgs cannot be null");
        }
        row = getJdbcTemplate().batchUpdate(sql, batchArgs);
        return row;
    }

}
