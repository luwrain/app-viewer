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

import java.awt.Rectangle;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import java.awt.image.BufferedImage;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.canvas.GraphicsContext;
//import javafx.embed.swing.SwingFXUtils;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.scene.input.KeyEvent;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.graphical.*;

import static org.luwrain.graphical.FxThread.*;
import static org.luwrain.app.viewer.App.*;

abstract class ViewImage
{
        static private final float SCALE_STEP = 0.2f;
        static private final double OFFSET_STEP = 200.0;

    private final Luwrain luwrain;
    private final File file;
    private ResizableCanvas canvas = null;
    private Image image = null;

    private float scale = 1;
    private double
	offsetX = 0,
	offsetY = 0;

    ViewImage(Luwrain luwrain, File file) throws IOException
    {
	this.luwrain = luwrain;
	this.file = file;
	    }

    abstract void inaccessible();
    abstract void announcePage(int pageNum, int pageCount);
    abstract void announceMoveLeft();
    abstract void announceMoveRight();
    abstract void announceMoveUp();
    abstract void announceMoveDown();
    abstract void announceZoomIn();
    abstract void announceZoomOut();

    public void show()
    {
	this.luwrain.showGraphical((graphicalModeControl)->{
	try {
	    this.canvas = new ResizableCanvas();
	    this.canvas.setOnKeyPressed((event)->onKey(graphicalModeControl, event));
	    this.canvas.setVisible(true);
	    	    this.canvas.requestFocus();
		    load();
		    	    draw();
			    Log.debug(LOG_COMPONENT, "created the canvas for the image");
	    return canvas;
	}
	catch(Throwable e)
	{
	    Log.error(LOG_COMPONENT, "unable to initialize the image preview: " + e.getClass().getName() + ": " + e.getMessage());
	    e.printStackTrace();
	    this.canvas = null;
	    return null;
	}
	    });
    }

    /*
    private void moveRight()
    {
												if (setOffsetX(getOffsetX() + OFFSET_STEP))
												    announceMoveRight(); else
												    inaccessible();
    }

    private void moveLeft()
    {
												if (setOffsetX(getOffsetX() - OFFSET_STEP))
												    announceMoveLeft(); else
												    inaccessible();
    }

    private void moveUp()
    {
													if (setOffsetY(getOffsetY() - OFFSET_STEP))
													    announceMoveUp(); else
													    inaccessible();
												return;
    }

    private void moveDown()
    {
													if (setOffsetY(getOffsetY() + OFFSET_STEP))
													    announceMoveDown(); else
													    inaccessible();
    }

    private void zoomIn()
    {
		    final float newScale = getScale() + SCALE_STEP;
setScale(newScale);
announceZoomIn();
    }

    private void zoomOut()
    {
    		    final float newScale = getScale() - SCALE_STEP;
		    if (newScale < 0.5)
		    {
			inaccessible();
			return;
		    }
		    setScale(newScale);
		    announceZoomOut();
    }
    */

    public void close()
    {
	runSync(()->{
		//		interaction.closeCanvas(this.canvas);
		//		interaction.disableGraphicalMode();
		
	    });
    }

    private void load() throws IOException
    {
	//FIXME: Respect scale
		final double
	screenWidth = canvas.getWidth(),
screenHeight = canvas.getHeight();
		try (final BufferedInputStream is = new BufferedInputStream(new FileInputStream(this.file))) {
		    this.image = new Image(is, screenWidth, screenHeight, true, false);
		}
    }

    private void draw()
    {
	ensure();
	if (image == null || canvas == null)
	    return;
	final double
	imageWidth = image.getWidth(),
	imageHeight = image.getHeight(),
	screenWidth = canvas.getWidth(),
screenHeight = canvas.getHeight();
	final Fragment
	horizFrag = calcFragment(imageWidth, screenWidth, offsetX),
vertFrag = calcFragment(imageHeight, screenHeight, offsetY);
	final GraphicsContext gc = canvas.getGraphicsContext2D();
	gc.setFill(Color.BLACK);
	gc.fillRect(0, 0, screenWidth, screenHeight);
	gc.drawImage(image,
		     horizFrag.from, vertFrag.from, horizFrag.size, vertFrag.size,
		     horizFrag.to, vertFrag.to, horizFrag.size, vertFrag.size);
    }

    private void onKey(Interaction.GraphicalModeControl graphicalModeControl, KeyEvent event)
    {
ensure();
	switch(event.getCode())
	{
	case ESCAPE:
	    	    graphicalModeControl.close();
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

        static private final class Fragment
    {
	final double from, to, size;
	Fragment(double from, double to, double size)
	{
	    this.from = from;
	    this.to = to;
	    this.size = size;
	}
    }

}
