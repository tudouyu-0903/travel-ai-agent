package com.cxy.travelaiagent.tools;

import com.aliyuncs.exceptions.ClientException;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import jakarta.annotation.Resource;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

@Component
public class PDFGenerationTool {

    @Resource
    private AliyunOSSOperator aliyunOSSOperator;

    @Tool(description = "Generate a PDF file with given content and return the access link", returnDirect = true)
    public String generatePDF(
            @ToolParam(description = "Name of the file to save the generated PDF") String fileName,
            @ToolParam(description = "Content to be included in the PDF") String content) throws ClientException {

        String originalFileName = fileName;
        if (!originalFileName.endsWith(".pdf")) {
            originalFileName = originalFileName + ".pdf";
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try (PdfWriter writer = new PdfWriter(outputStream);
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {

                document.setFont(createChineseFont());
                document.add(new Paragraph(content));
            }

            byte[] pdfBytes = outputStream.toByteArray();
            String ossUrl = aliyunOSSOperator.upload(pdfBytes, originalFileName);
            return "PDF generated successfully to: " + ossUrl;
        } catch (IOException e) {
            return "Error generating PDF: " + e.getMessage();
        } catch (Exception e) {
            return "Error uploading to OSS: " + e.getMessage();
        }
    }

    public String generateTravelPdf(String fileName, String content) {
        try {
            return generatePDF(fileName, content);
        } catch (ClientException e) {
            return "Error generating PDF: " + e.getMessage();
        }
    }

    private PdfFont createChineseFont() throws IOException {
        String[] fontPaths = {
                "C:/Windows/Fonts/msyh.ttc,0",
                "C:/Windows/Fonts/simsun.ttc,0",
                "C:/Windows/Fonts/simhei.ttf"
        };

        for (String fontPath : fontPaths) {
            String filePath = fontPath.split(",")[0];
            if (new File(filePath).exists()) {
                return PdfFontFactory.createFont(fontPath, "Identity-H");
            }
        }

        return PdfFontFactory.createFont();
    }
}
