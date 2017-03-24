package com.comapi;

import android.text.TextUtils;

import java.util.List;

/**
 * Helper class to construct http query string.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class QueryBuilder {

    private String query;

    private static final String EQUAL = "=";

    private static final String UNEQUAL = "=!";

    private static final String GREATER_THAN = "=>";

    private static final String LESS_THAN = "=<";

    private static final String GREATER_OR_EQUAL_THAN = "=>=";

    private static final String LESS_OR_EQUAL_THAN = "=<=";

    private static final String STARTS_WITH = "=^";

    private static final String ENDS_WITH = "=$";

    private static final String CONTAINS = "=~";

    private static final String EXISTS = "=";

    private static final String NOT_EXISTS = "=!";

    private static final String IN_ARRAY = "[]=";

    private static final String NOT_IN_ARRAY = "[]=!";

    private static final String AND = "&";

    private static final String BEGIN = "?";

    public QueryBuilder() {
        query = "";
    }

    public QueryBuilder addEqual(String key, String value) {

        query = query.concat(getStartChar() + key + EQUAL + value);

        return this;
    }

    public QueryBuilder addUnequal(String key, String value) {

        query = query.concat(getStartChar() + key + UNEQUAL + value);

        return this;
    }

    public QueryBuilder addGreaterThan(String key, String value) {

        query = query.concat(getStartChar() + key + GREATER_THAN + value);

        return this;
    }

    public QueryBuilder addGreaterOrEqualThan(String key, String value) {

        query = query.concat(getStartChar() + key + GREATER_OR_EQUAL_THAN + value);

        return this;
    }

    public QueryBuilder addLessThan(String key, String value) {

        query = query.concat(getStartChar() + key + LESS_THAN + value);

        return this;
    }

    public QueryBuilder addLessOrEqualThan(String key, String value) {

        query = query.concat(getStartChar() + key + LESS_OR_EQUAL_THAN + value);

        return this;
    }

    public QueryBuilder addStartsWith(String key, String value) {

        query = query.concat(getStartChar() + key + STARTS_WITH + value);

        return this;
    }

    public QueryBuilder addEndsWith(String key, String value) {

        query = query.concat(getStartChar() + key + ENDS_WITH + value);

        return this;
    }

    public QueryBuilder addContains(String key, String value) {

        query = query.concat(getStartChar() + key + CONTAINS + value);

        return this;
    }

    public QueryBuilder addExists(String key) {

        query = query.concat(getStartChar() + key + EXISTS);

        return this;

    }

    public QueryBuilder addNotExists(String key) {

        query = query.concat(getStartChar() + key + NOT_EXISTS);

        return this;

    }

    public QueryBuilder inArray(String key, List<String> values) {

        if (!values.isEmpty()) {
            String str = getStartChar() + key + IN_ARRAY + values.get(0);
            for (int i = 1; i < values.size(); i++) {
                str = str.concat(AND + key + IN_ARRAY + values.get(i));
            }
            query = query.concat(str);
        }

        return this;
    }

    public QueryBuilder notInArray(String key, List<String> values) {

        if (!values.isEmpty()) {
            String str = getStartChar() + key + NOT_IN_ARRAY + values.get(0);
            for (int i = 1; i < values.size(); i++) {
                str = str.concat(AND + key + NOT_IN_ARRAY + values.get(i));
            }
            query = query.concat(str);
        }

        return this;
    }

    private String getStartChar() {

        if (TextUtils.isEmpty(query)) {
            return BEGIN;
        }

        return AND;
    }

    /**
     * Query string.
     */
    public String build() {
        return query;
    }

}
