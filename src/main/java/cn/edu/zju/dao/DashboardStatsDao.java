package cn.edu.zju.dao;

import cn.edu.zju.bean.DashboardStats;
import cn.edu.zju.dbutils.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

public class DashboardStatsDao {

    private static final Logger log = LoggerFactory.getLogger(DashboardStatsDao.class);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public DashboardStats loadStats() {
        DashboardStats stats = new DashboardStats();
        stats.setCoveredDrugs(countRows("drug"));
        stats.setExplainedVariants(countRows("annovar"));
        stats.setTotalUsers(countRows("user"));

        if (tableExists("recommendation_record")) {
            stats.setMatchedSamples(countDistinctSamplesInRecommendation());
        } else {
            stats.setMatchedSamples(countRows("sample"));
        }
        stats.setLastUpdated(LocalDateTime.now().format(TIME_FORMAT));
        return stats;
    }

    private long countRows(String tableName) {
        AtomicLong count = new AtomicLong(0);
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement("select count(*) as cnt from " + tableName);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    count.set(resultSet.getLong("cnt"));
                }
            } catch (SQLException e) {
                log.info("Failed to count table {}", tableName, e);
            }
        });
        return count.get();
    }

    private long countDistinctSamplesInRecommendation() {
        AtomicLong count = new AtomicLong(0);
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement("select count(distinct sample_id) as cnt from recommendation_record");
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    count.set(resultSet.getLong("cnt"));
                }
            } catch (SQLException e) {
                log.info("Failed to count matched samples from recommendation_record", e);
            }
        });
        return count.get();
    }

    private boolean tableExists(String tableName) {
        AtomicLong count = new AtomicLong(0);
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "select count(*) as cnt from information_schema.tables where table_schema = database() and table_name = ?"
                );
                preparedStatement.setString(1, tableName);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    count.set(resultSet.getLong("cnt"));
                }
            } catch (SQLException e) {
                log.info("Failed to check table existence {}", tableName, e);
            }
        });
        return count.get() > 0;
    }
}

