// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.linting.extensions.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * Verifies that classes implementing JsonSerializable or XmlSerializable provide a static fromJson or fromXml method.
 */
public class SerializableMethodsCheck extends SdkCheck {
    static final String ERR_NO_FROM_JSON = "Class implementing JsonSerializable must provide a static fromJson method.";
    static final String ERR_NO_FROM_XML = "Class implementing XmlSerializable must provide a static fromXml method.";

    private List<TypeSnapshot> snapshotArchive;

    /** Creates a check for the static deserialization methods required by serialization interfaces. */
    public SerializableMethodsCheck() {
    }

    @Override
    public int[] getDefaultTokens() {
        return getRequiredTokens();
    }

    @Override
    public int[] getAcceptableTokens() {
        return getRequiredTokens();
    }

    @Override
    public int[] getRequiredTokens() {
        return new int[] { TokenTypes.CLASS_DEF, TokenTypes.METHOD_DEF };
    }

    @Override
    public void beginTree(DetailAST rootNode) {
        super.beginTree(rootNode);
        snapshotArchive = isIgnoredSource() ? null : new ArrayList<>();
    }

    @Override
    public void visitToken(DetailAST currentNode) {
        if (isIgnoredSource()) {
            return;
        }
        int tokenKind = currentNode.getType();

        if (tokenKind == TokenTypes.CLASS_DEF) {
            TypeSnapshot snapshot = captureTypeSnapshot(currentNode);
            snapshotArchive.add(snapshot);
        } else if (tokenKind == TokenTypes.METHOD_DEF) {
            integrateMethodIntoSnapshot(currentNode);
        }
    }

    @Override
    public void leaveToken(DetailAST currentNode) {
        if (!isIgnoredSource() && currentNode.getType() == TokenTypes.CLASS_DEF) {
            performSnapshotAudit(currentNode);
        }
    }

    private TypeSnapshot captureTypeSnapshot(DetailAST classNode) {
        TypeSnapshot snapshot = new TypeSnapshot();

        // Check if the class is abstract - skip validation for abstract types
        snapshot.isAbstract = isAbstractType(classNode);

        DetailAST interfaceSection = classNode.findFirstToken(TokenTypes.IMPLEMENTS_CLAUSE);
        if (interfaceSection != null) {
            digestInterfaceSection(interfaceSection, snapshot);
        }

        return snapshot;
    }

    private boolean isAbstractType(DetailAST classNode) {
        DetailAST modifierBlock = classNode.findFirstToken(TokenTypes.MODIFIERS);
        if (modifierBlock == null) {
            return false;
        }
        return modifierBlock.findFirstToken(TokenTypes.ABSTRACT) != null;
    }

    private void digestInterfaceSection(DetailAST interfaceSection, TypeSnapshot snapshot) {
        DetailAST cursor = interfaceSection.getFirstChild();

        while (cursor != null) {
            if (cursor.getType() == TokenTypes.IDENT) {
                String interfaceLabel = cursor.getText();
                if ("JsonSerializable".equals(interfaceLabel)) {
                    snapshot.implementsJsonSerializable = true;
                } else if ("XmlSerializable".equals(interfaceLabel)) {
                    snapshot.implementsXmlSerializable = true;
                }
            }
            cursor = cursor.getNextSibling();
        }
    }

    private void integrateMethodIntoSnapshot(DetailAST methodNode) {
        if (snapshotArchive.isEmpty()) {
            return;
        }

        TypeSnapshot latestSnapshot = snapshotArchive.get(snapshotArchive.size() - 1);

        if (latestSnapshot.isAbstract
            || (!latestSnapshot.implementsJsonSerializable && !latestSnapshot.implementsXmlSerializable)) {
            return;
        }

        DetailAST nameNode = methodNode.findFirstToken(TokenTypes.IDENT);
        if (nameNode == null) {
            return;
        }

        String methodLabel = nameNode.getText();
        if (!"fromJson".equals(methodLabel) && !"fromXml".equals(methodLabel)) {
            return;
        }
        boolean markedStatic = probeForStaticMarker(methodNode);

        latestSnapshot.digestMethod(methodLabel, markedStatic);
    }

    private boolean probeForStaticMarker(DetailAST methodNode) {
        DetailAST modifierBlock = methodNode.findFirstToken(TokenTypes.MODIFIERS);

        if (modifierBlock == null) {
            return false;
        }

        return modifierBlock.findFirstToken(TokenTypes.LITERAL_STATIC) != null;
    }

    private void performSnapshotAudit(DetailAST classNode) {
        if (snapshotArchive.isEmpty()) {
            return;
        }

        TypeSnapshot snapshot = snapshotArchive.remove(snapshotArchive.size() - 1);

        // Skip validation for abstract types
        if (snapshot.isAbstract) {
            return;
        }

        if (snapshot.implementsJsonSerializable && !snapshot.observedFromJson) {
            log(classNode, ERR_NO_FROM_JSON);
        }

        if (snapshot.implementsXmlSerializable && !snapshot.observedFromXml) {
            log(classNode, ERR_NO_FROM_XML);
        }
    }

    private static class TypeSnapshot {
        boolean isAbstract;
        boolean implementsJsonSerializable;
        boolean implementsXmlSerializable;
        boolean observedFromJson;
        boolean observedFromXml;

        void digestMethod(String methodLabel, boolean markedStatic) {
            if ("fromJson".equals(methodLabel) && markedStatic) {
                observedFromJson = true;
            } else if ("fromXml".equals(methodLabel) && markedStatic) {
                observedFromXml = true;
            }
        }
    }
}
