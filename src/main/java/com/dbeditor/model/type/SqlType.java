package com.dbeditor.model.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.dbeditor.sql.DbType;

public abstract class SqlType {

    public abstract boolean isConform(String data);
    public abstract String getRepr(DbType dbType);

    public static SqlType get(String type, DbType dbType) {
        // TODO: nettoyer "type"
        type = type.trim().replace(" ", "");

        for(SqlType d : AllSqlType.ALL_SQL_TYPE) {
            String data = d.getRepr(dbType);
            // TODO: nettoyer "data"
            if(data.equals(type)) {
                return d;
            }
        }
        return null;
    }

    private static class AllSqlType {
        public final static BigintSql BIGINT = new BigintSql();
        public final static CharSql CHAR = new CharSql(1);
        public final static DateSql DATE = new DateSql();
        public final static DatetimeSql DATETIME = new DatetimeSql();
        public final static DecimalSql DECIMAL = new DecimalSql(1, 1);
        public final static IntSql INT = new IntSql();
        public final static SmallintSql SMALLINT = new SmallintSql();
        public final static TextSql TEXT = new TextSql();
        public final static TimeSql TIME = new TimeSql();
        public final static TimestampSql TIMESTAMP = new TimestampSql();
        public final static TinyintSQL TINYINT = new TinyintSQL();
        public final static VarcharSql VARCHAR = new VarcharSql(1);

        public final static List<SqlType> ALL_SQL_TYPE = new ArrayList<>();
        static {
            ALL_SQL_TYPE.addAll(Arrays.asList(BIGINT, CHAR, DATE, DATETIME, DECIMAL, INT, SMALLINT, TEXT, TIME, TIMESTAMP, TINYINT, VARCHAR));
        }
    }
}
