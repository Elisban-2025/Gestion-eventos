package com.union.asistencia.config;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;

public class SQLiteDialect extends Dialect {
    
    public SQLiteDialect() {
        registerFunction("concat", new StandardSQLFunction("concat", StandardBasicTypes.STRING));
    }
}