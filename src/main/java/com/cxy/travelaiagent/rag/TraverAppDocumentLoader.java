package com.cxy.travelaiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/***
 * loveApp 文档加载器
 */
@Component
@Slf4j
public class TraverAppDocumentLoader {
    private final ResourcePatternResolver resourcePatternResolver;
    //用来加载多个文档
    public TraverAppDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    /***
     * 加载多个文档
     * @return
     */
    public List<Document>  loadMarkdowns(){
        List<Document> allDocuments= new ArrayList<>();
        //加载多篇 文档
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath*:document/*.md");
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeCodeBlock(false)
                        .withIncludeBlockquote(false)
                        .withAdditionalMetadata("filename", filename)
                        .build();
                MarkdownDocumentReader markdownDocumentReader = new MarkdownDocumentReader(resource, config);
                allDocuments.addAll(markdownDocumentReader.get());
            }
        } catch (IOException e) {
            log.error("Markdown文档加载失败");
        }
        return allDocuments;
    }
}
