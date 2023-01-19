/*
   Copyright 2012-2023 Michael Pozhidaev <msp@luwrain.org>

   This file is part of LUWRAIN.

   LUWRAIN is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   LUWRAIN is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.viewer;

import java.util.*;
import java.io.*;
import java.net.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.app.base.*;
import org.luwrain.util.*;

import static org.luwrain.util.UrlUtils.*;

public class App extends AppBase<Strings>
{
    static final String
	LOG_COMPONENT = "viewer";

    private final String arg;
    private URL url = null;
    private String[] text = new String[0];
    private MainLayout mainLayout = null;

    public App() { this(null); }
    public App(String arg)
    {
	super(Strings.NAME, Strings.class);
	this.arg = arg;
    }

    @Override protected AreaLayout onAppInit() throws IOException
    {
	final File file;
	if (arg != null && !arg.isEmpty())
	    file = urlToFile(arg); else
	    file = null;
	if (file != null && file.getName().toUpperCase().endsWith(".PDF"))
	{
	    final ViewPdf viewPdf = createPdfView(file);
	    viewPdf.show();
	    setAppName(file.getName());
	} else
	    setAppName(getStrings().appName());
	this.mainLayout = new MainLayout(this);
	return mainLayout.getAreaLayout();
    }

    @Override public boolean onEscape()
    {
	closeApp();
	return true;
    }

    private ViewPdf createPdfView(File file) throws IOException
    {
	return new ViewPdf(getLuwrain(), file){
	    @Override void inaccessible() {  getLuwrain().playSound(Sounds.EVENT_NOT_PROCESSED); }
	    @Override void announcePage(int pageNum, int pageCount) { message(getStrings().pdfPage(String.valueOf(pageNum), String.valueOf(pageCount)), Luwrain.MessageType.OK); }
	    @Override void announceMoveLeft() {}
	    @Override void announceMoveRight() {}
	    @Override void announceMoveUp() {}
	    @Override void announceMoveDown() {}
	    @Override void announceZoomIn() {}
	    @Override void announceZoomOut() {}
	};
    }
}
