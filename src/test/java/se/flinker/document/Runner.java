package se.flinker.document;

import static se.flinker.document.utils.LogUtil.debug;
import static se.flinker.document.utils.LogUtil.warn;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.attach.ITagWorker;
import com.itextpdf.html2pdf.attach.ProcessorContext;
import com.itextpdf.html2pdf.css.CssConstants;
import com.itextpdf.html2pdf.css.apply.ICssApplier;
import com.itextpdf.html2pdf.css.apply.impl.BlockCssApplier;
import com.itextpdf.html2pdf.css.apply.impl.DefaultCssApplierFactory;
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
import com.itextpdf.html2pdf.html.TagConstants;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.xobject.PdfXObject;
import com.itextpdf.layout.IPropertyContainer;
import com.itextpdf.layout.font.FontProvider;
import com.itextpdf.layout.properties.Background;
import com.itextpdf.layout.properties.BackgroundImage;
import com.itextpdf.layout.properties.BackgroundRepeat;
import com.itextpdf.layout.properties.BackgroundRepeat.BackgroundRepeatValue;
import com.itextpdf.layout.properties.Property;
import com.itextpdf.styledxmlparser.css.util.CssDimensionParsingUtils;
import com.itextpdf.styledxmlparser.css.util.CssUtils;
import com.itextpdf.styledxmlparser.node.IElementNode;
import com.itextpdf.styledxmlparser.node.IStylesContainer;

import se.flinker.document.util.Fixtures;
import se.flinker.document.utils.CMYKBorderStyleApplierUtil;
import se.flinker.document.utils.CMYKFontStyleApplierUtil;

public class Runner {
    private static final Logger log = LoggerFactory.getLogger(Runner.class);
    private static final String tx = "tx";
    
    @Test
    public void runner() throws Exception {
        Path outputPath = Paths.get("target/generated_cmyk.pdf");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ConverterProperties props = createProperties(tx);
        props.setCssApplierFactory(new MyCssApplierFactory());
        HtmlConverter.convertToPdf(Fixtures.load("c.html"), baos, props);
        System.out.println(Files.write(outputPath, baos.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));
    }
    
    @Test
    public void runner_rgb() throws Exception {
        Path outputPath = Paths.get("target/generated_rgb.pdf");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ConverterProperties props = createProperties(tx);
        HtmlConverter.convertToPdf(Fixtures.load("c.html"), baos, props);
        System.out.println(Files.write(outputPath, baos.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));
    }
    
    private ConverterProperties createProperties(String tx) {
        ConverterProperties props = new ConverterProperties();
        props.setCharset("utf-8");
        try {
            FontProvider fontProvider = new DefaultFontProvider();
            Path path = Paths.get("fonts");
            if (Files.exists(path) && Files.isDirectory(path) && Files.list(path).count() > 0) {
                debug(tx, "adding fonts dir: " + path.toAbsolutePath(), log);
                fontProvider.addDirectory("fonts");
                props.setFontProvider(fontProvider);
            }
        } catch (IOException e) {
            warn(tx, "Problems adding fonts directory: " + e.getMessage(), log);
        }
        return props;
    }
    
    private static class MyCssApplierFactory extends DefaultCssApplierFactory {

        @Override
        public ICssApplier getCustomCssApplier(IElementNode tag) {
            System.out.printf("get css applier for tag [%s] [%s]\n", tag.name(), tag.getStyles());
            if(tag.name().equals(TagConstants.DIV) || tag.name().equals(TagConstants.TD) || tag.name().equals(TagConstants.H4)){ 
                return new MyBlockCssApplier();
            }
            return null;
        }
        
    }
    
    private static class MyBlockCssApplier extends BlockCssApplier {

        @Override
        public void apply(ProcessorContext context, IStylesContainer stylesContainer, ITagWorker tagWorker) {
            Map<String, String> cssProps = stylesContainer.getStyles();

            IPropertyContainer container = tagWorker.getElementResult();
            if (container != null) {
                WidthHeightApplierUtil.applyWidthHeight(cssProps, context, container);
                applyBackground(cssProps, context, container);
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
        
        void applyBackground(Map<String, String> cssProps, ProcessorContext context, IPropertyContainer element) {
            String backgroundColorStr = cssProps.get(CssConstants.BACKGROUND_COLOR);
            if (backgroundColorStr != null && !CssConstants.TRANSPARENT.equals(backgroundColorStr)) {
                float[] rgbaColor = CssDimensionParsingUtils.parseRgbaColor(backgroundColorStr);
//                Color color = new DeviceRgb(rgbaColor[0], rgbaColor[1], rgbaColor[2]);
                Color color = Color.convertRgbToCmyk(new DeviceRgb(rgbaColor[0], rgbaColor[1], rgbaColor[2]));
                float opacity = rgbaColor[3];
                Background backgroundColor = new Background(color, opacity);
                element.setProperty(Property.BACKGROUND, backgroundColor);
            }
            String backgroundImageStr = cssProps.get(CssConstants.BACKGROUND_IMAGE);
            if (backgroundImageStr != null && !backgroundImageStr.equals(CssConstants.NONE)) {
                String backgroundRepeatStr = cssProps.get(CssConstants.BACKGROUND_REPEAT);
                PdfXObject image = context.getResourceResolver().retrieveImage(CssUtils.extractUrl(backgroundImageStr));
                boolean repeatX = true, repeatY = true;
                BackgroundRepeatValue repeatXValue = BackgroundRepeatValue.REPEAT;
                BackgroundRepeatValue repeatYValue = BackgroundRepeatValue.REPEAT;
                if (backgroundRepeatStr != null) {
                    repeatX = backgroundRepeatStr.equals(CssConstants.REPEAT) || backgroundRepeatStr.equals(CssConstants.REPEAT_X);
                    repeatXValue = repeatX ? BackgroundRepeatValue.REPEAT : BackgroundRepeatValue.NO_REPEAT;
                    repeatY = backgroundRepeatStr.equals(CssConstants.REPEAT) || backgroundRepeatStr.equals(CssConstants.REPEAT_Y);
                    repeatYValue = repeatY ? BackgroundRepeatValue.REPEAT : BackgroundRepeatValue.NO_REPEAT;
                }
                if (image != null) {
                    BackgroundImage backgroundImage = new BackgroundImage.Builder()
                            .setImage(image)
                            .setBackgroundRepeat(new BackgroundRepeat(repeatXValue, repeatYValue))
                            .build();
                    element.setProperty(Property.BACKGROUND_IMAGE, backgroundImage);
                }
            }
        }
        
    }
}
