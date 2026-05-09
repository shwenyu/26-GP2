package cn.edu.zju.controller;

import cn.edu.zju.bean.DrugLabel;
import cn.edu.zju.bean.RecommendationRecord;
import cn.edu.zju.bean.Sample;
import cn.edu.zju.bean.User;
import cn.edu.zju.dao.AnnovarDao;
import cn.edu.zju.dao.DrugLabelDao;
import cn.edu.zju.dao.RecommendationDao;
import cn.edu.zju.dao.SampleDao;
import cn.edu.zju.servlet.DispatchServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

public class MatchingController {

    private static final Logger log = LoggerFactory.getLogger(MatchingController.class);

    private SampleDao sampleDao = new SampleDao();
    private AnnovarDao annovarDao = new AnnovarDao();
    private DrugLabelDao drugLabelDao = new DrugLabelDao();
    private RecommendationDao recommendationDao = new RecommendationDao();

    public void register(DispatchServlet.Dispatcher dispatcher) {
        dispatcher.registerPostMapping("/upload", this::uploadAnnovarOutput);
        dispatcher.registerGetMapping("/matchingIndex", this::matchingIndex);
        dispatcher.registerGetMapping("/matching", this::matching);
        dispatcher.registerGetMapping("/samples", this::samples);

    }

    public void matchingIndex(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        request.getRequestDispatcher("/views/matching_index.jsp").forward(request, response);
    }

    public void samples(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        List<Sample> samples = sampleDao.findAll();
        request.setAttribute("samples", samples);
        request.getRequestDispatcher("/views/samples.jsp").forward(request, response);
    }

    public void matching(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        User currentUser = session == null ? null : (User) session.getAttribute(AuthController.CURRENT_USER);
        if (currentUser == null || currentUser.getId() == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String sampleIdParameter = request.getParameter("sampleId");
        if (sampleIdParameter == null) {
            response.sendRedirect("samples");
            return;
        }
        Integer sampleId = null;
        try {
            sampleId = Integer.valueOf(sampleIdParameter);
        } catch (NumberFormatException e) {
            response.sendRedirect("samples");
            return;
        }
        Sample sample = sampleDao.findById(sampleId);
        if (sample == null || sample.getUploadedBy() == null || !sample.getUploadedBy().equals(currentUser.getId())) {
            response.sendRedirect("samples");
            return;
        }

        List<RecommendationRecord> recommendations;
        try {
            recommendations = recommendationDao.findBySampleId(sampleId);
            if (recommendations.isEmpty()) {
                recommendations = buildRecommendationRecords(sampleId, currentUser.getId());
                recommendationDao.replaceBySample(sampleId, currentUser.getId(), recommendations);
            }
        } catch (IllegalStateException e) {
            recommendations = buildRecommendationRecords(sampleId, currentUser.getId());
            request.setAttribute("matchingWarn", "推荐记录表未初始化，当前结果仅临时展示。请执行 V20260313_01_create_recommendation_record.sql");
        }

        request.setAttribute("matchedRecords", recommendations);
        request.setAttribute("sample", sample);
        request.getRequestDispatcher("/views/matching_index_search.jsp").forward(request, response);
    }

    private List<RecommendationRecord> doMatch(int sampleId, long userId, List<String> refGenes, List<DrugLabel> drugLabels) {
        List<RecommendationRecord> matchedLabels = new ArrayList<>();
        List<String> normalizedGenes = normalizeGenes(refGenes);
        for (DrugLabel drugLabel : drugLabels) {
            if (drugLabel.getSummaryMarkdown() == null || drugLabel.getSummaryMarkdown().isEmpty()) {
                continue;
            }
            String summaryUpper = drugLabel.getSummaryMarkdown().toUpperCase(Locale.ROOT);
            StringJoiner matchedGenes = new StringJoiner(", ");
            for (String gene : normalizedGenes) {
                if (summaryUpper.contains(gene)) {
                    matchedGenes.add(gene);
                }
            }
            String matchedGeneText = matchedGenes.toString();
            if (!matchedGeneText.isEmpty()) {
                RecommendationRecord record = new RecommendationRecord();
                record.setSampleId(sampleId);
                record.setUserId(userId);
                record.setDrugLabelId(drugLabel.getId());
                record.setDrugName(drugLabel.getName());
                record.setSource(drugLabel.getSource());
                record.setSummaryMarkdown(drugLabel.getSummaryMarkdown());
                record.setMatchedGenes(matchedGeneText);
                matchedLabels.add(record);
            }
        }
        return matchedLabels;
    }

    private List<String> normalizeGenes(List<String> refGenes) {
        LinkedHashSet<String> geneSet = new LinkedHashSet<>();
        for (String refGene : refGenes) {
            if (refGene == null || refGene.isBlank()) {
                continue;
            }
            String[] pieces = refGene.split("[,;]");
            for (String piece : pieces) {
                String gene = piece == null ? "" : piece.trim().toUpperCase(Locale.ROOT);
                if (!gene.isEmpty() && !".".equals(gene)) {
                    geneSet.add(gene);
                }
            }
        }
        return new ArrayList<>(geneSet);
    }

    private List<RecommendationRecord> buildRecommendationRecords(int sampleId, long userId) {
        List<String> refGenes = annovarDao.getRefGenes(sampleId);
        if (refGenes.isEmpty()) {
            return new ArrayList<>();
        }
        List<DrugLabel> drugLabels = drugLabelDao.findAll();
        return doMatch(sampleId, userId, refGenes, drugLabels);
    }

    public void uploadAnnovarOutput(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        User currentUser = session == null ? null : (User) session.getAttribute(AuthController.CURRENT_USER);
        if (currentUser == null || currentUser.getId() == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        Part requestPart = request.getPart("annovar");
        if (requestPart == null || requestPart.getSize() == 0) {
            request.setAttribute("validateError", "annovar output file can not be blank");
            request.getRequestDispatcher("/views/matching_index_error.jsp").forward(request, response);
            return;
        }
        InputStream inputStream = requestPart.getInputStream();
        byte[] bytes = inputStream.readAllBytes();
        String content = new String(bytes, StandardCharsets.UTF_8);
        int sampleId = sampleDao.save(currentUser.getId());
        try {
            annovarDao.save(sampleId, content);
        } catch (ArrayIndexOutOfBoundsException e) {
            log.info("Invalid annovar output for sample {}", sampleId, e);
            request.setAttribute("validateError", "annovar output file is invalid");
            request.getRequestDispatcher("/views/matching_index_error.jsp").forward(request, response);
            return;
        }
        List<RecommendationRecord> recommendations = buildRecommendationRecords(sampleId, currentUser.getId());
        try {
            recommendationDao.replaceBySample(sampleId, currentUser.getId(), recommendations);
        } catch (IllegalStateException e) {
            log.warn("Recommendation table is not ready, skip persistence for sample {}", sampleId, e);
        }
        response.sendRedirect("matching?sampleId=" + sampleId);
    }
}
