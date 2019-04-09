package se.flinker.document.services;

import com.itextpdf.html2pdf.css.apply.ICssApplier;
import com.itextpdf.html2pdf.css.apply.impl.DefaultCssApplierFactory;
import com.itextpdf.html2pdf.html.TagConstants;
import com.itextpdf.styledxmlparser.node.IElementNode;


public class CMYKCssApplierFactory extends DefaultCssApplierFactory {

    @Override
    public ICssApplier getCustomCssApplier(IElementNode tag) {
        if(tag.name().equals(TagConstants.DIV) || tag.name().equals(TagConstants.TD) || tag.name().equals(TagConstants.H4)){
            return new CMYKBackgroundBlockCssApplier();
        }
        return null;
    }
}
