/*
   Copyright 2012-2021 Michael Pozhidaev <msp@luwrain.org>

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

class App extends AppBase<Strings>
{
    private final String arg;
    private URL url = null;
    private String[] text = new String[0];

    App()
    {
	this(null);
    }

    App(String arg)
    {
	super(Strings.NAME, Strings.class);
	this.arg = arg;
    }

    @Override protected AreaLayout onAppInit()
    {
	if (arg != null && !arg.isEmpty())
	    load(arg);
	return null;
    }

    private void  load(String file)
    {
	/*
	NullCheck.notEmpty(file, "file");
	try {
	    		this.url = new URL(file);
	}
	catch(Exception e)
	{
	    this.text = new String[]{
		"",
		"ERROR:",
		e.getClass().getName(),
		e.getMessage(),
	    };
	    luwrain.onAreaNewContent(area);
	}
	*/
    }
}
