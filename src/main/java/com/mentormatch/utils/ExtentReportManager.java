package com.mentormatch.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ExtentReportManager {
    private static ExtentReports extentReports;

    private ExtentReportManager() {
    }

    public static synchronized ExtentReports getReportInstance() {
        if (extentReports == null) {
            extentReports = createReport();
        }
        return extentReports;
    }

    public static synchronized void flushReport() {
        if (extentReports != null) {
            extentReports.flush();
        }
    }

    private static ExtentReports createReport() {
        Path reportDirectory = Path.of("target", "extent-report");
        try {
            Files.createDirectories(reportDirectory);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create ExtentReports directory", exception);
        }

        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(
                reportDirectory.resolve("MentorMatch-Test-Report.html").toString()
        );
        sparkReporter.config().setDocumentTitle("MentorMatch Automation Report");
        sparkReporter.config().setReportName("MentorMatch Selenium TestNG Report");

        ExtentReports reports = new ExtentReports();
        reports.attachReporter(sparkReporter);
        reports.setSystemInfo("Application", "MentorMatch");
        reports.setSystemInfo("Base URL", ConfigReader.get("base.url"));
        reports.setSystemInfo("Browser", ConfigReader.get("browser", "chrome"));
        reports.setSystemInfo("Headless", ConfigReader.get("headless", "false"));
        return reports;
    }
}
