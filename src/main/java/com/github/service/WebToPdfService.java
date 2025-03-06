package com.github.service;

import com.microsoft.playwright.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WebToPdfService {

    private static final String NEXT_BUTTON_TAGS = "#bottomPagination nav.pagination.noprt a.next";
    private final static String TEMP_PDFS_NAME_FORMAT = "tempPdf%s.pdf";
    private static final String PAGE_MODIFICATIONS = "() => { " +
            "document.body.style.margin = '0'; " +
            "const nav = document.getElementById('siteNav'); " +
            "if (nav) nav.remove(); " +
            "const images = document.querySelectorAll('img'); " +
            "images.forEach(img => { " +
            "   img.style.width = '100%'; " +
            "   img.style.height = 'auto'; " +
            "   img.style.display = 'block'; " +  // Fuerza a las imágenes a ocupar todo el ancho
            "   img.style.marginBottom = '10px'; " +  // Añade un margen debajo de la imagen
            "}); " +
            "const mainWrapper = document.getElementById('main-wrapper'); " +
            "if (mainWrapper) { " +
            "   mainWrapper.style.marginLeft = '0'; " +
            "   mainWrapper.style.marginRight = '0'; " +
            "} " +
            "document.querySelectorAll('p, span, div').forEach(el => { " +
            "   el.style.marginTop = '0'; " +  // Elimina márgenes superiores innecesarios
            "   el.style.clear = 'both'; " +  // Asegura que el texto no quede al lado de imágenes
            "}); " +
            "}";
    private static WebToPdfService webToPdfService;

    private final Path tempPdfPath = Paths.get("src/main/resources/temporal/pdf").toAbsolutePath();
    private final String email = PropertiesService.getInstance().getProperty("email");
    private final String mainPageUrl = PropertiesService.getInstance().getProperty("main.page.url");
    private final Path pdfPath = Paths.get(PropertiesService.getInstance().getProperty("pdf" +
            ".path")).toAbsolutePath();
    private int pageNumber = 0;

    private WebToPdfService() {
        createTempPdfFolder();
    }

    public static WebToPdfService getInstance() {
        if (webToPdfService == null) {
            webToPdfService = new WebToPdfService();
        }
        return webToPdfService;
    }

    public Path getTempPdfPath() {
        return tempPdfPath;
    }

    public Playwright startExtraction() {
        return Playwright.create();
    }

    public Browser openBrowser(Playwright playwright) {
        return playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
    }

    public Page openPageAndAuthenticate(Browser browser) {
        final BrowserContext context = browser.newContext();
        final Page page = context.newPage();

        page.navigate(mainPageUrl);
        page.waitForSelector("//a[contains(@href, 'auth/oauth2/login.php')]").click();
        page.waitForSelector("#identifierId").fill(email);

        page.locator("//span[text()='Siguiente']").click();
        page.waitForSelector(".exe-web-site");
        return page;
    }

    public boolean goToNextPageIfAvailable(Page page) {
        if (!page.locator(NEXT_BUTTON_TAGS).isVisible()) {
            System.out.println("No hay más páginas.");
            return false;
        }
        page.locator(NEXT_BUTTON_TAGS).click();
        System.out.println("Avanzando a la siguiente página...");
        page.waitForTimeout(1000);
        return true;
    }

    public void createPdf(Page page) {
        final Path destinationPath = createTempPdfFileName();
        page.evaluate(PAGE_MODIFICATIONS);

        page.pdf(new Page.PdfOptions()
                .setPath(destinationPath)
                .setWidth("210mm")
                .setPrintBackground(true)
        );
        System.out.println("PDF generado correctamente en: " + pdfPath);
    }

    public String obtainOriginalFileNameUsingUrl() {
        String[] splitUrl = mainPageUrl.split("/");
        return splitUrl[8].concat(".pdf");
    }

    private Path createTempPdfFileName() {
        final String filename = String.format(TEMP_PDFS_NAME_FORMAT, pageNumber);
        pageNumber++;
        return tempPdfPath.resolve(filename);
    }

    private void createTempPdfFolder() {
        try {
            Files.createDirectories(tempPdfPath);

        } catch (IOException e) {
            throw new RuntimeException("No se puede crear el fichero donde iran los archivos " +
                    "temporales.", e);
        }
    }
}
