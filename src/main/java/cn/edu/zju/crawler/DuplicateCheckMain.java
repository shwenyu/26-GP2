package cn.edu.zju.crawler;

import cn.edu.zju.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DuplicateCheckMain {

    public static void main(String[] args) throws Exception {
        AppConfig config = AppConfig.getInstance();
        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection connection = DriverManager.getConnection(
                config.getJdbcUrl(),
                config.getJdbcUsername(),
                config.getJdbcPassword()
        )) {
            printCount(connection, "duplicate_pk_drug", "select count(*) from (select id from drug group by id having count(*) > 1) t");
            printCount(connection, "duplicate_pk_dosing_guideline", "select count(*) from (select id from dosing_guideline group by id having count(*) > 1) t");
            printCount(connection, "duplicate_pk_drug_label", "select count(*) from (select id from drug_label group by id having count(*) > 1) t");
            printCount(connection, "duplicate_pk_user", "select count(*) from (select id from user group by id having count(*) > 1) t");
            printCount(connection, "duplicate_pk_sample", "select count(*) from (select id from sample group by id having count(*) > 1) t");
            printCount(connection, "duplicate_pk_recommendation_record", "select count(*) from (select id from recommendation_record group by id having count(*) > 1) t");

            printCount(connection, "duplicate_drug_name_normalized", "select count(*) from (select lower(replace(name,' ','')) n from drug group by n having count(*) > 1) t");
            printCount(connection, "duplicate_guideline_drug_source_name", "select count(*) from (select drug_id, source, name from dosing_guideline group by drug_id, source, name having count(*) > 1) t");
            printCount(connection, "duplicate_drug_label_drug_source_name", "select count(*) from (select drug_id, source, name from drug_label group by drug_id, source, name having count(*) > 1) t");
            printCount(connection, "duplicate_recommendation_user_sample_label", "select count(*) from (select user_id, sample_id, drug_label_id from recommendation_record group by user_id, sample_id, drug_label_id having count(*) > 1) t");
        }
    }

    private static void printCount(Connection connection, String key, String sql) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                System.out.println(key + "=" + rs.getLong(1));
            }
        }
    }
}

