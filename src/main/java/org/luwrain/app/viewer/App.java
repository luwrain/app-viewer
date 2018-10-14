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
import java.net.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.interaction.graphical.*;
import org.luwrain.util.*;

class App implements Application, Pdf.Listener
{
    static private final float SCALE_STEP = 0.2f;
        static private final double OFFSET_STEP = 200.0;

    private Luwrain luwrain = null;
    private Strings strings = null;
    private NavigationArea area = null;

    private final String arg;
    private Pdf pdf = null;
    private URL url = null;
    private String[] text = new String[0];

    App()
    {
	arg = null;
    }

    App(String arg)
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
	if (arg != null && !arg.isEmpty())
	    load(arg);
	return new InitResult();
    }

    private void createArea()
    {
	this.area = new NavigationArea(new DefaultControlEnvironment(luwrain)) {
		@Override public String getLine(int index)
		{
		    return index < text.length?text[index]:"";
		}
		@Override public int getLineCount()
		{
		    return text.length > 0?text.length:1;
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
		    default:
			return super.onSystemEvent(event);
		    }
		}
		@Override public String getAreaName()
		{
		    if (url == null)
			return strings.appName();
		    final File file = Urls.toFile(url);
		    return file.getName();
		}
	    };
    }

    private void  load(String file)
    {
	NullCheck.notEmpty(file, "file");
	try {
	    		this.url = new URL(file);
			this.pdf = luwrain.createPdfPreview(this, Urls.toFile(url));
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
    }

    @Override public void onInputEvent(KeyboardEvent event)
    {
	NullCheck.notNull(event, "event");
	if (!event.isSpecial() && !event.isModified())
	    switch(event.getChar())
	    {
	    case '=':
		{
		    if (pdf == null)
			return;
		    final float newScale = pdf.getScale() + SCALE_STEP;
		    pdf.setScale(newScale);
		    luwrain.message("Увеличение");
		    return;
		}
	    case '-':
		{
		    if (pdf == null)
			return;
		    final float newScale = pdf.getScale() - SCALE_STEP;
		    if (newScale < 0.5)
		    {
			luwrain.playSound(Sounds.EVENT_NOT_PROCESSED);
			return;
		    }
		    pdf.setScale(newScale);
		    luwrain.message("Уменьшение");
		    return;
		}
	    }
	if (event.isSpecial() && !event.isModified())
	    switch(event.getSpecial())
	    {
	    case PAGE_DOWN:
		if (pdf == null)
		    return;
		{
		    final int nextPage = pdf.getCurrentPageNum() + 1;
		    if (nextPage >= pdf.getPageCount())
		    {
			luwrain.playSound(Sounds.EVENT_NOT_PROCESSED);
			return;
		    }
		    pdf.showPage(nextPage);
		    luwrain.message("Страница " + (nextPage + 1) + " из " + pdf.getPageCount());//FIXME:
												}
												return;
												case PAGE_UP:
												if (pdf == null)
												return;
												{
												final int prevPage = pdf.getCurrentPageNum() - 1;
												if (prevPage < 0)
												{
												luwrain.playSound(Sounds.EVENT_NOT_PROCESSED);
												return;
												}
												pdf.showPage(prevPage);
												luwrain.message("Страница " + (prevPage + 1) + " из " + pdf.getPageCount());//FIXME:
												}
												return;
												case ARROW_RIGHT:
												if (pdf == null)
												return;
												if (pdf.setOffsetX(pdf.getOffsetX() + OFFSET_STEP))
												luwrain.message("Сдвиг вправо"); else //FIXME:
												luwrain.playSound(Sounds.EVENT_NOT_PROCESSED);
												return;
												case ARROW_LEFT:
												if (pdf == null)
												return;
												if (pdf.setOffsetX(pdf.getOffsetX() - OFFSET_STEP))
												luwrain.message("Сдвиг влево"); else//FIXME:
												luwrain.playSound(Sounds.EVENT_NOT_PROCESSED);
												return;
												case ARROW_UP:
												if (pdf == null)
												return;
												if (pdf.setOffsetY(pdf.getOffsetY() - OFFSET_STEP))
												luwrain.message("Сдвиг вверх"); else //FIXME:
												luwrain.playSound(Sounds.EVENT_NOT_PROCESSED);
												return;
												case ARROW_DOWN:
												if (pdf == null)
												return;
												if (pdf.setOffsetY(pdf.getOffsetY() + OFFSET_STEP))
												luwrain.message("Сдвиг вниз"); else //FIXME:
												luwrain.playSound(Sounds.EVENT_NOT_PROCESSED);
												return;
												case ESCAPE:
												if (pdf != null)
												{
												pdf.close();
												pdf = null;
												closeApp();
												}
												return;
												}
												luwrain.playSound(Sounds.EVENT_NOT_PROCESSED);
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
			    if (url == null)
			return strings.appName();
		    final File file = Urls.toFile(url);
		    return file.getName();
    }
}
