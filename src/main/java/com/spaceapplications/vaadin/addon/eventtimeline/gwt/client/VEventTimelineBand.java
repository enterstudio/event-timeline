/*
@VaadinAddonLicenseForJavaFiles@
 */
package com.spaceapplications.vaadin.addon.eventtimeline.gwt.client;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.Widget;

/**
 * VEventTimelineBand.
 * 
 * @author Thomas Neidhart / Space Applications Services NV/SA
 */
public class VEventTimelineBand extends Widget implements MouseDownHandler, MouseMoveHandler,
    MouseUpHandler, NativePreviewHandler {

  private static final String CLASSNAME_BAND = VEventTimelineWidget.CLASSNAME + "-band";
  private static final String CLASSNAME_BAND_LABEL = CLASSNAME_BAND + "-label";
  private static final String CLASSNAME_BAND_ADJUSTER = "v-band-adjuster";

  private final Element bandRoot;

  private final Element bandLabel;
  private final Element bandAdjuster;

  private boolean mouseDown;
  private boolean mouseIsActive = false;

  private boolean sizeAdjust;

  private int dragStartY;

  private HandlerRegistration mouseMoveReg, mouseUpReg, mouseDownReg, preview;
  
  private int bandId;
  private VEventTimelineBandArea bandArea;

  public VEventTimelineBand(int bandId, final String caption, final VEventTimelineBandArea bandArea) {
    this.bandId = bandId;
    this.bandArea = bandArea;
    
    bandRoot = DOM.createDiv();
    setElement(bandRoot);

    setStyleName(CLASSNAME_BAND);

    bandLabel = DOM.createDiv();
    bandLabel.setClassName(CLASSNAME_BAND_LABEL);
    bandLabel.setInnerText(caption);
    bandRoot.appendChild(bandLabel);

    // FIXME: the adjuster does not work correctly in IE
    bandAdjuster = DOM.createDiv();
    bandAdjuster.setClassName(CLASSNAME_BAND_ADJUSTER);

    bandRoot.appendChild(bandAdjuster);
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    mouseDownReg = addDomHandler(this, MouseDownEvent.getType());
    mouseUpReg = addDomHandler(this, MouseUpEvent.getType());
    mouseMoveReg = addDomHandler(this, MouseMoveEvent.getType());
    preview = Event.addNativePreviewHandler(this);
  }

  
  @Override
  public void setWidth(String width) {
    super.setWidth(width);
    refreshAdjuster();
  }

  
  @Override
  public void setHeight(String height) {
    super.setHeight(height);
    refreshAdjuster();
  }
  
  public void refreshAdjuster() {
    int center = bandRoot.getOffsetWidth() / 2 - 8;
    DOM.setStyleAttribute(bandAdjuster, "left", center + "px");

    int bottom = bandRoot.getOffsetHeight() - bandLabel.getOffsetHeight() - 4;

    DOM.setStyleAttribute(bandAdjuster, "top", bottom + "px");
  }
  
  @Override
  protected void onUnload() {
    super.onUnload();
    if (mouseDownReg != null) {
      mouseDownReg.removeHandler();
      mouseDownReg = null;
    }
    if (mouseUpReg != null) {
      mouseUpReg.removeHandler();
      mouseUpReg = null;
    }
    if (mouseMoveReg != null) {
      mouseMoveReg.removeHandler();
      mouseMoveReg = null;
    }
    if (preview != null) {
      preview.removeHandler();
      preview = null;
    }
  }

  public boolean isMouseOverSizeAdjuster(Event mouseEvent) {
    Element mouseOver = (Element) Element.as(mouseEvent.getEventTarget());
    return mouseOver == bandAdjuster;
  }

  /**
   * Checks if element exists in the browser
   * 
   * @param elem
   *          The element
   * @return True if the element exists, else false
   */
  public boolean hasElement(com.google.gwt.dom.client.Element elem) {
    if (elem == getElement() || elem == bandRoot || elem == bandLabel || elem == bandAdjuster) {
      return true;
    } else {
      return false;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.gwt.event.dom.client.MouseUpHandler#onMouseUp(com.google.gwt
   * .event.dom.client.MouseUpEvent)
   */
  public void onMouseUp(MouseUpEvent event) {
    mouseIsActive = false;

    DOM.releaseCapture(bandRoot);
    sizeAdjust = false;
    
    // redraw
    bandArea.redraw();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.gwt.event.dom.client.MouseMoveHandler#onMouseMove(com.google
   * .gwt.event.dom.client.MouseMoveEvent)
   */
  public void onMouseMove(MouseMoveEvent event) {
    NativeEvent mouseEvent = event.getNativeEvent();
    if (mouseDown) {
      int adjustment = mouseEvent.getClientY() - dragStartY;
      if (sizeAdjust) {
        int newHeight = getHeight() + adjustment;
        if (bandArea.requestResize(bandId, newHeight)) {
          dragStartY = mouseEvent.getClientY();
        }
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.gwt.event.dom.client.MouseDownHandler#onMouseDown(com.google
   * .gwt.event.dom.client.MouseDownEvent)
   */
  public void onMouseDown(MouseDownEvent event) {
    NativeEvent mouseEvent = event.getNativeEvent();
    //Element mouseOver = (Element) Element.as(mouseEvent.getEventTarget());

    mouseIsActive = true;

    event.preventDefault();
    event.stopPropagation();

    DOM.setCapture(bandRoot);

    if (isMouseOverSizeAdjuster((Event) mouseEvent)) {
      sizeAdjust = true;
      dragStartY = mouseEvent.getClientY();
    }
  }

  public void onPreviewNativeEvent(NativePreviewEvent event) {
    // Monitor mouse button state
    if (event.getTypeInt() == Event.ONMOUSEUP
        && event.getNativeEvent().getButton() == NativeEvent.BUTTON_LEFT) {
      mouseDown = false;
      if (mouseIsActive) {
        onMouseUp(null);
      }
    } else if (event.getTypeInt() == Event.ONMOUSEDOWN
        && event.getNativeEvent().getButton() == NativeEvent.BUTTON_LEFT) {
      mouseDown = true;
    }
  }
  
  public int getHeight() {
    return DOM.getIntStyleAttribute(bandRoot, "height");
  }
}