package cn.edu.zju.crawler;

import cn.edu.zju.importer.CpicImporter;

import java.io.IOException;

public class KnowledgeBaseRefreshMain {

    public static void main(String[] args) {
        DrugLabelCrawler drugLabelCrawler = new DrugLabelCrawler();
        DosingGuidelineCrawler dosingGuidelineCrawler = new DosingGuidelineCrawler();

        // Refresh the drug table first, then refresh dosing guidelines.
        drugLabelCrawler.doCrawlerDrug();
        dosingGuidelineCrawler.doCrawlerDosingGuidelineList();

        // Keep a small deterministic CPIC seed refresh for key teaching pairs.
        try {
            CpicImporter cpicImporter = new CpicImporter();
            cpicImporter.importBundledSeed();
        } catch (IOException e) {
            throw new RuntimeException("Failed to import CPIC seed TSV", e);
        }
    }
}

