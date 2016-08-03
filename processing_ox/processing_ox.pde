/**
Processing Ox: a Simple MIDI Device Connector
Author: Donya Quick
Last modified: 23-July-2016

This program reproduces some of the MIDI message routing 
functionality of the program called MIDI Ox: 
http://www.midiox.com/

Running this program requires the ControlP5 library:
http://www.sojamo.de/libraries/controlP5/
*/

import java.util.ArrayList; // for lists of MIDI devices
import java.util.List; // for lists of MIDI devices
import java.util.Vector; // for storing listeners
import javax.sound.midi.*;// for working with MIDI
import controlP5.*; // for GUI elements

/************** GLOBALS ****************/

MidiHub mh; // for connecting MIDI devices

/* 
ControlP5's check boxes and radio buttons seem to require 
dedicated instances of ControlP5, so 3 are needed here. 
*/
ControlP5 outputControl; // for output device settings
ControlP5 inputControl;  // for input device settings
ControlP5 labelControl;  // for non-interactive things

/************** METHODS ****************/

/* What should we do when we start the program? */
void setup() { 
  // initial window properties
  size(500,500); 
  surface.setTitle("Processing Ox: a Simple MIDI Device Connector");
  surface.setResizable(true); // make sure we can customize the size
  
  // construct ControlP5s
  outputControl = new ControlP5(this);
  inputControl = new ControlP5(this);
  labelControl = new ControlP5(this);
  
  // MIDI Utils configuration - false shows potentially bad synths
  MidiUtils.filterDevices = true;
  
  // what input/output devices exist? Fetch their names.
  List<MidiDevice.Info> outDevsInfo = MidiUtils.getOutputDeviceInfo();
  String[] names = MidiUtils.deviceNames(outDevsInfo);
  List<MidiDevice.Info> inDevsInfo = MidiUtils.getInputDeviceInfo();
  String[] names2 = MidiUtils.deviceNames(inDevsInfo);
  
  // now we can reset the window size based on device count.
  int newH = max(100, (11*max(names.length, names2.length))+50);
  surface.setSize(500, newH);
  
  // set up the input devices check boxes
  labelControl.addLabel("INPUT DEVICES").setPosition(10,10);
  CheckBox rIn = inputControl.addCheckBox("InDevs").setPosition(10,25);
  rIn.setCaptionLabel("Input Devices");
  for (int i=0; i<names2.length; i++) {
    rIn.addItem(names2[i], i);
  }

  // set up the output devices check boxes
  labelControl.addLabel("OUTPUT DEVICES").setPosition(250,10);
  CheckBox rOut = outputControl.addCheckBox("OutDevs").setPosition(250,25);
  rOut.setCaptionLabel("Output Devices");
  for (int i=0; i<names.length; i++) {
    rOut.addItem(names[i], i);
  }
  
  // set up the MidiHub instance and open its devices.
  mh = new MidiHub();
  mh.openAll();
}

/* What should we do before exiting the program? */
void exit() {
  mh.closeAll();
}

/* Update the canvas */
void draw() {
  background(0);
}

/* What to do when the user interacts with GUI elements? */
void controlEvent(ControlEvent theEvent) {
  // Do the user interact with an output device tick box?
  if (theEvent.isGroup() && theEvent.getName().equals("OutDevs")) {
    float[] f = theEvent.getArrayValue(); // fetch boxes' states
    for (int i=0; i<f.length; i++) { // for each box...
      if(f[i]>0){ // is it ticked?
        mh.setOutputStatus(i,true); // yes, so turn on that device
      } else { 
        mh.setOutputStatus(i,false); // no, so turn it off
      }
    }
  } 
  // did the user interact with an input device tick box?
  else if (theEvent.isGroup() && theEvent.getName().equals("InDevs")) {
    float[] f = theEvent.getArrayValue(); // fetch boxes' states
    for (int i=0; i<f.length; i++) { // for each box...
      if(f[i]>0){ // is it ticked?
        mh.setInputStatus(i,true); // yes, so turn on that device 
      } else {
        mh.setInputStatus(i,false); // no, so turn it off
      }
    }
  }
}