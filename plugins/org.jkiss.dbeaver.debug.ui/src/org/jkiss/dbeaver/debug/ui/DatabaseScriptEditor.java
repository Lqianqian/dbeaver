/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2023 DBeaver Corp and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jkiss.dbeaver.debug.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.osgi.util.NLS;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.DBCExecutionContext;
import org.jkiss.dbeaver.model.runtime.VoidProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.ui.editors.StringEditorInput;
import org.jkiss.dbeaver.ui.editors.sql.SQLEditorBase;
import org.jkiss.dbeaver.ui.editors.text.BaseTextDocumentProvider;

public class DatabaseScriptEditor extends SQLEditorBase {
    
    static protected final Log log = Log.getLog(DatabaseScriptEditor.class);

    private final DBCExecutionContext executionContext;
    
    public DatabaseScriptEditor(DBSObject dbsObject, String title) {
        DBCExecutionContext isolatedContext = null;
        try {
            isolatedContext = DBUtils.getObjectOwnerInstance(dbsObject).openIsolatedContext(new VoidProgressMonitor(), title, null);
        } catch (DBException e) {
            String message = NLS.bind("Unable to open execution context for {0}", dbsObject);
            log.error(message, e);
        }
        this.executionContext = isolatedContext;
        setDocumentProvider(new BaseTextDocumentProvider() {
            
            @Override
            protected void doSaveDocument(IProgressMonitor monitor, Object element, IDocument document, boolean overwrite)
                    throws CoreException {
                //we are not going to save it
            }
            
            @Override
            protected IDocument createDocument(Object element) throws CoreException {
                Document document = new Document();
                if (element instanceof StringEditorInput) {
                    StringEditorInput stringInput = (StringEditorInput) element;
                    StringBuilder buffer = stringInput.getBuffer();
                    document.set(buffer.toString());
                } else {
                    document.set(title);
                }
                return document;
            }
            
            @Override
            public boolean isReadOnly(Object element) {
                return false;
            }
            
            @Override
            public boolean isModifiable(Object element) {
                return true;
            }
        });
    }

    @Override
    public DBCExecutionContext getExecutionContext() {
        return executionContext;
    }
    
    @Override
    public void dispose() {
        if (executionContext != null) {
            executionContext.close();
        }
        super.dispose();
    }

}
