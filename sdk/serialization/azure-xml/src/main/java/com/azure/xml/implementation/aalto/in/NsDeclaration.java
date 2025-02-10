// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
/* Woodstox Lite ("wool") XML processor
 *
 * Copyright (c) 2006- Tatu Saloranta, tatu.saloranta@iki.fi
 *
 * Licensed under the License specified in the file LICENSE which is
 * included with the source code.
 * You may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.azure.xml.implementation.aalto.in;

import java.util.Objects;

/**
 * This class encapsulates information about a namespace binding declaration,
 * associated with start elements. Declarations are stored as a linked list,
 * to minimize fixed allocations, and since they only need to be accessed
 * directly when dealing with START_ELEMENT and END_ELEMENT, not when
 * binding element or attribute names.
 */
public final class NsDeclaration {
    /**
     * Reference to the actual binding that will be updated by this
     * declaration (URI changed when declaration comes in and goes out
     * of scope)
     */
    private final NsBinding mBinding;

    private final String mPreviousURI;

    private final NsDeclaration mPrevDeclaration;

    /**
     * Nesting level of this declaration. Used when unbinding declarations,
     * to see if the particular declaration is associated with the start
     * element for which end element is pair.
     */
    private final int mLevel;

    public NsDeclaration(NsBinding binding, String newURI, NsDeclaration prevDecl, int level) {
        mBinding = binding;
        mPrevDeclaration = prevDecl;
        mLevel = level;

        // Also, need to update the binding itself
        mPreviousURI = binding.mURI;
        binding.mURI = newURI;
    }

    public int getLevel() {
        return mLevel;
    }

    public NsDeclaration getPrev() {
        return mPrevDeclaration;
    }

    public NsBinding getBinding() {
        return mBinding;
    }

    public String getPrefix() {
        return mBinding.mPrefix;
    }

    public String getCurrNsURI() {
        return mBinding.mURI;
    }

    public boolean hasPrefix(String prefix) {
        return prefix.equals(mBinding.mPrefix);
    }

    public boolean hasNsURI(String uri) {
        return uri.equals(mBinding.mURI);
    }

    /**
     * Method called after END_ELEMENT is processed, to unbind
     * declaration that now goes out of scope
     */
    public NsDeclaration unbind() {
        mBinding.mURI = mPreviousURI;
        return mPrevDeclaration;
    }

    public boolean alreadyDeclared(String prefix, int level) {
        if (mLevel >= level) {
            if (Objects.equals(prefix, mBinding.mPrefix)) {
                return true;
            }
            NsDeclaration prev = mPrevDeclaration;
            while (prev != null && prev.mLevel >= level) {
                if (Objects.equals(prefix, prev.mBinding.mPrefix)) {
                    return true;
                }
                prev = prev.mPrevDeclaration;
            }
        }
        return false;
    }

    public int countDeclsOnLevel(int level) {
        int count = 0;
        if (mLevel == level) {
            ++count;
            NsDeclaration prev = mPrevDeclaration;
            while (prev != null && prev.mLevel == level) {
                ++count;
                prev = prev.mPrevDeclaration;
            }
        }
        return count;
    }

    @Override
    public String toString() {
        return "[NS-DECL, prefix = <" + mBinding.mPrefix + ">, current URI <" + mBinding.mURI + ">, level " + mLevel
            + ", prev URI <" + mPreviousURI + ">]";
    }
}
