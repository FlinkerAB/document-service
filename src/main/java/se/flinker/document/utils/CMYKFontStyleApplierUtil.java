package se.flinker.document.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.html2pdf.attach.ProcessorContext;
import com.itextpdf.html2pdf.css.CssConstants;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.canvas.PdfCanvasConstants;
import com.itextpdf.layout.IPropertyContainer;
import com.itextpdf.layout.properties.BaseDirection;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.Leading;
import com.itextpdf.layout.properties.Property;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.TransparentColor;
import com.itextpdf.layout.properties.Underline;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.styledxmlparser.css.CommonCssConstants;
import com.itextpdf.styledxmlparser.css.util.CssDimensionParsingUtils;
import com.itextpdf.styledxmlparser.exceptions.StyledXMLParserException;
import com.itextpdf.styledxmlparser.node.IElementNode;
import com.itextpdf.styledxmlparser.node.IStylesContainer;

/**
* Utilities class to apply font styles.
*/
public final class CMYKFontStyleApplierUtil {

/** The logger. */
private static final Logger logger = LoggerFactory.getLogger(CMYKFontStyleApplierUtil.class);

/**
 * Creates a {@link CMYKFontStyleApplierUtil} instance.
 */
private CMYKFontStyleApplierUtil() {
}

/**
 * Applies font styles to an element.
 *
 * @param cssProps the CSS props
 * @param context the processor context
 * @param stylesContainer the styles container
 * @param element the element
 */
public static void applyFontStyles(Map<String, String> cssProps, ProcessorContext context, IStylesContainer stylesContainer, IPropertyContainer element) {
    float em = CssDimensionParsingUtils.parseAbsoluteLength(cssProps.get(CssConstants.FONT_SIZE));
    float rem = context.getCssContext().getRootFontSize();
    if (em != 0) {
        element.setProperty(Property.FONT_SIZE, UnitValue.createPointValue(em));
    }

    if (cssProps.get(CssConstants.FONT_FAMILY) != null) {
        element.setProperty(Property.FONT, cssProps.get(CssConstants.FONT_FAMILY));
    }
    if (cssProps.get(CssConstants.FONT_WEIGHT) != null) {
        element.setProperty(Property.FONT_WEIGHT, cssProps.get(CssConstants.FONT_WEIGHT));
    }
    if (cssProps.get(CssConstants.FONT_STYLE) != null) {
        element.setProperty(Property.FONT_STYLE, cssProps.get(CssConstants.FONT_STYLE));
    }

    String cssColorPropValue = cssProps.get(CssConstants.COLOR);
    if (cssColorPropValue != null) {
        TransparentColor transparentColor;
        if (!CssConstants.TRANSPARENT.equals(cssColorPropValue)) {
            float[] rgbaColor = CssDimensionParsingUtils.parseRgbaColor(cssColorPropValue);
            Color color = Color.convertRgbToCmyk(new DeviceRgb(rgbaColor[0], rgbaColor[1], rgbaColor[2]));
            float opacity = rgbaColor[3];
            transparentColor = new TransparentColor(color, opacity);
        } else {
            transparentColor = new TransparentColor(ColorConstants.BLACK, 0f);
        }
        element.setProperty(Property.FONT_COLOR, transparentColor);
    }

    // Make sure to place that before text-align applier
    String direction = cssProps.get(CssConstants.DIRECTION);
    if (CssConstants.RTL.equals(direction)) {
        element.setProperty(Property.BASE_DIRECTION, BaseDirection.RIGHT_TO_LEFT);
        element.setProperty(Property.TEXT_ALIGNMENT, TextAlignment.RIGHT);
    } else if (CssConstants.LTR.equals(direction)) {
        element.setProperty(Property.BASE_DIRECTION, BaseDirection.LEFT_TO_RIGHT);
        element.setProperty(Property.TEXT_ALIGNMENT, TextAlignment.LEFT);
    }

    if (stylesContainer instanceof IElementNode && ((IElementNode) stylesContainer).parentNode() instanceof IElementNode &&
            CssConstants.RTL.equals(((IElementNode) ((IElementNode) stylesContainer).parentNode()).getStyles().get(CssConstants.DIRECTION))) {
        // We should only apply horizontal alignment if parent has dir attribute or direction property
        element.setProperty(Property.HORIZONTAL_ALIGNMENT, HorizontalAlignment.RIGHT);
    }

    // Make sure to place that after direction applier
    String align = cssProps.get(CssConstants.TEXT_ALIGN);
    if (CssConstants.LEFT.equals(align)) {
        element.setProperty(Property.TEXT_ALIGNMENT, TextAlignment.LEFT);
    } else if (CssConstants.RIGHT.equals(align)) {
        element.setProperty(Property.TEXT_ALIGNMENT, TextAlignment.RIGHT);
    } else if (CssConstants.CENTER.equals(align)) {
        element.setProperty(Property.TEXT_ALIGNMENT, TextAlignment.CENTER);
    } else if (CssConstants.JUSTIFY.equals(align)) {
        element.setProperty(Property.TEXT_ALIGNMENT, TextAlignment.JUSTIFIED);
        element.setProperty(Property.SPACING_RATIO, 1f);
    }

    String textDecorationProp = cssProps.get(CssConstants.TEXT_DECORATION);
    if (textDecorationProp != null) {
        String[] textDecorations = textDecorationProp.split("\\s+");
        List<Underline> underlineList = new ArrayList<>();
        for (String textDecoration : textDecorations) {
            if (CssConstants.BLINK.equals(textDecoration)) {
                logger.error("TEXT_DECORATION_BLINK_NOT_SUPPORTED");
            } else if (CssConstants.LINE_THROUGH.equals(textDecoration)) {
                underlineList.add(new Underline(null, .75f, 0, 0, 1 / 4f, PdfCanvasConstants.LineCapStyle.BUTT));
            } else if (CssConstants.OVERLINE.equals(textDecoration)) {
                underlineList.add(new Underline(null, .75f, 0, 0, 9 / 10f, PdfCanvasConstants.LineCapStyle.BUTT));
            } else if (CssConstants.UNDERLINE.equals(textDecoration)) {
                underlineList.add(new Underline(null, .75f, 0, 0, -1 / 10f, PdfCanvasConstants.LineCapStyle.BUTT));
            } else if (CssConstants.NONE.equals(textDecoration)) {
                underlineList = null;
                // if none and any other decoration are used together, none is displayed
                break;
            }
        }
        element.setProperty(Property.UNDERLINE, underlineList);
    }

    String textIndent = cssProps.get(CssConstants.TEXT_INDENT);
    if (textIndent != null) {
        UnitValue textIndentValue = CssDimensionParsingUtils.parseLengthValueToPt(textIndent, em, rem);
        if (textIndentValue != null) {
            if (textIndentValue.isPointValue()) {
                element.setProperty(Property.FIRST_LINE_INDENT, textIndentValue.getValue());
            } else {
                logger.error("CSS_PROPERTY_IN_PERCENTS_NOT_SUPPORTED");
            }
        }
    }

    String letterSpacing = cssProps.get(CssConstants.LETTER_SPACING);
    if (letterSpacing != null && !letterSpacing.equals(CssConstants.NORMAL)) {
        UnitValue letterSpacingValue = CssDimensionParsingUtils.parseLengthValueToPt(letterSpacing, em, rem);
        if (letterSpacingValue.isPointValue()) {
            element.setProperty(Property.CHARACTER_SPACING, letterSpacingValue.getValue());
        } else {
            // browsers ignore values in percents
        }
    }

    String wordSpacing = cssProps.get(CssConstants.WORD_SPACING);
    if (wordSpacing != null) {
        UnitValue wordSpacingValue = CssDimensionParsingUtils.parseLengthValueToPt(wordSpacing, em, rem);
        if (wordSpacingValue != null) {
            if (wordSpacingValue.isPointValue()) {
                element.setProperty(Property.WORD_SPACING, wordSpacingValue.getValue());
            } else {
                // browsers ignore values in percents
            }
        }
    }

    String lineHeight = cssProps.get(CssConstants.LINE_HEIGHT);
    // specification does not give auto as a possible lineHeight value
    // nevertheless some browsers compute it as normal so we apply the same behaviour.
    // What's more, it's basically the same thing as if lineHeight is not set in the first place
    if (lineHeight != null && !CssConstants.NORMAL.equals(lineHeight) && !CssConstants.AUTO.equals(lineHeight)) {
        Float mult = CssDimensionParsingUtils.parseFloat(lineHeight);
        if (mult != null) {
            element.setProperty(Property.LEADING, new Leading(Leading.MULTIPLIED, mult));
        } else {
            UnitValue lineHeightValue = CssDimensionParsingUtils.parseLengthValueToPt(lineHeight, em, rem);
            if (lineHeightValue != null && lineHeightValue.isPointValue()) {
                element.setProperty(Property.LEADING, new Leading(Leading.FIXED, lineHeightValue.getValue()));
            } else if (lineHeightValue != null) {
                element.setProperty(Property.LEADING, new Leading(Leading.MULTIPLIED, lineHeightValue.getValue() / 100));
            }
        }
    } else {
        element.setProperty(Property.LEADING, new Leading(Leading.MULTIPLIED, 1.2f));
    }
}

/**
 * Parses the absolute font size.
 *
 * @param fontSizeValue the font size value as a {@link String}
 * @return the font size value as a {@code float}
 */
public static float parseAbsoluteFontSize(String fontSizeValue) {
    if (CommonCssConstants.FONT_ABSOLUTE_SIZE_KEYWORDS_VALUES.containsKey(fontSizeValue)) {
        switch (fontSizeValue) {
            case CssConstants.XX_SMALL:
                fontSizeValue = "9px";
                break;
            case CssConstants.X_SMALL:
                fontSizeValue = "10px";
                break;
            case CssConstants.SMALL:
                fontSizeValue = "13px";
                break;
            case CssConstants.MEDIUM:
                fontSizeValue = "16px";
                break;
            case CssConstants.LARGE:
                fontSizeValue = "18px";
                break;
            case CssConstants.X_LARGE:
                fontSizeValue = "24px";
                break;
            case CssConstants.XX_LARGE:
                fontSizeValue = "32px";
                break;
            default:
                fontSizeValue = "16px";
                break;
        }
    }
    try {
        /* Styled XML Parser will throw an exception when it can't parse the given value
           but in html2pdf, we want to fall back to the default value of 0
         */
        return CssDimensionParsingUtils.parseAbsoluteLength(fontSizeValue);
    } catch (StyledXMLParserException sxpe) {
        return 0f;
    }
}

/**
 * Parses the relative font size.
 *
 * @param relativeFontSizeValue the relative font size value as a {@link String}
 * @param baseValue the base value
 * @return the relative font size value as a {@code float}
 */
public static float parseRelativeFontSize(final String relativeFontSizeValue, final float baseValue) {
    if (CssConstants.SMALLER.equals(relativeFontSizeValue)) {
        return (float)(baseValue / 1.2);
    } else if (CssConstants.LARGER.equals(relativeFontSizeValue)) {
        return (float)(baseValue * 1.2);
    }
    return CssDimensionParsingUtils.parseRelativeValue(relativeFontSizeValue, baseValue);
}

}

