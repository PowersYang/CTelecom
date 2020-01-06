package com.ysir308.analysis;

import com.ysir308.analysis.tool.AnalysisTextTool;
import org.apache.hadoop.util.ToolRunner;

/**
 * 分析数据
 */
public class AnalysisData {
    public static void main(String[] args) throws Exception {

        int result = ToolRunner.run(new AnalysisTextTool(), args);


    }
}
