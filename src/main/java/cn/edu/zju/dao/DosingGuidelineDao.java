package cn.edu.zju.dao;

import cn.edu.zju.bean.DosingGuideline;
import cn.edu.zju.dbutils.DBUtils;
import cn.edu.zju.util.SourceNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DosingGuidelineDao extends BaseDao {

    private static final Logger log = LoggerFactory.getLogger(DosingGuidelineDao.class);
    private static final Map<String, List<String>> SOURCE_ALIASES = buildSourceAliases();

    public boolean existsById(String id) {
        return super.existsById(id, "dosing_guideline");
    }

    public void saveDosingGuideline(DosingGuideline dosingGuideline) {
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement("insert into dosing_guideline (id,obj_cls,name,recommendation,drug_id,source,evidence_level,summary_markdown,text_markdown,raw) values (?,?,?,?,?,?,?,?,?,?)");
                preparedStatement.setString(1, dosingGuideline.getId());
                preparedStatement.setString(2, dosingGuideline.getObjCls());
                preparedStatement.setString(3, truncate(dosingGuideline.getName(), 100));
                preparedStatement.setBoolean(4, dosingGuideline.isRecommendation());
                preparedStatement.setString(5, dosingGuideline.getDrugId());
                preparedStatement.setString(6, SourceNormalizer.normalize(dosingGuideline.getSource()));
                preparedStatement.setString(7, dosingGuideline.getEvidenceLevel());
                preparedStatement.setString(8, dosingGuideline.getSummaryMarkdown());
                preparedStatement.setString(9, dosingGuideline.getTextMarkdown());
                preparedStatement.setString(10, dosingGuideline.getRaw());
                preparedStatement.execute();
            } catch (SQLException e) {
                log.info("", e);
            }
        });

    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    public int updateMetadataById(String id, String source, String evidenceLevel, String drugId) {
        final int[] affected = {0};
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "update dosing_guideline " +
                                "set source = case when ? is null or trim(?) = '' then source else ? end, " +
                                "evidence_level = case when ? is null or trim(?) = '' then evidence_level else ? end, " +
                                "drug_id = case when ? is null or trim(?) = '' then drug_id else ? end " +
                                "where id = ?"
                );
                String normalizedSource = source == null ? null : SourceNormalizer.normalize(source);
                preparedStatement.setString(1, normalizedSource);
                preparedStatement.setString(2, normalizedSource);
                preparedStatement.setString(3, normalizedSource);
                preparedStatement.setString(4, evidenceLevel);
                preparedStatement.setString(5, evidenceLevel);
                preparedStatement.setString(6, evidenceLevel);
                preparedStatement.setString(7, drugId);
                preparedStatement.setString(8, drugId);
                preparedStatement.setString(9, drugId);
                preparedStatement.setString(10, id);
                affected[0] = preparedStatement.executeUpdate();
            } catch (SQLException e) {
                log.info("", e);
            }
        });
        return affected[0];
    }

    public List<DosingGuideline> findAll() {
        return findByFilters(null, null);
    }

    public List<DosingGuideline> findBySource(String source) {
        return findByFilters(normalizeSource(source), null);
    }

    public List<DosingGuideline> findByEvidenceLevel(String evidenceLevel) {
        return findByFilters(null, normalizeEvidenceLevel(evidenceLevel));
    }

    public List<DosingGuideline> findBySourceAndEvidence(String source, String evidenceLevel) {
        return findByFilters(normalizeSource(source), normalizeEvidenceLevel(evidenceLevel));
    }

    public List<DosingGuideline> findAllOrdered() {
        return findByFilters(null, null);
    }

    private List<DosingGuideline> findByFilters(String source, String evidenceLevel) {
        List<DosingGuideline> dosingGuidelines = new ArrayList<>();
        StringBuilder sql = new StringBuilder("select id,obj_cls,name,recommendation,drug_id,source,evidence_level,summary_markdown,text_markdown,raw from dosing_guideline where 1=1");
        List<String> args = new ArrayList<>();
        if (source != null) {
            List<String> sourceCandidates = resolveSourceCandidates(source);
            sql.append(" and lower(source) in (");
            for (int i = 0; i < sourceCandidates.size(); i++) {
                if (i > 0) {
                    sql.append(",");
                }
                sql.append("?");
                args.add(sourceCandidates.get(i));
            }
            sql.append(")");
        }
        if (evidenceLevel != null) {
            sql.append(" and evidence_level = ?");
            args.add(evidenceLevel);
        }
        sql.append(" order by case evidence_level ")
                .append("when '1A' then 1 ")
                .append("when '1B' then 2 ")
                .append("when '2A' then 3 ")
                .append("when '2B' then 4 ")
                .append("when '3' then 5 ")
                .append("when '4' then 6 ")
                .append("else 7 end, id asc");

        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql.toString());
                for (int i = 0; i < args.size(); i++) {
                    preparedStatement.setString(i + 1, args.get(i));
                }
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    String id = resultSet.getString("id");
                    String objCls = resultSet.getString("obj_cls");
                    String name = resultSet.getString("name");
                    boolean recommendation = resultSet.getBoolean("recommendation");
                    String drugId = resultSet.getString("drug_id");
                    String sourceValue = resultSet.getString("source");
                    String evidenceLevelValue = resultSet.getString("evidence_level");
                    String summaryMarkdown = resultSet.getString("summary_markdown");
                    String textMarkdown = resultSet.getString("text_markdown");
                    String raw = resultSet.getString("raw");
                    DosingGuideline dosingGuideline = new DosingGuideline(id, objCls, name, recommendation, drugId, sourceValue, evidenceLevelValue, summaryMarkdown, textMarkdown, raw);
                    dosingGuidelines.add(dosingGuideline);
                }
            } catch (SQLException e) {
                log.info("", e);
            }
        });
        log.info("Dosing guideline query complete source={} sourceCandidates={} evidenceLevel={} size={}", source, source == null ? Collections.emptyList() : resolveSourceCandidates(source), evidenceLevel, dosingGuidelines.size());
        return dosingGuidelines;
    }

    private String normalizeSource(String source) {
        if (source == null || source.trim().isEmpty() || "all".equalsIgnoreCase(source)) {
            return null;
        }
        return SourceNormalizer.normalize(source);
    }

    private String normalizeEvidenceLevel(String evidenceLevel) {
        if (evidenceLevel == null || evidenceLevel.trim().isEmpty() || "all".equalsIgnoreCase(evidenceLevel)) {
            return null;
        }
        return evidenceLevel.trim().toUpperCase(Locale.ROOT);
    }

    private List<String> resolveSourceCandidates(String source) {
        List<String> aliases = SOURCE_ALIASES.get(source.toLowerCase(Locale.ROOT));
        if (aliases != null && !aliases.isEmpty()) {
            return aliases;
        }
        return Collections.singletonList(source.toLowerCase(Locale.ROOT));
    }

    private static Map<String, List<String>> buildSourceAliases() {
        Map<String, List<String>> aliases = new HashMap<>();
        aliases.put("cpic", Arrays.asList("cpic", "clinical pharmacogenetics implementation consortium"));
        aliases.put("cpnds", Arrays.asList("cpnds", "canadian pharmacogenomics network for drug safety"));
        aliases.put("dpwg", Arrays.asList("dpwg", "dutch pharmacogenetics working group"));
        aliases.put("fda", Arrays.asList("fda", "u.s. food and drug administration", "us food and drug administration", "food and drug administration"));
        aliases.put("pro", Arrays.asList("pro", "professional society"));
        aliases.put("pharmgkb", Arrays.asList("pharmgkb"));
        aliases.put("unknown", Arrays.asList("unknown"));
        return aliases;
    }

}
