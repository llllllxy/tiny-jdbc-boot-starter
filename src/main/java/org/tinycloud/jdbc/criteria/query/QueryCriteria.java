package org.tinycloud.jdbc.criteria.query;

import org.springframework.util.ObjectUtils;
import org.tinycloud.jdbc.criteria.AbstractCriteria;

import java.util.Arrays;

/**
 * <p>
 * 查询操作-条件构造器
 * </p>
 *
 * @author liuxingyu01
 * @since 2023-08-02
 **/
public class QueryCriteria<T> extends AbstractCriteria<T, QueryCriteria<T>> {

    public final QueryCriteria<T> select(String... field) {
        if (!ObjectUtils.isEmpty(field)) {
            selectFields.addAll(Arrays.asList(field));
        }
        return this;
    }

    public final QueryCriteria<T> orderBy(String field, boolean desc) {
        String orderByString = field;
        if (desc) {
            orderByString += " DESC";
        }
        orderBy.add(orderByString);
        return this;
    }

    public final QueryCriteria<T> orderBy(String field) {
        String orderByString = field;
        orderBy.add(orderByString);
        return this;
    }

    @Override
    protected QueryCriteria<T> instance() {
        return new QueryCriteria<T>();
    }
}