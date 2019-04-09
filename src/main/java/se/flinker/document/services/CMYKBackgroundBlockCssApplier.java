package se.flinker.document.services;

import java.util.Map;

import com.itextpdf.html2pdf.attach.ITagWorker;
import com.itextpdf.html2pdf.attach.ProcessorContext;
import com.itextpdf.html2pdf.css.apply.impl.BlockCssApplier;
import com.itextpdf.html2pdf.css.apply.util.FloatApplierUtil;
import com.itextpdf.html2pdf.css.apply.util.HyphenationApplierUtil;
import com.itextpdf.html2pdf.css.apply.util.MarginApplierUtil;
import com.itextpdf.html2pdf.css.apply.util.OpacityApplierUtil;
import com.itextpdf.html2pdf.css.apply.util.OutlineApplierUtil;
import com.itextpdf.html2pdf.css.apply.util.OverflowApplierUtil;
import com.itextpdf.html2pdf.css.apply.util.PaddingApplierUtil;
import com.itextpdf.html2pdf.css.apply.util.PageBreakApplierUtil;
import com.itextpdf.html2pdf.css.apply.util.PositionApplierUtil;
import com.itextpdf.html2pdf.css.apply.util.TransformationApplierUtil;
import com.itextpdf.html2pdf.css.apply.util.WidthHeightApplierUtil;
import com.itextpdf.layout.IPropertyContainer;
import com.itextpdf.styledxmlparser.node.IStylesContainer;

import se.flinker.document.utils.CMYKBackgroundApplierUtil;
import se.flinker.document.utils.CMYKBorderStyleApplierUtil;
import se.flinker.document.utils.CMYKFontStyleApplierUtil;

public class CMYKBackgroundBlockCssApplier extends BlockCssApplier {

    @Override
    public void apply(ProcessorContext context, IStylesContainer stylesContainer, ITagWorker tagWorker) {
        Map<String, String> cssProps = stylesContainer.getStyles();

        IPropertyContainer container = tagWorker.getElementResult();
        if (container != null) {
            WidthHeightApplierUtil.applyWidthHeight(cssProps, context, container);
            CMYKBackgroundApplierUtil.applyBackground(cssProps, context, container);
            MarginApplierUtil.applyMargins(cssProps, context, container);
            PaddingApplierUtil.applyPaddings(cssProps, context, container);
            CMYKFontStyleApplierUtil.applyFontStyles(cssProps, context, stylesContainer, container);
            CMYKBorderStyleApplierUtil.applyBorders(cssProps, context, container);
            HyphenationApplierUtil.applyHyphenation(cssProps, context, stylesContainer, container);
            FloatApplierUtil.applyFloating(cssProps, context, container);
            PositionApplierUtil.applyPosition(cssProps, context, container);
            OpacityApplierUtil.applyOpacity(cssProps, context, container);
            PageBreakApplierUtil.applyPageBreakProperties(cssProps, context, container);
            OverflowApplierUtil.applyOverflow(cssProps, container);
            TransformationApplierUtil.applyTransformation(cssProps, context, container);
            OutlineApplierUtil.applyOutlines(cssProps, context, container);
        }
    }
}
