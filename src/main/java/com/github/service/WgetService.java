package com.github.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WgetService {

    private static WgetService wgetService;

    private final Path wgetFilePath = Paths.get("src/main/resources/tools/wget/wget.exe")
            .toAbsolutePath();
    private final Path savedHtmlFilePath = Paths.get("src/main/resources/temporal/html")
            .toAbsolutePath();

    private WgetService() {
    }

    public static WgetService getInstance() {
        if (wgetService == null) {
            wgetService = new WgetService();
        }
        return wgetService;
    }

    public void downloadPage(String url, String cookiesPath) {
        final ProcessBuilder processBuilder = new ProcessBuilder(
                wgetFilePath.toString(),
                "--load-cookies", cookiesPath,
                "--recursive",            // Descarga todos los recursos relacionados
                "--page-requisites",      // Descarga imágenes, CSS, JS necesarios
                "--convert-links", // Convierte los enlaces para que apunten a recursos locales
                "--adjust-extension",     // Agrega extensiones adecuadas a los archivos (ej. .html)
                "--no-clobber",           // Evita sobrescribir archivos existentes
                "--directory-prefix", savedHtmlFilePath.toString(),  // Carpeta donde se guardará
                url
        );

        try {
            // Redirige la salida estándar y de error a la consola
            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

            // Ejecuta el proceso
            Process process = processBuilder.start();

            // Espera a que el proceso termine
            int exitCode = process.waitFor();
            System.out.println("El proceso terminó con el código de salida: " + exitCode);

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error al ejecutar el comando wget", e);
        }

        processBuilder.inheritIO(); 
    }

    public List<Path> obtainDownloadedHtmlsPath() {
        try (final Stream<Path> stream = Files.walk(savedHtmlFilePath)) {
            final List<Path> paths = stream //
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".html"))
                    .collect(Collectors.toList());

            System.out.println("Htmls obtenidos obtenidos:");
            paths.forEach(System.out::println);
            return paths;

        } catch (IOException e) {
            throw new RuntimeException("No se ha podido acceder a la ruta: " + savedHtmlFilePath,
                    e);
        }
    }
}
