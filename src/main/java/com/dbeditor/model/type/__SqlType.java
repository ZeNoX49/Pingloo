package com.dbeditor.model.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.dbeditor.sql.DbType;

public abstract class __SqlType {

    public abstract boolean isConform(String data);
    public abstract String getRepr(DbType dbType);

    public static __SqlType get(String baseType, DbType dbType) {
        String type = clean(baseType);

        for(__SqlType d : AllSqlType.ALL_SQL_TYPE) {
            String data = clean(d.getRepr(dbType));
            if(data.equals(type)) {
                if(d instanceof _OneModifier om) {
                    String m = baseType.trim().replace(" ", "").split("\\(")[1].split("\\)")[0];
                    om.updateData(Integer.parseInt(m));
                }
                else if(d instanceof _TwoModifier tm) {
                    String m = baseType.trim().replace(" ", "").split("\\(")[1].split("\\)")[0];
                    tm.updateData(
                        Integer.parseInt(m.split(", ")[0]),
                        Integer.parseInt(m.split(", ")[1])
                    );
                }
                return d;
            }
        }
        return null;
    }

    protected static String clean(String data) {
        data = data.trim().replace(" ", "");
        data = data.split("\\(")[0];
        return data;
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

        public final static List<__SqlType> ALL_SQL_TYPE = new ArrayList<>();
        static {
            ALL_SQL_TYPE.addAll(Arrays.asList(BIGINT, CHAR, DATE, DATETIME, DECIMAL, INT, SMALLINT, TEXT, TIME, TIMESTAMP, TINYINT, VARCHAR));
        }
    }
}
