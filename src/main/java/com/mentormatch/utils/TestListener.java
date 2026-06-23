package com.mentormatch.utils;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener implements ITestListener {
    private static final ThreadLocal<ExtentTest> TEST = new ThreadLocal<>();

    @Override
    public void onStart(ITestContext context) {
        ExtentReportManager.getReportInstance();
    }

    @Override
    public void onTestStart(ITestResult result) {
        String description = result.getMethod().getDescription();
        ExtentTest extentTest = description == null || description.isBlank()
                ? ExtentReportManager.getReportInstance().createTest(result.getMethod().getMethodName())
                : ExtentReportManager.getReportInstance().createTest(result.getMethod().getMethodName(), description);
        TEST.set(extentTest);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        getCurrentTest().pass("Test passed");
        TEST.remove();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = getCurrentTest();
        Throwable throwable = result.getThrowable();

        if (throwable != null) {
            test.fail("Test failed with exception:");
            test.fail(MarkupHelper.createCodeBlock(stackTraceOf(throwable)));
        }

        String screenshot = DriverManager.getScreenshotAsBase64();
        if (screenshot != null) {
            test.fail(
                    "Browser screenshot at failure",
                    MediaEntityBuilder.createScreenCaptureFromBase64String(screenshot).build()
            );
        }

        TEST.remove();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        getCurrentTest().skip("Test skipped");
        if (result.getThrowable() != null) {
            getCurrentTest().skip(MarkupHelper.createCodeBlock(stackTraceOf(result.getThrowable())));
        }
        TEST.remove();
    }

    @Override
    public void onFinish(ITestContext context) {
        ExtentReportManager.flushReport();
    }

    private static ExtentTest getCurrentTest() {
        ExtentTest test = TEST.get();
        if (test == null) {
            test = ExtentReportManager.getReportInstance().createTest("Untracked TestNG event");
            TEST.set(test);
        }
        return test;
    }

    private static String stackTraceOf(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
}
