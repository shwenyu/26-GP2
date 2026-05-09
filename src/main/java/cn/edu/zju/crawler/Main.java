package cn.edu.zju.crawler;

import cn.edu.zju.importer.CpicImporter;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {

        DrugLabelCrawler drugLabelCrawler = new DrugLabelCrawler();
        DosingGuidelineCrawler dosingGuidelineCrawler = new DosingGuidelineCrawler();

        // comment the step, if you have finished it

        // Step 1
        drugLabelCrawler.doCrawlerDrug();

        // Step 2
        drugLabelCrawler.doCrawlerDrugLabel();

        // Step 3
        dosingGuidelineCrawler.doCrawlerDosingGuidelineList();

        // Step 4 (optional): import CPIC seed TSV for Feature2 evidence/source demo
        try {
            CpicImporter cpicImporter = new CpicImporter();
            cpicImporter.importBundledSeed();
        } catch (IOException e) {
            throw new RuntimeException("Failed to import CPIC seed TSV", e);
        }
    }
}
