/*
   Copyright 2012-2022 Michael Pozhidaev <msp@luwrain.org>

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

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.app.base.*;

import static org.luwrain.core.DefaultEventResponse.*;

final class MainLayout extends LayoutBase
{
    private final App app;
    final NavigationArea infoArea;

    private List<String> info = new ArrayList<>();

    MainLayout(App app)
    {
	super(app);
	this.app = app;
	this.infoArea = new NavigationArea(getControlContext()){
		@Override public int getLineCount()
		{
		    final int count = info.size();
		    return count >= 1?count:1;
		}
		@Override public String getLine(int index)
		{
		    if (index < info.size())
			return info.get(index);
		    return "";
		}
		@Override public String getAreaName() { return app.getStrings().appName(); }
	    };
	setAreaLayout(infoArea, null);
    }
}
