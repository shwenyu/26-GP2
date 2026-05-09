package cn.edu.zju.dao;

import cn.edu.zju.bean.RecommendationRecord;
import cn.edu.zju.dbutils.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RecommendationDao extends BaseDao {

    private static final Logger log = LoggerFactory.getLogger(RecommendationDao.class);

    public void replaceBySample(int sampleId, long userId, List<RecommendationRecord> recommendations) {
        DBUtils.execSQL(connection -> {
            try {
                connection.setAutoCommit(false);

                PreparedStatement deleteStatement = connection.prepareStatement(
                        "delete from recommendation_record where sample_id = ?"
                );
                deleteStatement.setInt(1, sampleId);
                deleteStatement.executeUpdate();

                if (recommendations != null && !recommendations.isEmpty()) {
                    PreparedStatement insertStatement = connection.prepareStatement(
                            "insert into recommendation_record(user_id, sample_id, drug_label_id, drug_name, source, summary_markdown, matched_genes, created_at) values (?, ?, ?, ?, ?, ?, ?, ?)"
                    );
                    Timestamp now = new Timestamp(new Date().getTime());
                    for (RecommendationRecord recommendation : recommendations) {
                        insertStatement.setLong(1, userId);
                        insertStatement.setInt(2, sampleId);
                        insertStatement.setString(3, recommendation.getDrugLabelId());
                        insertStatement.setString(4, recommendation.getDrugName());
                        insertStatement.setString(5, recommendation.getSource());
                        insertStatement.setString(6, recommendation.getSummaryMarkdown());
                        insertStatement.setString(7, recommendation.getMatchedGenes());
                        insertStatement.setTimestamp(8, now);
                        insertStatement.addBatch();
                    }
                    insertStatement.executeBatch();
                }

                connection.commit();
            } catch (SQLException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    log.warn("Failed to rollback recommendation transaction", rollbackEx);
                }
                throw new IllegalStateException("Failed to persist recommendation records", e);
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    log.warn("Failed to reset auto commit", e);
                }
            }
        });
    }

    public List<RecommendationRecord> findBySampleId(int sampleId) {
        List<RecommendationRecord> results = new ArrayList<>();
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "select id, user_id, sample_id, drug_label_id, drug_name, source, summary_markdown, matched_genes, created_at from recommendation_record where sample_id = ? order by id asc"
                );
                preparedStatement.setInt(1, sampleId);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    results.add(mapRecord(resultSet));
                }
            } catch (SQLException e) {
                throw new IllegalStateException("Failed to query recommendation by sample", e);
            }
        });
        return results;
    }

    public List<RecommendationRecord> findByUserId(long userId) {
        List<RecommendationRecord> results = new ArrayList<>();
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "select id, user_id, sample_id, drug_label_id, drug_name, source, summary_markdown, matched_genes, created_at from recommendation_record where user_id = ? order by created_at desc, id desc"
                );
                preparedStatement.setLong(1, userId);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    results.add(mapRecord(resultSet));
                }
            } catch (SQLException e) {
                throw new IllegalStateException("Failed to query recommendation by user", e);
            }
        });
        return results;
    }

    private RecommendationRecord mapRecord(ResultSet resultSet) throws SQLException {
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        return new RecommendationRecord(
                resultSet.getLong("id"),
                resultSet.getLong("user_id"),
                resultSet.getInt("sample_id"),
                resultSet.getString("drug_label_id"),
                resultSet.getString("drug_name"),
                resultSet.getString("source"),
                resultSet.getString("summary_markdown"),
                resultSet.getString("matched_genes"),
                createdAt == null ? null : new Date(createdAt.getTime())
        );
    }
}

