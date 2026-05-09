package cn.edu.zju.crawler;

import cn.edu.zju.bean.DosingGuideline;
import cn.edu.zju.dao.DosingGuidelineDao;
import cn.edu.zju.util.SourceNormalizer;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DosingGuidelineCrawler extends BaseCrawler {

    private static final Logger log = LoggerFactory.getLogger(DosingGuidelineCrawler.class);

    public static final String URL_BASE = "https://api.pharmgkb.org/v1/data%s";
    public static final String URL_GUIDELINES = "https://api.pharmgkb.org/v1/site/guidelinesByDrugs";
    private static final Pattern EVIDENCE_PATTERN = Pattern.compile("\\b(1A|1B|2A|2B|3|4)\\b", Pattern.CASE_INSENSITIVE);

    private DosingGuidelineDao dosingGuidelineDao = new DosingGuidelineDao();

    public void doCrawlerDosingGuidelineList() {
        String content = this.getURLContent(URL_GUIDELINES);
        Gson gson = new Gson();
        Map drugLabels = gson.fromJson(content, Map.class);
        List<Map> data = (List<Map>) drugLabels.get("data");
        data.stream().forEach(x -> {
            log.info("{}", x);
            List.of("cpic", "cpnds", "dpwg", "fda", "pro").forEach(source -> {
                List<Map> guidelineList = (List<Map>) x.get(source);
                if (guidelineList == null || guidelineList.isEmpty()) {
                    log.debug("No guideline list for source={} drug={}", source, x.get("drugUrl"));
                    return;
                }
                guidelineList.forEach(guideline -> {
                    String url = (String) guideline.get("url");
                    doCrawlerDosingGuideline(url, source);
                });
            });
        });
    }

    public void doCrawlerDosingGuideline(String url, String sourceCategory) {
        String content = this.getURLContent(String.format(URL_BASE, url));
        Gson gson = new Gson();
        Map guideline = gson.fromJson(content, Map.class);
        Map data = ((Map) guideline.get("data"));
        String id = (String) data.get("id");
        String objCls = (String) data.get("objCls");
        String name = (String) data.get("name");
        boolean recommendation = (Boolean) data.get("recommendation");
        String drugId = ((String) ((List<Map>) data.get("relatedChemicals")).get(0).get("id"));
        String source = normalizeSourceCategory(sourceCategory, (String) data.get("source"));
        String evidenceLevel = extractEvidenceLevel(data);
        String summaryMarkdown = ((String) ((Map) data.get("summaryMarkdown")).get("html"));
        String textMarkdown = ((String) ((Map) data.get("textMarkdown")).get("html"));
        String raw = gson.toJson(guideline);
        DosingGuideline dosingGuideline = new DosingGuideline(id, objCls, name, recommendation, drugId, source, evidenceLevel, summaryMarkdown, textMarkdown, raw);
        if (!dosingGuidelineDao.existsById(id)) {
            dosingGuidelineDao.saveDosingGuideline(dosingGuideline);
            log.info("Saving dosing guideline: {} source={} evidence={}", id, source, evidenceLevel);
        } else {
            int updated = dosingGuidelineDao.updateMetadataById(id, source, evidenceLevel, drugId);
            log.info("Dosing guideline exists, metadata refresh id={} updated={} source={} evidence={}", id, updated, source, evidenceLevel);
        }
    }

    private String extractEvidenceLevel(Map data) {
        Object level = data.get("evidenceLevel");
        String normalized = normalizeEvidenceToken(level);
        if (normalized != null) {
            return normalized;
        }

        normalized = normalizeEvidenceToken(data.get("level"));
        if (normalized != null) {
            return normalized;
        }

        Map summary = (Map) data.get("summaryMarkdown");
        if (summary != null) {
            normalized = extractEvidenceFromText((String) summary.get("html"));
            if (normalized != null) {
                return normalized;
            }
        }

        Map text = (Map) data.get("textMarkdown");
        if (text != null) {
            normalized = extractEvidenceFromText((String) text.get("html"));
            if (normalized != null) {
                return normalized;
            }
        }
        return "4";
    }

    private String normalizeSourceCategory(String sourceCategory, String payloadSource) {
        if (sourceCategory != null && !sourceCategory.trim().isEmpty()) {
            return SourceNormalizer.normalize(sourceCategory);
        }
        if (payloadSource != null && !payloadSource.trim().isEmpty()) {
            return SourceNormalizer.normalize(payloadSource);
        }
        return SourceNormalizer.normalize(null);
    }

    private String normalizeEvidenceToken(Object value) {
        if (value == null) {
            return null;
        }
        String token = String.valueOf(value).trim().toUpperCase(Locale.ROOT);
        Matcher matcher = EVIDENCE_PATTERN.matcher(token);
        if (!matcher.find()) {
            return null;
        }
        String level = matcher.group(1).toUpperCase(Locale.ROOT);
        return level;
    }

    private String extractEvidenceFromText(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        Matcher matcher = EVIDENCE_PATTERN.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1).toUpperCase(Locale.ROOT);
    }

}
