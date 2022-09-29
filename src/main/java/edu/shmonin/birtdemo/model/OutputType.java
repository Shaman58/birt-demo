package edu.shmonin.birtdemo.model;

import org.eclipse.birt.report.engine.api.IRenderOption;

public enum OutputType {
    HTML(IRenderOption.OUTPUT_FORMAT_HTML),
    XLS(IRenderOption.OUTPUT_EMITTERID_PDF),
    INVALID("invalid");

    String value;

    OutputType(String value) {
        this.value = value;
    }

    public static OutputType getType(String text) {
        for (OutputType outputType : values()) {
            if (outputType.value.equalsIgnoreCase(text)) {
                return outputType;
            }
        }
        return INVALID;
    }
}
