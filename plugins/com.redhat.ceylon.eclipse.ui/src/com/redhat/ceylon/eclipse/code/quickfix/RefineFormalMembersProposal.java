package com.redhat.ceylon.eclipse.code.quickfix;

import static com.redhat.ceylon.eclipse.code.editor.CeylonAutoEditStrategy.getDefaultIndent;
import static com.redhat.ceylon.eclipse.code.propose.CeylonContentProposer.getProposals;
import static com.redhat.ceylon.eclipse.code.propose.CeylonContentProposer.getRefinedProducedReference;
import static com.redhat.ceylon.eclipse.code.propose.CeylonContentProposer.getRefinementTextFor;
import static com.redhat.ceylon.eclipse.code.quickfix.CeylonQuickFixAssistant.getIndent;
import static com.redhat.ceylon.eclipse.code.quickfix.CeylonQuickFixAssistant.importSignatureTypes;
import static com.redhat.ceylon.eclipse.code.quickfix.Util.getSelectedNode;
import static org.eclipse.core.resources.ResourcesPlugin.getWorkspace;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;

import com.redhat.ceylon.compiler.typechecker.model.ClassOrInterface;
import com.redhat.ceylon.compiler.typechecker.model.Declaration;
import com.redhat.ceylon.compiler.typechecker.model.DeclarationWithProximity;
import com.redhat.ceylon.compiler.typechecker.model.ProducedReference;
import com.redhat.ceylon.compiler.typechecker.tree.Node;
import com.redhat.ceylon.compiler.typechecker.tree.Tree;
import com.redhat.ceylon.compiler.typechecker.tree.Tree.Statement;
import com.redhat.ceylon.eclipse.code.editor.CeylonEditor;
import com.redhat.ceylon.eclipse.code.propose.CeylonContentProposer;

class RefineFormalMembersProposal implements ICompletionProposal {

    private CeylonEditor editor;
    
    public RefineFormalMembersProposal(CeylonEditor editor) {
        this.editor = editor;
    }
    
    @Override
    public Point getSelection(IDocument doc) {
    	return null;
    }

    @Override
    public Image getImage() {
    	return CeylonContentProposer.FORMAL_REFINEMENT;
    }

    @Override
    public String getDisplayString() {
    	return "Refine formal members";
    }

    @Override
    public IContextInformation getContextInformation() {
    	return null;
    }

    @Override
    public String getAdditionalProposalInfo() {
    	return null;
    }

    @Override
    public void apply(IDocument doc) {
        try {
            RefineFormalMembersProposal.refineFormalMembers(editor);
        } 
        catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
    public static boolean canRefine(CeylonEditor editor) {
        Node node = getSelectedNode(editor);
        return node instanceof Tree.ClassBody ||
                node instanceof Tree.InterfaceBody ||
                node instanceof Tree.ClassDefinition ||
                node instanceof Tree.InterfaceDefinition ||
                node instanceof Tree.ObjectDefinition;
    }

    public static void refineFormalMembers(CeylonEditor editor) throws ExecutionException {
        Tree.CompilationUnit cu = editor.getParseController().getRootNode();
        if (cu==null) return;
        Node node = getSelectedNode(editor);
        IDocument document = editor.getDocumentProvider()
                .getDocument(editor.getEditorInput());
        final TextChange change = new DocumentChange("Refine Formal Members", document);
        change.setEdit(new MultiTextEdit());
        //TODO: copy/pasted from CeylonQuickFixAssistant
        Tree.Body body;
        int offset;
        if (node instanceof Tree.ClassDefinition) {
            body = ((Tree.ClassDefinition) node).getClassBody();
            offset = -1;
        }
        else if (node instanceof Tree.InterfaceDefinition) {
            body = ((Tree.InterfaceDefinition) node).getInterfaceBody();
            offset = -1;
        }
        else if (node instanceof Tree.ObjectDefinition) {
            body = ((Tree.ObjectDefinition) node).getClassBody();
            offset = -1;
        }
        else if (node instanceof Tree.ClassBody || 
                node instanceof Tree.InterfaceBody) {
            body = (Tree.Body) node;
            offset = editor.getSelectedRegion().getOffset();
        }
        else {
            //TODO run a visitor to find the containing body!
            return;//TODO popup error dialog
        }
        boolean isInterface = body instanceof Tree.InterfaceBody;
        //TODO: copy/pasted from ImplementFormalMembersProposal
        List<Statement> statements = body.getStatements();
        String indent;
        String indentAfter;
        if (statements.isEmpty()) {
            indentAfter = "\n" + getIndent(body, document);
            indent = indentAfter + getDefaultIndent();
            if (offset<0) offset = body.getStartIndex()+1;
        }
        else {
            Statement statement = statements.get(statements.size()-1);
            indent = "\n" + getIndent(statement, document);
            indentAfter = "";
            if (offset<0) offset = statement.getStopIndex()+1;
        }
        StringBuilder result = new StringBuilder();
        Set<Declaration> already = new HashSet<Declaration>();
        for (DeclarationWithProximity dwp: getProposals(node, cu).values()) {
            Declaration d = dwp.getDeclaration();
            if (d.isFormal() && 
                    ((ClassOrInterface) node.getScope()).isInheritedFromSupertype(d)) {
            	ProducedReference pr = getRefinedProducedReference(node, d);
                result.append(indent)
                    .append(getRefinementTextFor(d, pr, isInterface, indent))
                    .append(indentAfter);
                importSignatureTypes(d, cu, change, already);
            }
        }
        change.addEdit(new InsertEdit(offset, result.toString()));
        change.initializeValidationData(null);
        try {
            getWorkspace().run(new PerformChangeOperation(change), 
                    new NullProgressMonitor());
        }
        catch (CoreException ce) {
            throw new ExecutionException("Error cleaning imports", ce);
        }
    }

    public static void add(Collection<ICompletionProposal> proposals, CeylonEditor editor) {
    	if (RefineFormalMembersProposal.canRefine(editor)) {
    		proposals.add(new RefineFormalMembersProposal(editor));
    	}
    }

}