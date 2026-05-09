package cn.edu.zju.dao;

import cn.edu.zju.bean.AdminDataFlowRecord;
import cn.edu.zju.bean.AdminOverviewStats;
import cn.edu.zju.bean.AdminSampleSubmission;
import cn.edu.zju.bean.AdminUserStatus;
import cn.edu.zju.dbutils.DBUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class AdminDebugDao extends BaseDao {

    public AdminOverviewStats loadOverviewStats() {
        AtomicReference<AdminOverviewStats> statsRef = new AtomicReference<>(new AdminOverviewStats());
        DBUtils.execSQL(connection -> {
            try {
                boolean recommendationTableReady = isRecommendationTableReady(connection);
                String sql = recommendationTableReady
                        ? "select " +
                        "(select count(1) from `user`) as total_users, " +
                        "(select count(1) from sample) as total_samples, " +
                        "(select count(1) from recommendation_record) as total_recommendations, " +
                        "(select count(1) from sample where created_at >= curdate()) as samples_today"
                        : "select " +
                        "(select count(1) from `user`) as total_users, " +
                        "(select count(1) from sample) as total_samples, " +
                        "0 as total_recommendations, " +
                        "(select count(1) from sample where created_at >= curdate()) as samples_today";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    AdminOverviewStats stats = new AdminOverviewStats();
                    stats.setTotalUsers(resultSet.getLong("total_users"));
                    stats.setTotalSamples(resultSet.getLong("total_samples"));
                    stats.setTotalRecommendations(resultSet.getLong("total_recommendations"));
                    stats.setSamplesToday(resultSet.getLong("samples_today"));
                    statsRef.set(stats);
                }
            } catch (SQLException e) {
                throw new IllegalStateException("Failed to query admin overview stats", e);
            }
        });
        return statsRef.get();
    }

    public List<AdminUserStatus> listUserStatus(int limit) {
        List<AdminUserStatus> users = new ArrayList<>();
        DBUtils.execSQL(connection -> {
            try {
                boolean recommendationTableReady = isRecommendationTableReady(connection);
                String sql = recommendationTableReady
                        ? "select u.id, u.username, u.created_at, " +
                        "coalesce(s.sample_count, 0) as sample_count, " +
                        "coalesce(r.recommendation_count, 0) as recommendation_count, " +
                        "s.last_sample_at " +
                        "from `user` u " +
                        "left join (select uploaded_by, count(1) as sample_count, max(created_at) as last_sample_at from sample group by uploaded_by) s on s.uploaded_by = u.id " +
                        "left join (select user_id, count(1) as recommendation_count from recommendation_record group by user_id) r on r.user_id = u.id " +
                        "order by u.id desc limit ?"
                        : "select u.id, u.username, u.created_at, " +
                        "coalesce(s.sample_count, 0) as sample_count, " +
                        "0 as recommendation_count, " +
                        "s.last_sample_at " +
                        "from `user` u " +
                        "left join (select uploaded_by, count(1) as sample_count, max(created_at) as last_sample_at from sample group by uploaded_by) s on s.uploaded_by = u.id " +
                        "order by u.id desc limit ?";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setInt(1, limit);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    users.add(mapUserStatus(resultSet));
                }
            } catch (SQLException e) {
                throw new IllegalStateException("Failed to query admin user status", e);
            }
        });
        return users;
    }

    public List<AdminSampleSubmission> listRecentSubmissions(int limit) {
        List<AdminSampleSubmission> submissions = new ArrayList<>();
        DBUtils.execSQL(connection -> {
            try {
                boolean recommendationTableReady = isRecommendationTableReady(connection);
                String sql = recommendationTableReady
                        ? "select s.id as sample_id, s.created_at, u.username, " +
                        "coalesce(a.variant_count, 0) as variant_count, " +
                        "coalesce(r.recommendation_count, 0) as recommendation_count " +
                        "from sample s " +
                        "left join `user` u on u.id = s.uploaded_by " +
                        "left join (select sample_id, count(1) as variant_count from annovar group by sample_id) a on a.sample_id = s.id " +
                        "left join (select sample_id, count(1) as recommendation_count from recommendation_record group by sample_id) r on r.sample_id = s.id " +
                        "order by s.id desc limit ?"
                        : "select s.id as sample_id, s.created_at, u.username, " +
                        "coalesce(a.variant_count, 0) as variant_count, " +
                        "0 as recommendation_count " +
                        "from sample s " +
                        "left join `user` u on u.id = s.uploaded_by " +
                        "left join (select sample_id, count(1) as variant_count from annovar group by sample_id) a on a.sample_id = s.id " +
                        "order by s.id desc limit ?";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setInt(1, limit);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    submissions.add(mapSubmission(resultSet));
                }
            } catch (SQLException e) {
                throw new IllegalStateException("Failed to query admin submissions", e);
            }
        });
        return submissions;
    }

    public List<AdminDataFlowRecord> listRecentDataFlow(int limit) {
        List<AdminDataFlowRecord> records = new ArrayList<>();
        DBUtils.execSQL(connection -> {
            try {
                boolean recommendationTableReady = isRecommendationTableReady(connection);
                String sql = recommendationTableReady
                        ? "select s.id as sample_id, s.created_at, u.username, " +
                        "coalesce(a.variant_count, 0) as variant_count, " +
                        "coalesce(a.distinct_gene_count, 0) as distinct_gene_count, " +
                        "coalesce(r.recommendation_count, 0) as recommendation_count " +
                        "from sample s " +
                        "left join `user` u on u.id = s.uploaded_by " +
                        "left join (select sample_id, count(1) as variant_count, count(distinct `Gene.refGene`) as distinct_gene_count from annovar group by sample_id) a on a.sample_id = s.id " +
                        "left join (select sample_id, count(1) as recommendation_count from recommendation_record group by sample_id) r on r.sample_id = s.id " +
                        "order by s.id desc limit ?"
                        : "select s.id as sample_id, s.created_at, u.username, " +
                        "coalesce(a.variant_count, 0) as variant_count, " +
                        "coalesce(a.distinct_gene_count, 0) as distinct_gene_count, " +
                        "0 as recommendation_count " +
                        "from sample s " +
                        "left join `user` u on u.id = s.uploaded_by " +
                        "left join (select sample_id, count(1) as variant_count, count(distinct `Gene.refGene`) as distinct_gene_count from annovar group by sample_id) a on a.sample_id = s.id " +
                        "order by s.id desc limit ?";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setInt(1, limit);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    records.add(mapDataFlowRecord(resultSet));
                }
            } catch (SQLException e) {
                throw new IllegalStateException("Failed to query admin data flow", e);
            }
        });
        return records;
    }

    public boolean isRecommendationTableReady() {
        AtomicBoolean ready = new AtomicBoolean(false);
        DBUtils.execSQL(connection -> ready.set(isRecommendationTableReady(connection)));
        return ready.get();
    }

    private boolean isRecommendationTableReady(java.sql.Connection connection) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "select count(1) from information_schema.tables where table_schema = database() and table_name = 'recommendation_record'"
            );
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            return false;
        }
    }

    private AdminUserStatus mapUserStatus(ResultSet resultSet) throws SQLException {
        AdminUserStatus item = new AdminUserStatus();
        item.setUserId(resultSet.getLong("id"));
        item.setUsername(resultSet.getString("username"));
        item.setCreatedAt(toDate(resultSet.getTimestamp("created_at")));
        item.setSampleCount(resultSet.getLong("sample_count"));
        item.setRecommendationCount(resultSet.getLong("recommendation_count"));
        item.setLastSampleAt(toDate(resultSet.getTimestamp("last_sample_at")));
        return item;
    }

    private AdminSampleSubmission mapSubmission(ResultSet resultSet) throws SQLException {
        AdminSampleSubmission item = new AdminSampleSubmission();
        item.setSampleId(resultSet.getInt("sample_id"));
        item.setUsername(resultSet.getString("username"));
        item.setUploadedAt(toDate(resultSet.getTimestamp("created_at")));
        item.setVariantCount(resultSet.getLong("variant_count"));
        item.setRecommendationCount(resultSet.getLong("recommendation_count"));
        return item;
    }

    private AdminDataFlowRecord mapDataFlowRecord(ResultSet resultSet) throws SQLException {
        AdminDataFlowRecord item = new AdminDataFlowRecord();
        item.setSampleId(resultSet.getInt("sample_id"));
        item.setUsername(resultSet.getString("username"));
        item.setUploadedAt(toDate(resultSet.getTimestamp("created_at")));
        item.setVariantCount(resultSet.getLong("variant_count"));
        item.setDistinctGeneCount(resultSet.getLong("distinct_gene_count"));
        item.setRecommendationCount(resultSet.getLong("recommendation_count"));
        item.setFlowStatus(resolveFlowStatus(item.getVariantCount(), item.getRecommendationCount()));
        return item;
    }

    private Date toDate(Timestamp timestamp) {
        return timestamp == null ? null : new Date(timestamp.getTime());
    }

    private String resolveFlowStatus(long variantCount, long recommendationCount) {
        if (variantCount <= 0) {
            return "NO_VARIANT_DATA";
        }
        if (recommendationCount <= 0) {
            return "NO_MATCHED_DRUG";
        }
        return "MATCH_READY";
    }
}

