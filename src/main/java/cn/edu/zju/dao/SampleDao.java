package cn.edu.zju.dao;

import cn.edu.zju.bean.Sample;
import cn.edu.zju.dbutils.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class SampleDao extends BaseDao {

    private static final Logger log = LoggerFactory.getLogger(SampleDao.class);

    public int save(Long uploadedBy) {
        AtomicInteger key = new AtomicInteger();
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement("insert into sample(created_at, uploaded_by) values (?,?)", Statement.RETURN_GENERATED_KEYS);
                preparedStatement.setTimestamp(1, new Timestamp(new Date().getTime()));
                preparedStatement.setLong(2, uploadedBy);
                preparedStatement.executeUpdate();
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                while (generatedKeys.next()) {
                    key.set(generatedKeys.getInt(1));
                }
            } catch (SQLException e) {
                log.info("Failed to save sample", e);
            }
        });
        return key.get();
    }

    public List<Sample> findAll() {
        List<Sample> samples = new ArrayList<>();
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement("select s.id, s.created_at, s.uploaded_by, u.username from sample s left join user u on s.uploaded_by = u.id order by s.id desc");
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    samples.add(mapSample(resultSet));
                }
            } catch (SQLException e) {
                log.info("Failed to query samples", e);
            }
        });
        return samples;
    }

    public Sample findById(int id) {
        AtomicReference<Sample> sample = new AtomicReference<>();
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement("select s.id, s.created_at, s.uploaded_by, u.username from sample s left join user u on s.uploaded_by = u.id where s.id = ?");
                preparedStatement.setInt(1, id);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    sample.set(mapSample(resultSet));
                }
            } catch (SQLException e) {
                log.info("Failed to query sample by id", e);
            }
        });
        return sample.get();
    }

    private Sample mapSample(ResultSet resultSet) throws SQLException {
        int sampleId = resultSet.getInt("id");
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        long uploadedByValue = resultSet.getLong("uploaded_by");
        Long uploadedBy = resultSet.wasNull() ? null : uploadedByValue;
        String username = resultSet.getString("username");
        return new Sample(sampleId, createdAt == null ? null : new Date(createdAt.getTime()), uploadedBy, username);
    }
}
