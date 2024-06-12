package se.flinker.document.utils;

import java.util.Map;

import com.itextpdf.html2pdf.attach.ProcessorContext;
import com.itextpdf.html2pdf.css.CssConstants;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.xobject.PdfXObject;
import com.itextpdf.layout.IPropertyContainer;
import com.itextpdf.layout.properties.Background;
import com.itextpdf.layout.properties.BackgroundImage;
import com.itextpdf.layout.properties.BackgroundRepeat;
import com.itextpdf.layout.properties.BackgroundRepeat.BackgroundRepeatValue;
import com.itextpdf.layout.properties.Property;
import com.itextpdf.styledxmlparser.css.util.CssDimensionParsingUtils;
import com.itextpdf.styledxmlparser.css.util.CssUtils;

/**
 * Utilities class to apply backgrounds.
 */
public final class CMYKBackgroundApplierUtil {

    /**
     * Creates a new {@link CMYKBackgroundApplierUtil} instance.
     */
    private CMYKBackgroundApplierUtil() {
    }

    /**
     * Applies background to an element.
     *
     * @param cssProps the CSS properties
     * @param context the processor context
     * @param element the element
     */
    public static void applyBackground(Map<String, String> cssProps, ProcessorContext context, IPropertyContainer element) {
        String backgroundColorStr = cssProps.get(CssConstants.BACKGROUND_COLOR);
        if (backgroundColorStr != null && !CssConstants.TRANSPARENT.equals(backgroundColorStr)) {
            float[] rgbaColor = CssDimensionParsingUtils.parseRgbaColor(backgroundColorStr);
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
                        .build();//.se (image, repeatX, repeatY);
                
                element.setProperty(Property.BACKGROUND_IMAGE, backgroundImage);
            }
        }
    }
}