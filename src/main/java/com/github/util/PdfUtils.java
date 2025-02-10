package com.github.util;

import com.github.service.PropertiesService;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PdfUtils {

    private static final Path pdfPath = Paths.get(
            PropertiesService.getInstance().getProperty("pdf.path")).toAbsolutePath();

    public static void mergeAllPdfIntoOne(Path tempPdfPath, String filename) {
        final Path mergePdfPath = pdfPath.resolve(filename);

        try {
            final List<Path> tempPdfFiles = obtainTempPdfsPath(tempPdfPath);
            final PdfDocument mergedDocument;
            mergedDocument = new PdfDocument(new PdfWriter(mergePdfPath.toString()));
            tempPdfFiles.remove(0);

            for (Path tempPdfFile : tempPdfFiles) {
                final PdfDocument pdfDocument =
                        new PdfDocument(new PdfReader(tempPdfFile.toString()));
                pdfDocument.copyPagesTo(1, pdfDocument.getNumberOfPages(), mergedDocument);
                pdfDocument.close();
                System.out.println("Añadiendo: " + tempPdfFile);
            }

            mergedDocument.close();
            System.out.println("PDF combinado generado en: " + mergePdfPath);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Path> obtainTempPdfsPath(Path pdfsPath) {
        try (Stream<Path> lines = Files.walk(pdfsPath)) {
            return lines.collect(Collectors.toList());

        } catch (IOException e) {
            throw new RuntimeException("No se localizaron los pdfs en la ruta temporal", e);
        }
    }
}
