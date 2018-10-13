/*
   Copyright 2012-2018 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;

class App implements Application
{
    private Luwrain luwrain = null;
    private Strings strings = null;
    private NavigationArea area = null;
    private final File arg;

    App()
    {
	arg = null;
    }

    App(File arg)
    {
	NullCheck.notNull(arg, "arg");
	this.arg = arg;
    }

    @Override public InitResult onLaunchApp(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	final Object o = luwrain.i18n().getStrings(Strings.NAME);
	if (o == null || !(o instanceof Strings))
	    return new InitResult(InitResult.Type.NO_STRINGS_OBJ, Strings.NAME);
	this.strings = (Strings)o;
	this.luwrain = luwrain;
	createArea();
	return new InitResult();
    }

    private void createArea()
    {
	this.area = new NavigationArea(new DefaultControlEnvironment(luwrain)) {
		@Override public String getLine(int index)
		{
		    return "";
		}
		@Override public int getLineCount()
		{
		    return 1;
		}
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
		    {
		    case ESCAPE:
			closeApp();
			return true;
		    }
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() != EnvironmentEvent.Type.REGULAR)
			return super.onSystemEvent(event);
		    switch(event.getCode())
		    {
		    case CLOSE:
			closeApp();
			return true;
			/*
						{
						    try {
			    luwrain.createPdfPreview((ev)->{
				    return false;
				}, new File("/tmp/pr.pdf"));
			    				}
				catch(Exception e)
				{
				    luwrain.crash(e);
				}
						    			    return  true;
			*/
								    default:
			return super.onSystemEvent(event);
		    }
		}
		@Override public String getAreaName()
		{
		    return arg.getName();
		}
	    };
    }

    @Override public void closeApp()
    {
	luwrain.closeApp();
    }

    @Override public AreaLayout getAreaLayout()
    {
	return new AreaLayout(area);
    }

    @Override public String getAppName()
    {
	return strings.appName();
    }
}
