package cn.edu.zju.importer;

import cn.edu.zju.bean.DosingGuideline;
import cn.edu.zju.dao.DosingGuidelineDao;
import cn.edu.zju.dao.DrugDao;
import cn.edu.zju.bean.Drug;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CpicImporter {

    private static final Logger log = LoggerFactory.getLogger(CpicImporter.class);
    private static final String SOURCE_CPIC = "cpic";

    private final DosingGuidelineDao dosingGuidelineDao = new DosingGuidelineDao();
    private final DrugDao drugDao = new DrugDao();

    public int importFromTsv(Path tsvPath) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(tsvPath, StandardCharsets.UTF_8)) {
            return importFromReader(reader, "file:" + tsvPath.toAbsolutePath());
        }
    }

    public int importBundledSeed() throws IOException {
        InputStream inputStream = CpicImporter.class.getClassLoader().getResourceAsStream("cpic_guideline_seed.tsv");
        if (inputStream == null) {
            throw new IOException("cpic_guideline_seed.tsv not found in classpath");
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return importFromReader(reader, "classpath:cpic_guideline_seed.tsv");
        }
    }

    private int importFromReader(BufferedReader reader, String sourceName) throws IOException {
        String headerLine = reader.readLine();
        if (headerLine == null) {
            return 0;
        }
        String[] headers = headerLine.split("\\t", -1);
        Map<String, Integer> headerIndex = indexHeaders(headers);
        Map<String, String> normalizedDrugNameToId = buildDrugNameIndex();
        int inserted = 0;
        int updated = 0;
        int skipped = 0;

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) {
                continue;
            }
            String[] cols = line.split("\\t", -1);
            String id = value(cols, headerIndex, "guideline_id");
            String drug = value(cols, headerIndex, "drug_name");
            String gene = value(cols, headerIndex, "gene");
            String recommendationText = value(cols, headerIndex, "recommendation");
            String evidenceLevel = normalizeEvidence(value(cols, headerIndex, "evidence_level"));
            String summary = value(cols, headerIndex, "summary");
            String drugId = resolveDrugId(drug, normalizedDrugNameToId);

            if (id.isEmpty()) {
                id = buildFallbackId(drug, gene);
            }
            if (dosingGuidelineDao.existsById(id)) {
                int updatedRows = dosingGuidelineDao.updateMetadataById(id, SOURCE_CPIC, evidenceLevel, drugId);
                if (updatedRows > 0) {
                    updated++;
                    log.info("CPIC guideline metadata refreshed id={} evidence={} drugId={}", id, evidenceLevel, drugId);
                } else {
                    skipped++;
                }
                continue;
            }
            DosingGuideline dosingGuideline = new DosingGuideline(
                    id,
                    "GuidelineAnnotation",
                    buildName(drug, gene),
                    true,
                    drugId,
                    SOURCE_CPIC,
                    evidenceLevel,
                    summary,
                    recommendationText,
                    line
            );
            dosingGuidelineDao.saveDosingGuideline(dosingGuideline);
            if (drugId == null) {
                log.warn("CPIC guideline has unresolved drug_id id={} drug={} gene={}", id, drug, gene);
            }
            inserted++;
        }

        log.info("CPIC import done source={} inserted={} updated={} skipped={}", sourceName, inserted, updated, skipped);
        return inserted;
    }

    private Map<String, Integer> indexHeaders(String[] headers) {
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            index.put(headers[i].trim().toLowerCase(Locale.ROOT), i);
        }
        return index;
    }

    private String value(String[] cols, Map<String, Integer> headerIndex, String key) {
        Integer idx = headerIndex.get(key);
        if (idx == null || idx < 0 || idx >= cols.length) {
            return "";
        }
        return cols[idx].trim();
    }

    private String buildFallbackId(String drug, String gene) {
        String safeDrug = (drug == null || drug.isEmpty()) ? "UNKNOWN_DRUG" : drug;
        String safeGene = (gene == null || gene.isEmpty()) ? "UNKNOWN_GENE" : gene;
        return ("CPIC_" + safeDrug + "_" + safeGene)
                .replaceAll("[^A-Za-z0-9_]+", "_")
                .toUpperCase(Locale.ROOT);
    }

    private String buildName(String drug, String gene) {
        if ((drug == null || drug.isEmpty()) && (gene == null || gene.isEmpty())) {
            return "CPIC Guideline";
        }
        return String.format("%s guideline (%s)", emptyAsUnknown(drug), emptyAsUnknown(gene));
    }

    private String emptyAsUnknown(String value) {
        return (value == null || value.isEmpty()) ? "Unknown" : value;
    }

    private String normalizeEvidence(String level) {
        String normalized = level == null ? "" : level.trim().toUpperCase(Locale.ROOT);
        switch (normalized) {
            case "1A":
            case "1B":
            case "2A":
            case "2B":
            case "3":
            case "4":
                return normalized;
            default:
                return "4";
        }
    }

    private Map<String, String> buildDrugNameIndex() {
        List<Drug> drugs = drugDao.findAll();
        Map<String, String> index = new HashMap<>();
        for (Drug drug : drugs) {
            if (drug.getName() == null || drug.getId() == null) {
                continue;
            }
            String normalizedName = normalizeDrugName(drug.getName());
            if (!normalizedName.isEmpty()) {
                index.putIfAbsent(normalizedName, drug.getId());
            }
        }
        log.info("CPIC importer loaded {} drug names for id mapping", index.size());
        return index;
    }

    private String resolveDrugId(String drugName, Map<String, String> normalizedDrugNameToId) {
        if (drugName == null || drugName.trim().isEmpty()) {
            return null;
        }
        String normalized = normalizeDrugName(drugName);
        return normalizedDrugNameToId.get(normalized);
    }

    private String normalizeDrugName(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "");
    }

    public static void main(String[] args) throws IOException {
        CpicImporter importer = new CpicImporter();
        int inserted;
        if (args.length > 0) {
            inserted = importer.importFromTsv(Path.of(args[0]));
        } else {
            inserted = importer.importBundledSeed();
        }
        log.info("CPIC importer finished inserted={}", inserted);
    }
}


