package com.redhat.ceylon.eclipse.code.propose;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;


class CompletionProposal implements ICompletionProposal, ICompletionProposalExtension4 {
    
    private final String text;
    private final Image image;
    private final boolean selectParams;
    private final String prefix;
    private final String description;
    protected int offset;
    
    CompletionProposal(int offset, String prefix, Image image,
            String desc, String text, boolean selectParams) {
        this.text=text;
        this.image = image;
        this.selectParams = selectParams;
        this.offset = offset;
        this.prefix = prefix;
        this.description = desc;
    }
    
    @Override
    public Image getImage() {
        return image;
    }
    @Override
    public Point getSelection(IDocument document) {
        /*if (text.endsWith("= ")) {
                return new Point(offset-prefix.length()+text.length(), 0);
            }
        else*/ 
        if (selectParams) {
            int locOfTypeArgs = text.indexOf('<');
            int loc = locOfTypeArgs;
            if (loc<0) loc = text.indexOf('(');
            if (loc<0) loc = text.indexOf('=')+1;
            int start;
            int length;
            if (loc<=0 || locOfTypeArgs<0 &&
                    (text.contains("()") || text.contains("{}"))) {
                start = text.endsWith("{}") ? text.length()-1 : text.length();
                length = 0;
            }
            else {
                int endOfTypeArgs = text.indexOf('>'); 
                int end = text.indexOf(',');
                if (end<0) end = text.indexOf(';');
                if (end<0) end = text.length()-1;
                if (endOfTypeArgs>0) end = end < endOfTypeArgs ? end : endOfTypeArgs;
                start = loc+1;
                length = end-loc-1;
            }
            return new Point(offset-prefix.length() + start, length);
        }
        else {
            int loc = text.indexOf("bottom;");
            int length;
            int start;
            if (loc<0) {
                start = offset + text.length()-prefix.length();
                if (text.endsWith("{}")) start--;
                length = 0;
            }
            else {
                start = offset + loc-prefix.length();
                length = 6;
            }
            return new Point(start, length);
        }
    }
    
    public void apply(IDocument document) {
        try {
            document.replace(offset-prefix.length(), prefix.length(), text);
        } 
        catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    public String getDisplayString() {
        return description;
    }

    public String getAdditionalProposalInfo() {
        return null;
    }


    public IContextInformation getContextInformation() {
        return null;
    }
    
    @Override
    public boolean isAutoInsertable() {
    	return true;
    }

}