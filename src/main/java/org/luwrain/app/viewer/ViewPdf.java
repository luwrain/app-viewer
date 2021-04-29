/*
   Copyright 2012-2021 Michael Pozhidaev <msp@luwrain.org>
   Copyright 2015-2016 Roman Volovodov <gr.rPman@gmail.com>

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

import java.awt.Rectangle;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import java.awt.image.BufferedImage;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.canvas.GraphicsContext;
import javafx.embed.swing.SwingFXUtils;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.scene.input.KeyEvent;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import org.luwrain.core.*;
import org.luwrain.base.*;
import org.luwrain.core.events.*;
import org.luwrain.graphical.javafx.*;

final class ViewPdf
{
    static final String LOG_COMPONENT = "pdf";

    private final Luwrain luwrain;
    private ResizableCanvas canvas = null;
    //    private final org.luwrain.graphical.Pdf.Listener listener;

        private final PDDocument doc;
    private final PDFRenderer rend;
    private Image image = null;

    private int pageNum = 0;
    private float scale = 1;
    private double offsetX = 0;
    private double offsetY = 0;

    ViewPdf(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	this.luwrain = luwrain;
	this.doc = null;//PDDocument.load(file);
	this.rend = new PDFRenderer(doc);
	Log.debug(LOG_COMPONENT, "PDF renderer created");
    }

    public void show()
    {
	FxThread.runInFxThreadSync(()->{
	try {
	    this.canvas = new ResizableCanvas();
	    this.canvas.setOnKeyPressed((event)->onKey(event));
	    this.canvas.setVisible(false);
	    luwrain.showGraphical(canvas);
	    canvas.setVisible(true);
	    this.canvas.requestFocus();
	    drawInitial();
	}
	catch(Throwable e)
	{
	    Log.error(LOG_COMPONENT, "unable to initialize the PDF preview:" + e.getClass().getName() + ":" + e.getMessage());
	    e.printStackTrace();
	    this.canvas = null;
	}
	    });
    }

    private void drawInitial()
    {
	this.offsetX = 0;
	this.offsetY = 0;
	Log.debug(LOG_COMPONENT, "canvas " + String.format("%.2f", canvas.getWidth()) + "x" + String.format("%.2f", canvas.getHeight()));
	this.image = makeImage(pageNum, 1);
	this.scale = (float)matchingScale(image.getWidth(), image.getHeight(), canvas.getWidth(), canvas.getHeight());
	Log.debug(LOG_COMPONENT, "initial scale is " + String.format("%.2f", scale));
	this.image = makeImage(pageNum, scale);
	draw();
    }

    public void close()
    {
	FxThread.runInFxThreadSync(()->{
		//		interaction.closeCanvas(this.canvas);
		//		interaction.disableGraphicalMode();
	    });
    }

    public boolean showPage(int index)
    {
	if (index < 0 || index >= getPageCount())
	    return false;
	FxThread.runInFxThreadSync(()->{
	this.pageNum = index;
	drawInitial();
	    });
	return false;
    }

    public int getPageCount()
    {
	return doc.getNumberOfPages();
    }

    public int getCurrentPageNum()
    {
	return pageNum;
    }

    public double getOffsetX()
    {
	return offsetX;
    }

        public double getOffsetY()
    {
	return offsetY;
    }

        public boolean setOffsetX(double value)
    {
	if (value < 0)
	    return false;
	if (canvas == null || image == null)
	    return false;
	if (image.getWidth() - value < canvas.getWidth())
	    return false;
			FxThread.runInFxThreadSync(()->{
				this.offsetX = value;
				draw();
			    });
			    return true;
    }

            public boolean setOffsetY(double value)
    {
		if (value < 0)
	    return false;
	if (canvas == null || image == null)
	    return false;
	if (image.getHeight() - value < canvas.getHeight())
	    return false;
			FxThread.runInFxThreadSync(()->{
				this.offsetY = value;
				draw();
			    });
			    return true;
    }

    public float getScale()
    {
	return scale;
    }

    public void setScale(float value)
    {
	if (value < 0.5)
	    throw new IllegalArgumentException("Too small scale");
	FxThread.runInFxThreadSync(()->{
		this.scale = value;
		this.image = makeImage(pageNum, scale);
		draw();
	    });
    }
    
    private void draw()
    {
	//	InvalidThreadException.checkThread("PdfPreview.draw()");
	if (image == null || canvas == null)
	    return;
	final double imageWidth = image.getWidth();
	final double imageHeight = image.getHeight();
	final double screenWidth = canvas.getWidth();
	final double screenHeight = canvas.getHeight();
	final Fragment horizFrag = calcFragment(imageWidth, screenWidth, offsetX);
	final Fragment vertFrag = calcFragment(imageHeight, screenHeight, offsetY);
	final GraphicsContext gc = canvas.getGraphicsContext2D();
	gc.setFill(Color.BLACK);
	gc.fillRect(0, 0, screenWidth, screenHeight);
	gc.drawImage(image,
		     horizFrag.from, vertFrag.from, horizFrag.size, vertFrag.size,
		     horizFrag.to, vertFrag.to, horizFrag.size, vertFrag.size);
    }

    

    private Image makeImage(int pageNum, float scale)
    {
	//	InvalidThreadException.checkThread("PdfPreview.makeImage");
	        final BufferedImage pageImage;
        try {
            pageImage = rend.renderImage(pageNum, scale);
        }
	catch (IOException e)
	{
	    Log.error(LOG_COMPONENT, "unable to render a PDf page:" + e.getClass().getName() + ":" + e.getMessage());
	    return null;
        }
final Image image = SwingFXUtils.toFXImage(pageImage, null);
Log.debug(LOG_COMPONENT, "image " + String.format("%.2f", image.getWidth()) + "x" + String.format("%.2f", image.getHeight()));
return image;
    }

    private void onKey(KeyEvent event)
    {
	NullCheck.notNull(event, "event");
	switch(event.getCode())
	{
	case ESCAPE:
	    //	    listener.onInputEvent(new InputEvent(InputEvent.Special.ESCAPE));
	    break;
	case PAGE_DOWN:
	    //	    listener.onInputEvent(new InputEvent(InputEvent.Special.PAGE_DOWN));
	    	    break;
	case PAGE_UP:
	    //	    	    listener.onInputEvent(new InputEvent(InputEvent.Special.PAGE_UP));
		    	    break;
	case HOME:
	    //	    	    listener.onInputEvent(new InputEvent(InputEvent.Special.HOME));
		    	    break;
	case END:
	    //	    	    listener.onInputEvent(new InputEvent(InputEvent.Special.END));
		    	    break;
	case DOWN:
	    //	    	    listener.onInputEvent(new InputEvent(InputEvent.Special.ARROW_DOWN));
		    	    break;
	case UP:
	    //	    	    listener.onInputEvent(new InputEvent(InputEvent.Special.ARROW_UP));
		    	    break;
	case LEFT:
	    //	    	    listener.onInputEvent(new InputEvent(InputEvent.Special.ARROW_LEFT));
		    	    break;
			    	case RIGHT:
			    //	    	    listener.onInputEvent(new InputEvent(InputEvent.Special.ARROW_RIGHT));
		    	    break;
	case EQUALS:
	    //	    	    listener.onInputEvent(new InputEvent('='));
		    	    break;
	case MINUS:
	    //	    	    listener.onInputEvent(new InputEvent('-'));
		    	    break;
	case ENTER:
	    //	    	    listener.onInputEvent(new InputEvent(InputEvent.Special.ENTER));
		    	    break;
	default:break;
	}
    }

    static private final class Fragment
    {
	final double from;
	final double to;
	final double size;

	Fragment(double from, double to, double size)
	{
	    this.from = from;
	    this.to = to;
	    this.size = size;
	}
    }

    Fragment calcFragment(double imageSize, double screenSize, double offset)
    {
	if (imageSize < screenSize)
	    return new Fragment(0, (screenSize / 2) - (imageSize / 2), imageSize);
	return new Fragment(offset, 0, Math.min(screenSize, imageSize - offset));
    }

    static double matchingScale(double imageWidth, double imageHeight, double screenWidth, double screenHeight)
    {
	final double horizScale = screenWidth / imageWidth;
	final double vertScale = screenHeight / imageHeight;
	return Math.min(horizScale, vertScale);
    }
}