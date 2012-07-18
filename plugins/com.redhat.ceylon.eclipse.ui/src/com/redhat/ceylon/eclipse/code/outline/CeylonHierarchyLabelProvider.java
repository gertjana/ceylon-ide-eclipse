package com.redhat.ceylon.eclipse.code.outline;

import static com.redhat.ceylon.eclipse.code.propose.CeylonContentProposer.getDescriptionFor;
import static org.eclipse.jface.viewers.StyledString.QUALIFIER_STYLER;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;

import com.redhat.ceylon.compiler.typechecker.model.ClassOrInterface;
import com.redhat.ceylon.compiler.typechecker.model.Declaration;

final class CeylonHierarchyLabelProvider extends
		StyledCellLabelProvider {
	boolean isMember;
	
	public CeylonHierarchyLabelProvider() {}

	@Override
	public void removeListener(ILabelProviderListener listener) {}

	@Override
	public boolean isLabelProperty(Object element, String property) {
	    return false;
	}

	@Override
	public void dispose() {}

	@Override
	public void addListener(ILabelProviderListener listener) {}

	StyledString getStyledText(Object element) {
	    Declaration d = getDisplayedDeclaration(element);
	    StyledString result = new StyledString(getDescriptionFor(d));
	    if (d.getContainer() instanceof Declaration) {
	        result.append(" in ")
	                .append(getDescriptionFor((Declaration) d.getContainer()));
	    }
	    result.append(" - ", QUALIFIER_STYLER)
	            .append(CeylonLabelProvider.getPackageLabel(d), QUALIFIER_STYLER);
	    return result;
	}

	Declaration getDisplayedDeclaration(Object element) {
	    Declaration d = (Declaration) element;
	    if (isMember && d.isClassOrInterfaceMember()) {
	        d = (ClassOrInterface) d.getContainer();
	    }
	    return d;
	}

	@Override
	public void update(ViewerCell cell) {
		Object element = cell.getElement();
		StyledString styledText = getStyledText(element);
		cell.setText(styledText.toString());
		cell.setStyleRanges(styledText.getStyleRanges());
		cell.setImage(CeylonLabelProvider.getImage(getDisplayedDeclaration(element)));
		super.update(cell);
	}
}