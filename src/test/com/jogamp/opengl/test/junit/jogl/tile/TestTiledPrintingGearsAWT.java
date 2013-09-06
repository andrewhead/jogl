/**
 * Copyright 2013 JogAmp Community. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * 
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 * 
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY JogAmp Community ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JogAmp Community OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of JogAmp Community.
 */
 
package com.jogamp.opengl.test.junit.jogl.tile;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.jogamp.newt.event.TraceKeyAdapter;
import com.jogamp.newt.event.TraceWindowAdapter;
import com.jogamp.newt.event.awt.AWTKeyAdapter;
import com.jogamp.newt.event.awt.AWTWindowAdapter;
import com.jogamp.opengl.test.junit.jogl.demos.gl2.Gears;
import com.jogamp.opengl.test.junit.util.AWTRobotUtil;
import com.jogamp.opengl.test.junit.util.QuitAdapter;
import com.jogamp.opengl.util.Animator;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestTiledPrintingGearsAWT extends TiledPrintingAWTBase  {

    static boolean waitForKey = false;
    static GLProfile glp;
    static int width, height;
    
    @BeforeClass
    public static void initClass() {
        if(GLProfile.isAvailable(GLProfile.GL2)) {
            glp = GLProfile.get(GLProfile.GL2);
            Assert.assertNotNull(glp);
            width  = 640;
            height = 480;
        } else {
            setTestSupported(false);
        }
        // Runtime.getRuntime().traceInstructions(true);
        // Runtime.getRuntime().traceMethodCalls(true);
    }

    @AfterClass
    public static void releaseClass() {
    }
    
    protected void runTestGL(GLCapabilities caps, final boolean offscreenPrinting) throws InterruptedException, InvocationTargetException {
        final GLCanvas glCanvas = new GLCanvas(caps);
        Assert.assertNotNull(glCanvas);        
        Dimension glc_sz = new Dimension(width, height);
        glCanvas.setMinimumSize(glc_sz);
        glCanvas.setPreferredSize(glc_sz);
        glCanvas.setSize(glc_sz);
        
        final Gears gears = new Gears();
        glCanvas.addGLEventListener(gears);
        
        final Frame frame = new Frame("AWT Print (offscr "+offscreenPrinting+")");
        Assert.assertNotNull(frame);
        
        final ActionListener print72DPIAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doPrintManual(frame, glCanvas, TestTiledPrintingGearsAWT.this, offscreenPrinting, 72);
            } };
        final ActionListener print300DPIAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doPrintManual(frame, glCanvas, TestTiledPrintingGearsAWT.this, offscreenPrinting, 300);
            } };
        final Button print72DPIButton = new Button("72dpi");
        print72DPIButton.addActionListener(print72DPIAction);
        final Button print300DPIButton = new Button("300dpi");
        print300DPIButton.addActionListener(print300DPIAction);
            
        frame.setLayout(new BorderLayout());
        Panel printPanel = new Panel();
        printPanel.add(print72DPIButton);
        printPanel.add(print300DPIButton);
        Panel southPanel = new Panel();
        southPanel.add(new Label("South"));
        Panel eastPanel = new Panel();
        eastPanel.add(new Label("East"));
        Panel westPanel = new Panel();
        westPanel.add(new Label("West"));
        frame.add(printPanel, BorderLayout.NORTH);
        frame.add(glCanvas, BorderLayout.CENTER);
        frame.add(southPanel, BorderLayout.SOUTH);
        frame.add(eastPanel, BorderLayout.EAST);
        frame.add(westPanel, BorderLayout.WEST);
        frame.setTitle("Tiles AWT Print Test");
        
        Animator animator = new Animator(glCanvas);
        QuitAdapter quitAdapter = new QuitAdapter();

        new AWTKeyAdapter(new TraceKeyAdapter(quitAdapter)).addTo(glCanvas);
        new AWTWindowAdapter(new TraceWindowAdapter(quitAdapter)).addTo(frame);

        javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                frame.pack();
                frame.setVisible(true);
            }});
        Assert.assertEquals(true,  AWTRobotUtil.waitForVisible(frame, true));
        Assert.assertEquals(true,  AWTRobotUtil.waitForRealized(glCanvas, true));
        
        animator.setUpdateFPSFrames(60, System.err);        
        animator.start();

        boolean dpi72Done = false;
        boolean dpi300Done = false;
        while(!quitAdapter.shouldQuit() && animator.isAnimating() && ( 0 == duration || animator.getTotalFPSDuration()<duration )) {
            Thread.sleep(100);
            if( !dpi72Done ) {
                dpi72Done = true;
                doPrintAuto(frame, glCanvas, TestTiledPrintingGearsAWT.this, PageFormat.LANDSCAPE, null, offscreenPrinting, 72);
            } else if( !dpi300Done ) {
                dpi300Done = true;
                doPrintAuto(frame, glCanvas, TestTiledPrintingGearsAWT.this, PageFormat.LANDSCAPE, null, offscreenPrinting, 300);
            }
        }
        try { Thread.sleep(4000);  } catch (InterruptedException e) { } // time to finish print jobs .. FIXME ??
        
        Assert.assertNotNull(frame);
        Assert.assertNotNull(glCanvas);
        Assert.assertNotNull(animator);

        animator.stop();
        Assert.assertEquals(false, animator.isAnimating());
        javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                frame.setVisible(false);
            }});
        Assert.assertEquals(false, frame.isVisible());
        javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                final Frame _frame = frame;
                _frame.remove(glCanvas);
                _frame.dispose();
            }});
    }

    @Test
    public void test01_Onscreen() throws InterruptedException, InvocationTargetException {
        GLCapabilities caps = new GLCapabilities(glp);
        runTestGL(caps, false);
    }
    @Test
    public void test02_Offscreen() throws InterruptedException, InvocationTargetException {
        GLCapabilities caps = new GLCapabilities(glp);
        runTestGL(caps, true);
    }

    static long duration = 500; // ms

    public static void main(String args[]) {
        for(int i=0; i<args.length; i++) {
            if(args[i].equals("-time")) {
                i++;
                try {
                    duration = Integer.parseInt(args[i]);
                } catch (Exception ex) { ex.printStackTrace(); }
            } else if(args[i].equals("-wait")) {
                waitForKey = true;
            }
        }
        if(waitForKey) {
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            System.err.println("Press enter to continue");
            try {
                System.err.println(stdin.readLine());
            } catch (IOException e) { }
        }
        org.junit.runner.JUnitCore.main(TestTiledPrintingGearsAWT.class.getName());
    }
}
