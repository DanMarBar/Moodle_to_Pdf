package com.github.extractor;

import com.github.service.WebToPdfService;
import com.github.util.PdfUtils;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import java.io.File;
import java.nio.file.Path;

public class ResourcesExtractor {

    private final WebToPdfService webToPdfService;

    public ResourcesExtractor() {
        this.webToPdfService = WebToPdfService.getInstance();
    }

    public void extractPages() {
        try (final Playwright playwright = webToPdfService.startExtraction()) {
            final Browser browser = webToPdfService.openBrowser(playwright);

            final Page page = webToPdfService.openPageAndAuthenticate(browser);
            while (webToPdfService.goToNextPageIfAvailable(page)) {
                webToPdfService.createPdf(page);
            }

            final Path tempPdfsPath = webToPdfService.getTempPdfPath();
            final String filename = webToPdfService.obtainOriginalFileNameUsingUrl();
            PdfUtils.mergeAllPdfIntoOne(tempPdfsPath, filename);

        } finally {
            deleteTemporalFiles();
        }
    }

    private void deleteTemporalFiles() {
        final File tempPdfFile = webToPdfService.getTempPdfPath().toFile();
        final File[] files = tempPdfFile.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
        System.out.println("Archivos temporales eliminados");
    }
}
